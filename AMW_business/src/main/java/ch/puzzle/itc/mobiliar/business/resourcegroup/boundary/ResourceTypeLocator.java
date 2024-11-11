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

import java.util.List;
import java.util.stream.Collectors;

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
        return resourceTypeDomainService.getAllResourceTypesWithoutChildren();
    }

    public List<ResourceTypeEntity> getPredefinedResourceTypes() {
        return resourceTypeDomainService.getResourceTypes()
                .stream()
                .filter(e -> e.getParentResourceType() == null && ResourceType.createByResourceType(e, null).isDefaultResourceType())
                .collect(Collectors.toList());
    }

    public List<ResourceTypeEntity> getRootResourceTypes() {
        return resourceTypeDomainService.getResourceTypes()
                .stream()
                .filter(e -> e.getParentResourceType() == null && !ResourceType.createByResourceType(e, null).isDefaultResourceType())
                .collect(Collectors.toList());
    }
}
