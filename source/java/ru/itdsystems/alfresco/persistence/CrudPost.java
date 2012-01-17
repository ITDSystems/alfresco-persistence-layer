/**
 * Copyright © 2011 ITD Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.itdsystems.alfresco.persistence;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Webscript (Persistence API Post) implementation to perform a search inside
 * repository.
 * 
 * @author Alexey Ermakov
 * 
 */

public class CrudPost extends AbstractCrudWebScript {

	NodeService nodeService;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		nodeService = serviceRegistry.getNodeService();
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws WebScriptException {
		// construct path elements array from request parameters
		List<String> pathElements = new ArrayList<String>();
		Map<String, String> templateVars = req.getServiceMatch()
				.getTemplateVars();
		pathElements.add(templateVars.get("application_name"));
		pathElements.add(templateVars.get("form_name"));
		doBefore(pathElements, null);
		// parse xml from request and perform a search
		Document searchXML;
		DocumentBuilder xmlBuilder;
		DocumentBuilderFactory xmlFact;
		try {
			xmlFact = DocumentBuilderFactory.newInstance();
			xmlFact.setNamespaceAware(true);
			xmlBuilder = xmlFact.newDocumentBuilder();
			searchXML = xmlBuilder.parse(req.getContent().getInputStream());
		} catch (Exception e) {
			throw new WebScriptException(500,
					"Error occured while parsing XML from request.", e);
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		Integer pageSize;
		Integer pageNumber;
		String lang;
		// String applicationName;
		// String formName;
		NodeList queries;
		// extract search details
		try {
			pageSize = new Integer(
					((Node) xpath.evaluate("/search/page-size/text()",
							searchXML, XPathConstants.NODE)).getNodeValue());
			pageNumber = new Integer(((Node) xpath.evaluate(
					"/search/page-number/text()", searchXML,
					XPathConstants.NODE)).getNodeValue());
			lang = ((Node) xpath.evaluate("/search/lang/text()",
					searchXML, XPathConstants.NODE)).getNodeValue();
			// applicationName = ((Node) xpath.evaluate("/search/app/text()",
			// searchXML, XPathConstants.NODE)).getNodeValue();
			// formName = ((Node) xpath.evaluate("/search/form/text()",
			// searchXML,
			// XPathConstants.NODE)).getNodeValue();
			queries = (NodeList) xpath.evaluate("/search/query", searchXML,
					XPathConstants.NODESET);
			if (queries.getLength() == 0)
				throw new Exception("No queries found.");
		} catch (Exception e) {
			throw new WebScriptException(500, "XML in request is malformed.", e);
		}
		// check if requested query is supported
		if (!"".equals(queries.item(0).getTextContent()))
			throw new WebScriptException(500,
					"Freetext queries are not supported at the moment.");
		// resolve path to root data
		pathElements.add("data");
		NodeRef nodeRef = getRootNodeRef();
		// resolve path to file
		FileInfo fileInfo = null;
		Integer totalForms = 0;
		try {
//			fileInfo = fileFolderService.resolveNamePath(nodeRef, pathElements, false);
			fileInfo = fileFolderService.resolveNamePath(nodeRef, pathElements);
		} catch (FileNotFoundException e) {
			// do nothing here
		}
		if (fileInfo != null) {
			// iterate through all forms
			List<ChildAssociationRef> assocs = nodeService
					.getChildAssocs(fileInfo.getNodeRef());
			List<String> details = new ArrayList<String>();
			Document resultXML;
			try {
				resultXML = xmlBuilder.newDocument();
			} catch (Exception e) {
				throw new WebScriptException(500,
						"Smth really strange happened o.O", e);
			}
			Element rootElement = resultXML.createElement("documents");
			rootElement.setAttribute("page-size", pageSize.toString());
			rootElement.setAttribute("page-number", pageNumber.toString());
			rootElement.setAttribute("query", "");
			resultXML.appendChild(rootElement);
			DateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
			int skip = pageSize * (pageNumber - 1);
			Integer found = 0;
			Integer searchTotal = 0;
			for (ChildAssociationRef assoc : assocs) {
				if ((nodeRef = nodeService.getChildByName(assoc.getChildRef(),
						ContentModel.ASSOC_CONTAINS, "data.xml")) != null) {
					// parse file
					Document dataXML;
					try {
						dataXML = xmlBuilder.parse(fileFolderService.getReader(
								nodeRef).getContentInputStream());
					} catch (Exception e) {
						throw new WebScriptException(500,
								"Form file is malformed.", e);
					}
					totalForms++;
					details.clear();
					xpath.setNamespaceContext(new OrbeonNamespaceContext());
					// execute search queries
					for (int i = 1; i < queries.getLength(); i++) {
						Node query = queries.item(i);
						String path = query.getAttributes()
								.getNamedItem("path").getNodeValue();
						String match = query.getAttributes()
								.getNamedItem("match").getNodeValue();
						String queryString = query.getTextContent();
						if (path == null || match == null
								|| queryString == null)
							throw new WebScriptException(500,
									"Search query XML is malformed.");
						path = path.replace("$fb-lang", "'"+lang+"'");
						boolean exactMatch = "exact".equals(match);
						Node queryResult;
						try {
							queryResult = (Node) xpath.evaluate(path,
									dataXML.getDocumentElement(),
									XPathConstants.NODE);
						} catch (Exception e) {
							throw new WebScriptException(500,
									"Error in query xpath expression.", e);
						}
						if (queryResult == null)
							break;
						String textContent = queryResult.getTextContent();
						// TODO
						// check type while comparing values
						if (exactMatch && queryString.equals(textContent)
								|| !exactMatch && textContent != null
								&& textContent.contains(queryString)
								|| queryString.isEmpty())
							details.add(textContent);
						else
							break;
					}
					// add document to response xml
					if (details.size() == queries.getLength() - 1) {
						searchTotal++;
						if (skip > 0)
							skip--;
						else if (++found <= pageSize) {
							Element item = resultXML.createElement("document");
							String createdText = dateFormat.format( fileFolderService.
									getFileInfo(nodeRef).getCreatedDate() );
							item.setAttribute("created", createdText.substring(0, 26)
											 + ":" + createdText.substring(26));
							String modifiedText = dateFormat.format( fileFolderService.
									getFileInfo(nodeRef).getModifiedDate() );
							item.setAttribute("last-modified", modifiedText.substring(0, 26)
											+ ":" + modifiedText.substring(26));
							item.setAttribute("name", fileFolderService.
									getFileInfo( assoc.getChildRef() ).getName() );
							resultXML.getDocumentElement().appendChild(item);
							Element detailsElement = resultXML
									.createElement("details");
							item.appendChild(detailsElement);
							for (String detail : details) {
								Element detailElement = resultXML
										.createElement("detail");
								detailElement.appendChild(resultXML
										.createTextNode(detail));
								detailsElement.appendChild(detailElement);
							}
						}/*
						 * else break;
						 */

					}
				}
			}
			rootElement.setAttribute("total", totalForms.toString());
			rootElement.setAttribute("search-total", searchTotal.toString());
			// stream output to client
			try {
				TransformerFactory
						.newInstance()
						.newTransformer()
						.transform(new DOMSource(resultXML),
								new StreamResult(res.getOutputStream()));
			} catch (Exception e) {
				throw new WebScriptException(500,
						"Error occured while streaming output to client.", e);
			}
		}

	}
}
