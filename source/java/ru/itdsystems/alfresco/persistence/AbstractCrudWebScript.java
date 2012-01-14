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

import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Abstract persistence webscript implementation
 * @author Alexey Ermakov
 * 
 */

public abstract class AbstractCrudWebScript extends AbstractWebScript
		implements InitializingBean {

	protected FileFolderService fileFolderService;
	protected Repository repository;
	protected AuthenticationService authenticationService;
	protected PersonService personService;
	protected ServiceRegistry serviceRegistry;
	protected BaseCrudCallback callback;

	private String callbackClassName = null;

	// for spring injection
	public void setServiceRegistry(ServiceRegistry registry) {
		serviceRegistry = registry;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setCallbackClassName(String callbackClassName) {
		this.callbackClassName = callbackClassName;
	}

	public void afterPropertiesSet() throws Exception{
		fileFolderService = serviceRegistry.getFileFolderService();
		authenticationService = serviceRegistry.getAuthenticationService();
		personService = serviceRegistry.getPersonService();
		if ("".equals(callbackClassName))
			return;
		try {
			Class<?> callbackClass = Class.forName(callbackClassName);
			callback = (BaseCrudCallback) callbackClass.newInstance();
			callback.setServiceRegistry(serviceRegistry);
			callback.afterPropertiesSet();
		} catch (ClassNotFoundException e) {
			throw new Exception("Can't load callback class: "+callbackClassName, e);
		}
	}
	
	public abstract void execute(WebScriptRequest req,
			WebScriptResponse res) throws WebScriptException;

	// returns noderef of user's home
	public NodeRef getRootNodeRef() {
		String currentUserName = authenticationService.getCurrentUserName();
		NodeRef currentUser = personService.getPerson(currentUserName);
		return repository.getUserHome(currentUser);
	}
	
	// wrappers for callbacks
	public void doBefore(List<String> pathElements, NodeRef nodeRef) throws WebScriptException{
		if (callback != null)
			try {
				callback.doBefore(getRootNodeRef(), pathElements, nodeRef);
			} catch (Exception e) {
				// TODO
				// decide what should we do if callback failed
				throw new WebScriptException(500, "Error occured during callback execution.", e);
			}
	}
	
	public void doAfter(List<String> pathElements, NodeRef nodeRef) throws WebScriptException {
		if (callback != null)
			try {
				callback.doAfter(getRootNodeRef(), pathElements, nodeRef);
			} catch (Exception e) {
				// TODO
				// decide what should we do if callback failed
				throw new WebScriptException(500, "Error occured during callback execution.", e);
			}		
	}


}
