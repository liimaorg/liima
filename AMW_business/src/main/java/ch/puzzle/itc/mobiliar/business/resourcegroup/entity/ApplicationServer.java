/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import java.util.ArrayList;
import java.util.List;


import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public class ApplicationServer extends Resource {

	private List<Application> applications;


	public ApplicationServer(ResourceTypeProvider resourceTypeProvider, ContextEntity globalContext) {
		super(resourceTypeProvider, DefaultResourceTypeDefinition.APPLICATIONSERVER, globalContext);
	}

	public List<Application> getAMWApplications() {
		List<Application> applications = new ArrayList<Application>();
		List<ResourceEntity> list = getEntity().getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION);
		for (ResourceEntity r : list) {
			applications.add(Application.createByResource(r, resourceTypeProvider, null));
		}
		return applications;
	}

	public List<Application> getApplications() {
		if (applications == null) {
			applications = new ArrayList<Application>();
			List<ResourceEntity> list = getEntity().getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION);
			for (ResourceEntity r : list) {
				applications.add(Application.createByResource(r, resourceTypeProvider, null));
			}
		}
		return applications;
	}

	public void addApplication(Application application) throws ElementAlreadyExistsException {
		addApplication(application.getEntity());
	}

	public void addApplication(ResourceEntity application) throws ElementAlreadyExistsException {
		for(Application app : getApplications()){
			if(app.getId().equals(application.getId())){
				String msg = "Die Applikation " + application.getName() + " ist bereits im Server " + getName() + " enthalten";
				throw new ElementAlreadyExistsException(msg, Application.class, application.getName());
			}
		}
		ResourceRelationTypeEntity relationType = resourceTypeProvider.getOrCreateResourceRelationType(this
.getEntity().getResourceType(), application.getResourceType(), null);
		getEntity().addConsumedResourceRelation(application, relationType, null);
	}

	public static ApplicationServer createByResource(ResourceEntity r, ResourceTypeProvider resourceTypeProvider, ContextEntity globalContext) {
		ApplicationServer appServer = new ApplicationServer(resourceTypeProvider, globalContext);
		if (r == null){
            // app server created by system
			r = ResourceFactory.createNewResource();
		}
		appServer.wrap(r);
		return appServer;
	}

	public static ApplicationServer createByName(String name, ResourceTypeProvider resourceTypeProvider, ContextEntity globalContext) {
		ApplicationServer appServer = createByResource(null, resourceTypeProvider, globalContext);
		appServer.setName(name);
		return appServer;
	}
}
