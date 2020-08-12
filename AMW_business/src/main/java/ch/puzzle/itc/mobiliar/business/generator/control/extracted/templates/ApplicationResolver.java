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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.AppServerRelationProperties;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwAppServerNodeModel;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import com.google.common.collect.Lists;

import java.util.*;

public class ApplicationResolver {

	private ResourceEntity resource;
	private ResourceEntity application;
	private ResourceEntity applicationServer;
	private GenerationOptions options;
    private ResourceDependencyResolverService resourceDependencyResolverService;

	public ApplicationResolver(GenerationOptions options, ResourceEntity slave,ResourceDependencyResolverService dependencyResolverService) {
		this.resource = slave;
		this.options = options;
	    this.resourceDependencyResolverService = dependencyResolverService;
	}

	public boolean resolve() {
	    Set<ProvidedResourceRelationEntity> providedSlaveRelationsForRelease = resourceDependencyResolverService
			    .getProvidedSlaveRelationsForRelease(resource,
					    options.getContext().getTargetRelease());

	    if (providedSlaveRelationsForRelease.size() > 1) {
			String providedSlave = getProvidedSlaveRelationNames();

			throw new RuntimeException(resource + " is provided by multiple other resources:" + providedSlave);
		}
		if (providedSlaveRelationsForRelease.size() == 1) {
			resolveApplication();
			resolveApplicationServer();
			if (application == null) {
				throw new RuntimeException("application missing");
			}
			return true;
		}
		return false;
	}

	private String getProvidedSlaveRelationNames() {
		StringBuffer sb = new StringBuffer();
		for (ProvidedResourceRelationEntity relation : resource.getProvidedSlaveRelations()) {
			sb.append(relation);
		}
		return sb.toString();
	}

	public ResourceEntity getApplication() {
		return application;
	}

	public ResourceEntity getApplicationServer() {
		return applicationServer;
	}


	private void resolveApplication() {
		application = findResourceEntityForRelease(resource, resource.getMasterResourcesOfProvidedSlaveRelations());
	}

	private ResourceEntity findResourceEntityForRelease(ResourceEntity resource, Collection<ResourceEntity> resources) {
		Set<ResourceGroupEntity> result = new HashSet<ResourceGroupEntity>();
		for (ResourceEntity r : resources) {
			result.add(r.getResourceGroup());
		}
		if (result.size() > 1) {
			// TODO RuntimeException?
			throw new RuntimeException(resource + " is provided by multiple other resources");
		}
		return options.getContext().getResourceEntityForRelease(result.iterator().next());
	}

	private void resolveApplicationServer() {
		List<ResourceEntity> applicationServers = application
				.getMasterResourcesOfConsumedSlaveRelationByResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER);
		applicationServer = findResourceEntityForRelease(application, applicationServers);
	}

	public void transform(AMWTemplateExceptionHandler templateExceptionHandler, AmwAppServerNodeModel model) {
		AppServerRelationProperties app = new AppServerRelationProperties(options.getContext().getContext(), getApplication(), templateExceptionHandler);
		AppServerRelationProperties appServer = new AppServerRelationProperties(options.getContext().getContext(), getApplicationServer(), templateExceptionHandler);

		model.setAppProperties(app.getProperties());
		model.setAppServerProperties(appServer.getProperties());

		List<Map<String, FreeMarkerProperty>> nodePropertyList = Lists.newArrayList();

		model.setNodePropertyList(nodePropertyList);
	}
}
