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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Webscript (Persistence API Put) implementation to put or update new file in a
 * repository.
 * 
 * @author Alexey Ermakov
 * 
 */

public class CrudPut extends AbstractCrudWebScript {

	private NodeService nodeService;
	private CheckOutCheckInService checkOutCheckInService;

	private String mimetypeDetectorClassName = null;
	private AbstractContentTypeDetector contentDetector;
	private Boolean versionable = false;

	// for spring injection
	public void setMimetypeDetectorClassName(String className) {
		mimetypeDetectorClassName = className;
	}

	public void setVersionable(String versionable) {
		this.versionable = "true".equals(versionable);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		nodeService = serviceRegistry.getNodeService();
		checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();
		// create mimetype detector
		Class<?> mimetypeDetectorClass = "".equals(mimetypeDetectorClassName) ? SimpleContentTypeDetector.class
				: Class.forName(mimetypeDetectorClassName);
		contentDetector = (AbstractContentTypeDetector) mimetypeDetectorClass
				.newInstance();
		contentDetector.initialize(repository, serviceRegistry);

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
		if (templateVars.get("form_data_id") != null) {
			pathElements.add("data");
			pathElements.add(templateVars.get("form_data_id"));
		} else {
			pathElements.add("form");
		}
		String fileName = templateVars.get("file_name");
		pathElements.add(fileName);
		doBefore(pathElements, null);
		pathElements.remove(pathElements.size()-1);
		// get current user's home folder
		NodeRef currentNodeRef = getRootNodeRef();
		// resolve path to file or create file if doesn't exist yet
		NodeRef nodeRef = null;
		for (String pathElement : pathElements) {
			nodeRef = nodeService.getChildByName(currentNodeRef,
					ContentModel.ASSOC_CONTAINS, pathElement);
			// create node if it doesn't exist
			if (nodeRef == null) {
				currentNodeRef = fileFolderService.create(currentNodeRef,
						pathElement, ContentModel.TYPE_FOLDER).getNodeRef();
			} else
				currentNodeRef = nodeRef;
		}
		nodeRef = nodeService.getChildByName(currentNodeRef,
				ContentModel.ASSOC_CONTAINS, fileName);
		if (nodeRef == null) {
			QName contentType = contentDetector.detectMimetype(pathElements, fileName, req
							.getContent().getInputStream());
			nodeRef = fileFolderService.create(currentNodeRef, fileName,
					contentType).getNodeRef();
			if (versionable) {
				// add versionable aspect
				nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE,
						null);
			}
		}
		if (versionable) {
			// create working copy
			nodeRef = checkOutCheckInService.checkout(nodeRef);
		}
		// save content and mimetype
		ContentWriter contentWriter = fileFolderService.getWriter(nodeRef);
		contentWriter.putContent(req.getContent().getInputStream());
		contentWriter.setMimetype(req.getContentType());
		if (versionable) {
			// merge working copy
			Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
			versionProperties.put(Version.PROP_DESCRIPTION,
					"Modified by Orbeon");
			versionProperties.put(VersionModel.PROP_VERSION_TYPE,
					VersionType.MINOR);
			nodeRef = checkOutCheckInService.checkin(nodeRef, versionProperties);
		}
		doAfter(pathElements, nodeRef);
	}
}
