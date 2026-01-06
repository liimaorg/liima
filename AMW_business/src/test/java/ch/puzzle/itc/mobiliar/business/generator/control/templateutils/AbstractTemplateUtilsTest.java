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

package ch.puzzle.itc.mobiliar.business.generator.control.templateutils;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;

import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextTypeEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public abstract class AbstractTemplateUtilsTest {
	
	ResourceEntity application;
	ResourceEntity database;
	ResourceTypeEntity applicationType;
	ResourceTypeEntity databaseType;
	ConsumedResourceRelationEntity application2database;	
	ResourceRelationTypeEntity applicationType2databaseType;
	ContextTypeEntity globalContextType;
	ContextTypeEntity envContextType;
	ContextEntity globalContext;
	ContextEntity devContext;
	ResourceContextEntity applicationInGlobal;
	ResourceContextEntity applicationInDev;
	ResourceContextEntity databaseInGlobal;
	ResourceContextEntity databaseInDev;
	ResourceTypeContextEntity applicationTypeInGlobal;
	ResourceTypeContextEntity applicationTypeInDev;
	ResourceTypeContextEntity databaseTypeInGlobal;
	ResourceTypeContextEntity databaseTypeInDev;
	ResourceRelationTypeContextEntity applicationType2databaseTypeInGlobal;
	ResourceRelationTypeContextEntity applicationType2databaseTypeInDev;
	ResourceRelationContextEntity application2databaseInGlobal;
	ResourceRelationContextEntity application2databaseInDev;	
	TemplateDescriptorEntity applicationTypeTemplate;
	TemplateDescriptorEntity applicationTemplate;
	TemplateDescriptorEntity databaseTypeTemplate;
	TemplateDescriptorEntity databaseTemplate;
	TemplateDescriptorEntity applicationType2databaseTypeTemplate;
	TemplateDescriptorEntity application2databaseTemplate;
	PropertyDescriptorEntity applicationTypeProperty;
	PropertyDescriptorEntity applicationProperty;
	PropertyDescriptorEntity databaseTypeProperty;
	PropertyDescriptorEntity databaseProperty;
	PropertyDescriptorEntity applicationType2databaseTypeProperty;
	PropertyDescriptorEntity application2databaseProperty;
	PropertyEntity applicationTypePropertyValueGlobal;
	PropertyEntity applicationTypePropertyValueDev;
	PropertyEntity applicationPropertyValueGlobal;
	PropertyEntity applicationPropertyValueDev;
	PropertyEntity databaseTypePropertyValueGlobal;
	PropertyEntity databaseTypePropertyValueDev;
	PropertyEntity databaseTypePropertyValueOverriddenInGlobal;
	PropertyEntity databaseTypePropertyValueOverriddenInDev;
	PropertyEntity databasePropertyValueGlobal;
	PropertyEntity databasePropertyValueDev;
	PropertyEntity databasePropertyValueOverriddenInGlobal;
	PropertyEntity databasePropertyValueOverriddenInDev;
	PropertyEntity applicationType2databaseTypePropertyValueGlobal;
	PropertyEntity applicationType2databaseTypePropertyValueDev;
	PropertyEntity application2databasePropertyValueGlobal;
	PropertyEntity application2databasePropertyValueDev;
	
	private int idCounter = 0;
	
	
	@BeforeEach
	public void setUp() {
		
		applicationType = new ResourceTypeEntity();
		applicationType.setId(idCounter++);
		applicationType.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		
		databaseType = new ResourceTypeEntity();
		databaseType.setId(idCounter++);
		databaseType.setName("database");
				
		application =  ResourceFactory.createNewResource("application");
		application.setId(idCounter++);
		application.setResourceType(applicationType);
		
		database =  ResourceFactory.createNewResource("database");
		database.setId(idCounter++);
		database.setResourceType(databaseType);
		
		applicationTypeTemplate = new TemplateDescriptorEntity();
		applicationTypeTemplate.setId(idCounter++);
		applicationTypeTemplate.setName("applicationTypeTemplate");
			
		
		applicationTemplate = new TemplateDescriptorEntity();
		applicationTemplate.setId(idCounter++);
		applicationTemplate.setName("applicationTemplate");

				
		databaseTypeTemplate = new TemplateDescriptorEntity();
		databaseTypeTemplate.setId(idCounter++);
		databaseTypeTemplate.setName("databaseTypeTemplate");

				
		databaseTemplate = new TemplateDescriptorEntity();
		databaseTemplate.setId(idCounter++);
		databaseTemplate.setName("databaseTemplate");

		
		application2databaseTemplate = new TemplateDescriptorEntity();
		application2databaseTemplate.setId(idCounter++);
		application2databaseTemplate.setName("application2databaseTemplate");

		
		applicationType2databaseTypeTemplate = new TemplateDescriptorEntity();
		applicationType2databaseTypeTemplate.setId(idCounter++);
		applicationType2databaseTypeTemplate.setName("applicationType2databaseTypeTemplate");
			
		
		applicationType2databaseType = new ResourceRelationTypeEntity();
		applicationType2databaseType.setId(idCounter++);
		applicationType2databaseType.setResourceTypes(applicationType, databaseType);
		
				
		application2database = new ConsumedResourceRelationEntity();
		application2database.setId(idCounter++);
		application2database.setMasterResource(application);
		application2database.setSlaveResource(database);
		application2database.setResourceRelationType(applicationType2databaseType);		
		application.addConsumedRelation(application2database);
		database.setConsumedSlaveRelations(new HashSet<ConsumedResourceRelationEntity>());
		database.getConsumedSlaveRelations().add(application2database);
		
		globalContextType = new ContextTypeEntity();
		globalContextType.setId(idCounter++);
		globalContextType.setName("globalContextType");
		
		globalContext = new ContextEntity();
		globalContext.setId(idCounter++);
		globalContext.setName("Global");
		globalContext.setContextType(globalContextType);
		
		envContextType = new ContextTypeEntity();
		envContextType.setId(idCounter++);
		envContextType.setName("Environment");
		
		devContext = new ContextEntity();
		devContext.setId(idCounter++);
		devContext.setName("Dev");
		devContext.setParent(globalContext);
		devContext.setContextType(envContextType);
		
		applicationInGlobal = new ResourceContextEntity();
		applicationInGlobal.setId(idCounter++);
		applicationInGlobal.setContext(globalContext);
		applicationInGlobal.setContextualizedObject(application);
		applicationInGlobal.addTemplate(applicationTemplate);
		application.addContext(applicationInGlobal);
		
		applicationProperty = new PropertyDescriptorEntity();
		applicationProperty.setId(idCounter++);
		applicationProperty.setPropertyName("applicationProperty");
		applicationInGlobal.addPropertyDescriptor(applicationProperty);
				
		applicationPropertyValueGlobal = new PropertyEntity();
		applicationPropertyValueGlobal.setDescriptor(applicationProperty);
		applicationPropertyValueGlobal.setValueAndEncrypt("applicationPropertyValueGlobal");
		applicationInGlobal.addProperty(applicationPropertyValueGlobal);
		
		applicationInDev = new ResourceContextEntity();
		applicationInDev.setId(idCounter++);
		applicationInDev.setContext(devContext);
		applicationInDev.setContextualizedObject(application);
		application.addContext(applicationInDev);
		
		applicationPropertyValueDev = new PropertyEntity();
		applicationPropertyValueDev.setDescriptor(applicationProperty);
		applicationPropertyValueDev.setValue("applicationPropertyValueDev");
		applicationInDev.addProperty(applicationPropertyValueDev);
		
		databaseInGlobal = new ResourceContextEntity();
		databaseInGlobal.setId(idCounter++);
		databaseInGlobal.setContext(globalContext);
		databaseInGlobal.setContextualizedObject(database);
		databaseInGlobal.addTemplate(databaseTemplate);
		database.addContext(databaseInGlobal);
		
		databaseProperty = new PropertyDescriptorEntity();
		databaseProperty.setId(idCounter++);
		databaseProperty.setPropertyName("databaseProperty");
		databaseInGlobal.addPropertyDescriptor(databaseProperty);
		
		databasePropertyValueGlobal = new PropertyEntity();
		databasePropertyValueGlobal.setDescriptor(databaseProperty);
		databasePropertyValueGlobal.setValue("databasePropertyValueGlobal");
		databaseInGlobal.addProperty(databasePropertyValueGlobal);
		
		databaseInDev = new ResourceContextEntity();
		databaseInDev.setId(idCounter++);
		databaseInDev.setContext(devContext);
		databaseInDev.setContextualizedObject(database);
		database.addContext(databaseInDev);
		
		databasePropertyValueDev = new PropertyEntity();
		databasePropertyValueDev.setDescriptor(databaseProperty);
		databasePropertyValueDev.setValue("databasePropertyValueDev");
		databaseInDev.addProperty(databasePropertyValueDev);		
		
		applicationTypeInGlobal = new ResourceTypeContextEntity();
		applicationTypeInGlobal.setId(idCounter++);
		applicationTypeInGlobal.setContext(globalContext);
		applicationTypeInGlobal.setContextualizedObject(applicationType);
		applicationTypeInGlobal.addTemplate(applicationTypeTemplate);
		applicationType.addContext(applicationTypeInGlobal);
		
		
		applicationTypeProperty = new PropertyDescriptorEntity();
		applicationTypeProperty.setId(idCounter++);
		applicationTypeProperty.setPropertyName("applicationTypeProperty");
		applicationTypeInGlobal.addPropertyDescriptor(applicationTypeProperty);
		
		applicationTypePropertyValueGlobal = new PropertyEntity();
		applicationTypePropertyValueGlobal.setDescriptor(applicationTypeProperty);
		applicationTypePropertyValueGlobal.setValue("applicationTypePropertyValueGlobal");
		applicationTypeInGlobal.addProperty(applicationTypePropertyValueGlobal);
		
		applicationTypeInDev = new ResourceTypeContextEntity();
		applicationTypeInDev.setId(idCounter++);
		applicationTypeInDev.setContext(devContext);
		applicationTypeInDev.setContextualizedObject(applicationType);
		applicationType.addContext(applicationTypeInDev);
		
		applicationTypePropertyValueDev = new PropertyEntity();
		applicationTypePropertyValueDev.setDescriptor(applicationTypeProperty);
		applicationTypePropertyValueDev.setValue("applicationTypePropertyValueDev");
		applicationTypeInDev.addProperty(applicationTypePropertyValueDev);
		
		databaseTypeInGlobal = new ResourceTypeContextEntity();
		databaseTypeInGlobal.setId(idCounter++);
		databaseTypeInGlobal.setContext(globalContext);
		databaseTypeInGlobal.setContextualizedObject(databaseType);
		databaseTypeInGlobal.addTemplate(databaseTypeTemplate);
		databaseType.addContext(databaseTypeInGlobal);
		
		databaseTypeProperty = new PropertyDescriptorEntity();
		databaseTypeProperty.setId(idCounter++);
		databaseTypeProperty.setPropertyName("databaseTypeProperty");
		databaseTypeInGlobal.addPropertyDescriptor(databaseTypeProperty);
		
		databaseTypePropertyValueGlobal = new PropertyEntity();
		databaseTypePropertyValueGlobal.setDescriptor(databaseTypeProperty);
		databaseTypePropertyValueGlobal.setValue("databaseTypePropertyValueGlobal");
		databaseTypeInGlobal.addProperty(databaseTypePropertyValueGlobal);
		
		databaseTypeInDev = new ResourceTypeContextEntity();
		databaseTypeInDev.setId(idCounter++);
		databaseTypeInDev.setContext(devContext);
		databaseTypeInDev.setContextualizedObject(databaseType);
		databaseType.addContext(databaseTypeInDev);
		
		databaseTypePropertyValueDev = new PropertyEntity();
		databaseTypePropertyValueDev.setDescriptor(databaseTypeProperty);
		databaseTypePropertyValueDev.setValue("databaseTypePropertyValueDev");
		databaseTypeInDev.addProperty(databaseTypePropertyValueDev);
		
		applicationType2databaseTypeInGlobal = new ResourceRelationTypeContextEntity();
		applicationType2databaseTypeInGlobal.setId(idCounter++);
		applicationType2databaseTypeInGlobal.setContext(globalContext);
		applicationType2databaseTypeInGlobal.setContextualizedObject(applicationType2databaseType);
		applicationType2databaseTypeInGlobal.addTemplate(applicationType2databaseTypeTemplate);
		applicationType2databaseType.addContext(applicationType2databaseTypeInGlobal);
		applicationType.setResourceRelationTypesA(new HashSet<ResourceRelationTypeEntity>(Arrays.asList(applicationType2databaseType)));
		
		applicationType2databaseTypeProperty = new PropertyDescriptorEntity();
		applicationType2databaseTypeProperty.setId(idCounter++);
		applicationType2databaseTypeProperty.setPropertyName("applicationType2databaseTypeProperty");
		applicationType2databaseTypeInGlobal.addPropertyDescriptor(applicationType2databaseTypeProperty);
		
		
		applicationType2databaseTypePropertyValueGlobal = new PropertyEntity();
		applicationType2databaseTypePropertyValueGlobal.setDescriptor(applicationType2databaseTypeProperty);
		applicationType2databaseTypePropertyValueGlobal.setValue("applicationType2databaseTypePropertyValueGlobal");
		applicationType2databaseTypeInGlobal.addProperty(applicationType2databaseTypePropertyValueGlobal);
		
		databaseTypePropertyValueOverriddenInGlobal = new PropertyEntity();
		databaseTypePropertyValueOverriddenInGlobal.setDescriptor(databaseTypeProperty);
		databaseTypePropertyValueOverriddenInGlobal.setValue("databaseTypePropertyValueOverriddenInGlobal");
		applicationType2databaseTypeInGlobal.addProperty(databaseTypePropertyValueOverriddenInGlobal);		
				
		applicationType2databaseTypeInDev = new ResourceRelationTypeContextEntity();
		applicationType2databaseTypeInDev.setId(idCounter++);
		applicationType2databaseTypeInDev.setContext(devContext);
		applicationType2databaseTypeInDev.setContextualizedObject(applicationType2databaseType);
		applicationType2databaseType.addContext(applicationType2databaseTypeInDev);
		
		databaseTypePropertyValueOverriddenInDev = new PropertyEntity();
		databaseTypePropertyValueOverriddenInDev.setDescriptor(databaseTypeProperty);
		databaseTypePropertyValueOverriddenInDev.setValue("databaseTypePropertyValueOverriddenInDev");
		applicationType2databaseTypeInDev.addProperty(databaseTypePropertyValueOverriddenInDev);	
		
		applicationType2databaseTypePropertyValueDev = new PropertyEntity();
		applicationType2databaseTypePropertyValueDev.setDescriptor(applicationType2databaseTypeProperty);
		applicationType2databaseTypePropertyValueDev.setValue("applicationType2databaseTypePropertyValueDev");
		applicationType2databaseTypeInDev.addProperty(applicationType2databaseTypePropertyValueDev);
		
		application2databaseInGlobal = new ResourceRelationContextEntity();
		application2databaseInGlobal.setId(idCounter++);
		application2databaseInGlobal.setContext(globalContext);
		application2databaseInGlobal.setContextualizedObject(application2database);
		application2databaseInGlobal.addTemplate(application2databaseTemplate);
		application2database.addContext(application2databaseInGlobal);
		
		application2databaseProperty = new PropertyDescriptorEntity();
		application2databaseProperty.setId(idCounter++);
		application2databaseProperty.setPropertyName("application2databaseProperty");
		application2databaseInGlobal.addPropertyDescriptor(application2databaseProperty);
		
		databasePropertyValueOverriddenInGlobal = new PropertyEntity();
		databasePropertyValueOverriddenInGlobal.setDescriptor(databaseProperty);
		databasePropertyValueOverriddenInGlobal.setValue("databasePropertyValueOverriddenInGlobal");
		application2databaseInGlobal.addProperty(databasePropertyValueOverriddenInGlobal);	
		
		application2databasePropertyValueGlobal = new PropertyEntity();
		application2databasePropertyValueGlobal.setDescriptor(application2databaseProperty);
		application2databasePropertyValueGlobal.setValue("application2databasePropertyValueGlobal");
		application2databaseInGlobal.addProperty(application2databasePropertyValueGlobal);
		
		
		application2databaseInDev = new ResourceRelationContextEntity();
		application2databaseInDev.setId(idCounter++);
		application2databaseInDev.setContext(devContext);
		application2databaseInDev.setContextualizedObject(application2database);
		application2database.addContext(application2databaseInDev);
		
		application2databasePropertyValueDev = new PropertyEntity();
		application2databasePropertyValueDev.setDescriptor(application2databaseProperty);
		application2databasePropertyValueDev.setValue("application2databasePropertyValueDev");
		application2databaseInDev.addProperty(application2databasePropertyValueDev);
		
		databasePropertyValueOverriddenInDev = new PropertyEntity();
		databasePropertyValueOverriddenInDev.setDescriptor(databaseProperty);
		databasePropertyValueOverriddenInDev.setValue("databasePropertyValueOverriddenInDev");
		application2databaseInDev.addProperty(databasePropertyValueOverriddenInDev);	
	}

}
