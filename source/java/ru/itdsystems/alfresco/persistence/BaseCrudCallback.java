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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Abstract class for scripts callback.
 * 
 * @author Alexey Ermakov
 * 
 */

abstract public class BaseCrudCallback {
	
	protected ServiceRegistry serviceRegistry;
	
	public BaseCrudCallback() {
	}
	
	public void setServiceRegistry(ServiceRegistry registry) {
		serviceRegistry = registry;	
	}
	
	public abstract void afterPropertiesSet() throws Exception;
	
	public void doBefore(NodeRef rootNodeRef, List<String> pathElements, NodeRef nodeRef) throws Exception {
		// empty callback
	}
	
	public void doAfter(NodeRef rootNodeRef, List<String> pathElements, NodeRef nodeRef) throws Exception {
		// empty callback		
	}
}
