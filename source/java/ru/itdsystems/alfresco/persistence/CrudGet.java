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

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Webscript (Persistence API Get) implementation to return file from a
 * repository.
 * 
 * @author Alexey Ermakov
 * 
 */

public class CrudGet extends AbstractCrudWebScript {

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
		pathElements.add(templateVars.get("file_name"));
		// get current user's home folder
		NodeRef nodeRef = getRootNodeRef();
		// resolve path to file
		FileInfo fileInfo = null;
		Boolean emptyResponse = false;
		try {
			fileInfo = fileFolderService.resolveNamePath(nodeRef, pathElements);
			doBefore(pathElements, fileInfo.getNodeRef());
		} catch (FileNotFoundException e) {
			// return empty response
			emptyResponse = true;
			res.setStatus(404);
		}
		// stream back
		if (!emptyResponse) {
			ContentReader fileReader = fileFolderService.getReader(fileInfo
					.getNodeRef());
			try {
				fileReader.getContent(res.getOutputStream());
				// set response content type
				res.setContentType(fileReader.getMimetype());
			} catch (Exception e) {
				throw new WebScriptException(500,
						"Error occured while processing request", e);
			}
		}
	}
}
