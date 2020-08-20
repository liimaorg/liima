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
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.AppServerRelationsTemplateProcessor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Logic to validate, if a property fulfills the requirements
 */
public class PropertyValidationService {

    public boolean canPropertyValueBeSetOnContext(PropertyDescriptorEntity property, ContextDependency<?> contextDependency){

        if(isHostNameProperty(property) && isDefinedOnNode(contextDependency)){
            return contextDependency.getContext().isEnvironment();
        }
        return true;
    }

    private boolean isDefinedOnNode(ContextDependency<?> contextDependency) {
        Object contextualizedObject = contextDependency.getContextualizedObject();

        if (contextualizedObject instanceof ResourceEntity){
            return ((ResourceEntity) contextualizedObject).getResourceType().isNodeResourceType();
        }

        if (contextualizedObject instanceof ResourceTypeEntity){
            return ((ResourceTypeEntity) contextualizedObject).isNodeResourceType();
        }
        return false;
    }

    private boolean isHostNameProperty(PropertyDescriptorEntity property) {
        return  property.getPropertyName().equals(AppServerRelationsTemplateProcessor.HOST_NAME);
    }


    /**
     * checks whether the technicalKey is valid or not
     * @param technicalKey
     * @return
     */
    public boolean isValidTechnicalKey(String technicalKey){
        return !StringUtils.isEmpty(technicalKey);
    }


    public List<String> getDuplicatePropertyDescriptors(ResourceContextEntity resourceContext, ResourceTypeContextEntity resourceTypeContextEntity, PropertyDescriptorEntity...propertyDescriptors) {
        List<PropertyDescriptorEntity> descriptors = Arrays.asList(propertyDescriptors);
        return duplicatePropertyNames(descriptors, resourceContext, resourceTypeContextEntity);
    }

    /**
     * @return a list of names of the properties which have duplicate names or an empty list if no duplicates
     *         exist
     */
    private List<String> duplicatePropertyNames(List<PropertyDescriptorEntity> property,
              ResourceContextEntity resourceContext, ResourceTypeContextEntity resourceTypeContext) {
        List<String> result = new ArrayList<>();
        List<String> propertyNames = new ArrayList<>();
        List<String> newPropertynames = new ArrayList<>();
        List<Integer> handledIds = new ArrayList<>();
        // First check if there are duplicates in the given property list
        for (PropertyDescriptorEntity descr : property) {
            handledIds.add(descr.getId());
            if (newPropertynames.contains(descr.getPropertyName())) {
                result.add(descr.getPropertyName());
            }
            else {
                newPropertynames.add(descr.getPropertyName());
            }
        }

        // Now we collect the properties on the resource instances... if the properties were already
        // handled, we skip them (e.g. if a property descriptor has already been renamed)
        if (resourceContext != null && resourceContext.getPropertyDescriptors() != null) {
            for (PropertyDescriptorEntity descr : resourceContext.getPropertyDescriptors()) {
                if (!handledIds.contains(descr.getId())) {
                    propertyNames.add(descr.getPropertyName());
                }
            }
        }
        // ...as well as the properties defined on the resource type
        if (resourceTypeContext != null && resourceTypeContext.getPropertyDescriptors() != null) {
            for (PropertyDescriptorEntity descr : resourceTypeContext.getPropertyDescriptors()) {
                if (!handledIds.contains(descr.getId())) {
                    propertyNames.add(descr.getPropertyName());
                }
            }
        }
        for (String newProperty : newPropertynames) {
            if (propertyNames.contains(newProperty)) {
                result.add(newProperty);
            }
        }
        return result;
    }
}
