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

package ch.puzzle.itc.mobiliar.test;

import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextTypeEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import java.util.HashSet;

public class GeneratorObjectFactory {

	private ContextEntity globalContext = new ContextEntity();
	private ContextTypeEntity globalContextType = new ContextTypeEntity();
	private ContextTypeEntity environmentContextType = new ContextTypeEntity();

	private ResourceTypeEntity applicationServerType = new ResourceTypeEntity();
	private ResourceTypeEntity applicationType = new ResourceTypeEntity();
	private ResourceTypeEntity nodeType = new ResourceTypeEntity();

	public GeneratorObjectFactory() {
		globalContext.setContextType(globalContextType);
		globalContext.setId(idCounter++);
		globalContextType.setId(idCounter++);
		environmentContextType.setId(idCounter++);
	}

	private static int idCounter = 0;

	public ContextEntity createEnvironment() {
		ContextEntity env = new ContextEntity();
		env.setParent(globalContext);
		env.setId(idCounter++);
		env.setContextType(environmentContextType);
		return env;
	}

	public ContextEntity getGlobalContext() {
		return globalContext;
	}

	public ResourceEntity createApplicationServer() {
		return createByResourceType(applicationServerType);
	}

	public ResourceEntity createApplication() {
		return createByResourceType(applicationType);
	}

	public ResourceEntity createByResourceType(ResourceTypeEntity type) {
		ResourceEntity res =  ResourceFactory.createNewResource();
		res.setId(idCounter++);
		res.setResourceType(type);
		return res;
	}

	public ResourceEntity createNode() {
		return createByResourceType(nodeType);
	}

	public void addGlobalPropertyToResource(PropertyEntity property, ResourceEntity res) {
		addPropertyToResourceInContext(globalContext, property, res);
	}

	public void addPropertyToResourceInContext(ContextEntity ctx, PropertyEntity property, ResourceEntity res) {
		if (res.getContexts() == null) {
			res.setContexts(new HashSet<ResourceContextEntity>());
		}
		getOrCreateResourceContext(res, ctx).addProperty(property);
	}

	public ResourceContextEntity getOrCreateResourceContext(ResourceEntity res, ContextEntity ctx) {
		ResourceContextEntity context = null;
		if (res.getContexts() == null) {
			res.setContexts(new HashSet<ResourceContextEntity>());
		}
		for (ResourceContextEntity tmpcontext : res.getContexts()) {
			if (tmpcontext.getContext().equals(ctx)) {
				context = tmpcontext;
				break;
			}
		}

		if (context == null) {
			context = new ResourceContextEntity();
			context.setId(idCounter++);
			context.setContext(ctx);
			context.setContextualizedObject(res);
			res.getContexts().add(context);
		}
		return context;
	}

	public void addPropertyDescriptorToGlobalResourceContext(PropertyDescriptorEntity pd, ResourceEntity res) {
		getOrCreateResourceContext(res, getGlobalContext()).addPropertyDescriptor(pd);
	}

	public ResourceTypeEntity getApplicationType() {
		return applicationType;
	}

	public ResourceTypeEntity getNodeType() {
		return nodeType;
	}

	public PropertyEntity createAndAddPropertyToResource(PropertyDescriptorEntity p, String value, ContextEntity context,
			ResourceEntity res) {
		PropertyEntity property = new PropertyEntity();
		property.setDescriptor(p);
		property.setValueAndEncrypt(value);
		addPropertyToResourceInContext(context, property, res);
		return property;
	}

	public AbstractContext createTemplateOwner(ResourceEntity resource) {
		ResourceContextEntity resourceContextEntity = new ResourceContextEntity();

		return resourceContextEntity;
	}

	public TemplateDescriptorEntity createTemplate(String name, String content, Integer targetPlatformId) {
		TemplateDescriptorEntity templateDesc = new TemplateDescriptorEntity();
		templateDesc.setFileContent(content);
		templateDesc.setName(name);
		templateDesc.setTesting(false);
		templateDesc.setTargetPlatforms(new HashSet<ResourceGroupEntity>());
		templateDesc.getTargetPlatforms().add(createTargetPlatform(targetPlatformId));

		return templateDesc;

	}

	public ResourceGroupEntity createTargetPlatform(Integer id) {
		ResourceEntity platform = ResourceFactory.createNewResource();
		platform.setId(id);
	    	platform.getResourceGroup().setId(id);
		return platform.getResourceGroup();
	}

	public ResourceContextEntity createResourceContext(ContextEntity context, ResourceEntity resourceEntity) {
		ResourceContextEntity resourceContext = new ResourceContextEntity();
		resourceContext.setContext(context);
		resourceContext.setContextualizedObject(resourceEntity);
		return resourceContext;
	}

	public ResourceTypeContextEntity createResourceTypeContext(ContextEntity context, ResourceTypeEntity typeEntity) {
		ResourceTypeContextEntity resourceContext = new ResourceTypeContextEntity();
		resourceContext.setContext(context);
		resourceContext.setContextualizedObject(typeEntity);
		return resourceContext;
	}

	public ResourceRelationContextEntity createResourceRelationContext(Integer id, ContextEntity context,
			ConsumedResourceRelationEntity resourceRelation) {
		ResourceRelationContextEntity resourceRelationContext = new ResourceRelationContextEntity();
		resourceRelationContext.setId(id);
		resourceRelationContext.setContext(context);
		resourceRelationContext.setContextualizedObject(resourceRelation);

		return resourceRelationContext;

	}
}
