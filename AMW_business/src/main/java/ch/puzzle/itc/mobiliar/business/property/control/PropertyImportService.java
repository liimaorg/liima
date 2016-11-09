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

package ch.puzzle.itc.mobiliar.business.property.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.GlobalContext;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyDescriptorService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyValueService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceReleaseComparator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PropertyImportService {

    @Inject
    ResourceReleaseComparator resourceReleaseComparator;

    @Inject
    private Logger log;

    @Inject
    private PropertyValueService propertyValueService;

    @Inject
    @GlobalContext
    private ContextEntity globalContext;

    @Inject
    private PropertyDescriptorService descriptorService;

    @Inject
    private PropertyTagEditingService propertyTagService;


    public void deleteAllPropertiesAndValuesInAllContextForResource(ResourceEntity resource, List<PropertyDescriptorEntity> propertyDescriptorsToDelete) {
        Set<ResourceContextEntity> allContexts = resource.getNullSaveContexts();
        for (ResourceContextEntity context : allContexts) {
            deleteAllPropertiesAndValuesInAllContextForResource(context, propertyDescriptorsToDelete);
        }

        for (PropertyDescriptorEntity propertyDescriptor : propertyDescriptorsToDelete) {
            removeProperty(propertyDescriptor, resource);
        }
    }

    public void deleteAllPropertiesAndValuesInAllContextForRelation(ResourceEntity resource, AbstractResourceRelationEntity relation, List<PropertyDescriptorEntity> propertyDescriptorsToDelete) {
        Set<ResourceRelationContextEntity> allContexts = relation.getNullSaveContexts();
        for (ResourceRelationContextEntity context : allContexts) {
            deleteAllPropertiesAndValuesInAllContextForResource(context, propertyDescriptorsToDelete);
        }

        for (PropertyDescriptorEntity propertyDescriptor : propertyDescriptorsToDelete) {
            removeProperty(propertyDescriptor, resource);
        }
    }

    private void deleteAllPropertiesAndValuesInAllContextForResource(ContextDependency context, List<PropertyDescriptorEntity> properties) {
        for (PropertyDescriptorEntity propertyDescriptor : properties) {
            try {
                log.info("delete propertyvalue for propertydescriptor " + propertyDescriptor + "  for context " + context);
                propertyValueService.resetPropertyValue(context, propertyDescriptor.getId());

                for (PropertyEntity property : new ArrayList<>(propertyDescriptor.getProperties())) {
                    log.info("delete propertyvalue " + property + " for propertydescriptor " + propertyDescriptor);
                    propertyDescriptor.removeProperty(property);
                }
            } catch (NoResultException e) {
                log.info("Propertydescriptor " + propertyDescriptor + " in context " + context + " has no propertyvalues");
            }
        }
    }

    public void removeProperty(PropertyDescriptorEntity propertyDescriptor, ResourceEntity resourceEntity) {
        ResourceContextEntity resourceContext = resourceEntity.getOrCreateContext(globalContext);
        try {
            descriptorService.deletePropertyDescriptorByOwner(propertyDescriptor, resourceContext);
        } catch (AMWException e) {
            throw new AMWRuntimeException("Failed to delete property " + propertyDescriptor.getPropertyDescriptorDisplayName() + " which has still assigned values");
        }
    }

    public void savePropertyDescriptorWithTags(PropertyDescriptorEntity propDesc, List<String> tags, ResourceEntity resourceEntity, ForeignableOwner owner) throws AMWException {
        ResourceContextEntity resourceContext = resourceEntity.getOrCreateContext(globalContext);
        List<PropertyTagEntity> propertyTags = createPropertyTags(tags);
        descriptorService.savePropertyDescriptorForOwner(owner, resourceContext, propDesc, propertyTags);
    }

    private List<PropertyTagEntity> createPropertyTags(List<String> tags) {
        List<PropertyTagEntity> propertyTagEntities = new LinkedList<>();
        for (String tag : tags) {
            propertyTagEntities.add(propertyTagService.addPropertyTag((propertyTagService.createPropertyTagEntity(tag.trim()))));
        }
        return propertyTagEntities;
    }
}
