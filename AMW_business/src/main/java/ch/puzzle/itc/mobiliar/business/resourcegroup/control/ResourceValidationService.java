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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.database.control.QueryUtils;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;

import javax.inject.Inject;
import java.util.List;

/**
 * Validation logic for resources, resource groups and resource types
 */
public class ResourceValidationService {

    @Inject
    ResourcesScreenQueries queries;

    /**
     * @param resourceName - the resource name to be validated
     * @param id - the id of the resource
     * @throws AMWException - if the name is invalid, an AMW exception is thrown
     */
    public void validateResourceName(String resourceName, Integer id) throws AMWException {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            throw new AMWException("The resource name must not be empty!");
        }
        if (id == null) {
            throw new AMWException("The resource id must not be empty!");
        }
        // Check if a resource with the same name already exists...
        List<ResourceEntity> resourceEntities = QueryUtils.fetch(ResourceEntity.class,
                  queries.searchOtherResourcesWithName(resourceName, id), 0, -1);
        if (resourceEntities != null) {
            for (ResourceEntity resourceEntity : resourceEntities) {
                if (resourceEntity.getName().equalsIgnoreCase(resourceName)) {
                    String message = "A resource with the name " + resourceName + " already exists!";
                    throw new ElementAlreadyExistsException(message, ResourceType.class, resourceName);
                }
            }
        }
    }

    /**
     * @param resourceTypeName - the resource type name to be validated
     * @param oldResourceTypeName - the current name of the resource type
     * @throws AMWException - if the name is invalid, an AMW exception is thrown
     */
    public void validateResourceTypeName(String resourceTypeName, String oldResourceTypeName) throws AMWException {
        if (resourceTypeName == null || resourceTypeName.trim().isEmpty()) {
            throw new AMWException("The name of the resource type must not be empty!");
        }
        if (!resourceTypeName.equals(oldResourceTypeName)) {
            List<ResourceTypeEntity> resourceTypeEntities = QueryUtils.fetch(ResourceTypeEntity.class,
                      queries.searchResourceTypeByName(resourceTypeName), 0, -1);
            if (resourceTypeEntities != null) {
                for (ResourceTypeEntity resourceTypeEntity : resourceTypeEntities) {
                    if (resourceTypeEntity.getName().equalsIgnoreCase(resourceTypeName)) {
                        String message = "A resource type with the name " + resourceTypeName + " already exists!";
                        throw new ElementAlreadyExistsException(message, ResourceType.class, resourceTypeName);
                    }
                }
            }
        }
    }

    public void validateSoftlinkId(String softlinkId, Integer resourceGroupId) throws AMWException {
        if(softlinkId == null || softlinkId.isEmpty()){
            // empty softlink is allowed
            return;
        }
        // Check if a resource with the same softlinkId exists in an other resourceGroup...
        List<ResourceEntity> resourceEntities = QueryUtils.fetch(ResourceEntity.class,
                queries.searchResourceBySoftlinkIdAndHasNotResourceGroupId(softlinkId, resourceGroupId), 0, -1);
        if (resourceEntities.size() > 0) {
            String message = "A resource with the softlinkId " + softlinkId + " already exists in other resourceGroup!";
            throw new AMWException(message);
        }
    }
}
