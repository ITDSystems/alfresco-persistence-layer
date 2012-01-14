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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Webscript (Persistence API Delete) implementation to delete file from a
 * repository.
 * 
 * @author Alexey Ermakov
 * 
 */

public class CrudDelete extends AbstractCrudWebScript {

	private NodeService nodeService;

	// for spring injection
	@Override
	public void setServiceRegistry(ServiceRegistry registry) {
		super.setServiceRegistry(registry);
		nodeService = registry.getNodeService();
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws WebScriptException {
		// construct path elements array from request parameters
		List<String> pathElements = new ArrayList<String>();
		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
		pathElements.add(templateVars.get("application_name"));
		pathElements.add(templateVars.get("form_name"));
		String val;
		if ((val = templateVars.get("form_data_id")) != null) {
			pathElements.add("data");
			pathElements.add(val);
		} else {
			pathElements.add("form");
		}
		if ((val = templateVars.get("file_name")) != null)
			pathElements.add(val);
		// get current user's home folder
		NodeRef nodeRef = getRootNodeRef();
		try {
			// resolve path to file and delete
			FileInfo fileInfo = fileFolderService.resolveNamePath(nodeRef, pathElements);
			doBefore(pathElements, fileInfo.getNodeRef());
			nodeService.deleteNode(fileInfo.getNodeRef());
			doAfter(pathElements, null);
		} catch (FileNotFoundException e) {
			throw new WebScriptException(404, "Error occured while processing request", e);
		}
	}
}