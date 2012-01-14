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

import java.io.InputStream;
import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;

/**
 *  Abstract content type detector implementation.
 *  @author Alexey Ermakov
 */
public abstract class AbstractContentTypeDetector {

	protected Repository repository;
	protected ServiceRegistry serviceRegistry;

	// method to initialize class
	public void initialize(Repository repository,
			ServiceRegistry registry) {
		this.repository = repository;
		this.serviceRegistry = registry;
	}
	
	// returns QName of type that should be used
	public abstract QName detectMimetype(List<String> pathElements, String fileName, InputStream contents);

}
