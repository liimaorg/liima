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

package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

@Stateless
public class ResourceTypeLocator {

    @Inject
    ResourceTypeDomainService resourceTypeDomainService;

    public ResourceTypeEntity getResourceType(Integer resourceTypeId) {
        return resourceTypeDomainService.getResourceType(resourceTypeId);
    }

    /**
     * Returns all available ResourceTypeEntities without children, ordered alphabetically
     *
     * @return
     */
    public List<ResourceTypeEntity> getAllResourceTypes() throws NotFoundException {
        List<ResourceTypeEntity> allResourceTypes = resourceTypeDomainService.getAllResourceTypesWithoutChildren();
        if (allResourceTypes.isEmpty()) {
            throw new NotFoundException("No resource types found");
        }
        return allResourceTypes;
    }

    public List<ResourceTypeEntity> getPredefinedResourceTypes() throws NotFoundException {
        List<ResourceTypeEntity> predefinedResourceTypes = new ArrayList<>();
        for (ResourceTypeEntity e : resourceTypeDomainService.getResourceTypes()) {
            if (e.getParentResourceType() == null && ResourceType.createByResourceType(e, null).isDefaultResourceType()) {
                predefinedResourceTypes.add(e);
            }
        }
        if (predefinedResourceTypes.isEmpty()) {
            throw new NotFoundException("No predefined resource types found");
        }
        return predefinedResourceTypes;
    }

    public List<ResourceTypeEntity> getRootResourceTypes() throws NotFoundException {
        List<ResourceTypeEntity> allResourceTypes = getAllResourceTypes();
        List<ResourceTypeEntity> predefinedResourceTypes = getPredefinedResourceTypes();
        allResourceTypes.removeAll(predefinedResourceTypes);
        return allResourceTypes;
    }

    public boolean hasChildren(Integer resourceTypeId) {
        return resourceTypeDomainService.hasChildren(resourceTypeId);
    }
}
