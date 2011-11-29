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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Webscript (Persistence API Post) implementation to perform a search inside
 * repository.
 * 
 * @author Alexey Ermakov
 * 
 */

public class CrudPost extends AbstractCrudWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws WebScriptException {
		throw new WebScriptException(500, "Not implemented yet.");
		/* construct path elements array from request parameters
		List<String> pathElements = new ArrayList<String>();
		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();		
		pathElements.add(templateVars.get("application_name"));
		pathElements.add(templateVars.get("form_name"));
		if (callback != null)
				callback.doBefore(pathElements, null);
		// parse xml from request and perform a search
		Document searchXML;
		try {
			searchXML = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(req.getContent().getInputStream());
		} catch (Exception e) {
			throw new WebScriptException(500,
					"Error occured while parsing XML from request.", e);
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		Integer pageSize;
		Integer pageNumber;
		String applicationName;
		String formName;		
		try {			
			pageSize = new Integer(
					((Node) xpath.evaluate("//search/page-size/text()",
							searchXML, XPathConstants.NODE)).getNodeValue());			
			pageNumber = new Integer(((Node) xpath.evaluate(
					"//search/page-number/text()", searchXML,
					XPathConstants.NODE)).getNodeValue());			
			applicationName = ((Node) xpath.evaluate(
					"//search/app/text()", searchXML, XPathConstants.NODE))
					.getNodeValue();
			formName = ((Node) xpath.evaluate("//search/form/text()",
					searchXML, XPathConstants.NODE)).getNodeValue();
		} catch (Exception e) {
			throw new WebScriptException(500, "XML in request is malformed.", e);
		}*/
	}
}
