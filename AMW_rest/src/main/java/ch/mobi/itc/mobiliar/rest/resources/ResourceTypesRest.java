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

package ch.mobi.itc.mobiliar.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTypeDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestScoped
@Path("/resources")
@Api(value = "/resources", description = "ResourceTypes")
public class ResourceTypesRest {

    @Inject
    private ResourceTypeLocator resourceTypeLocator;

    @Inject
    PropertyEditor propertyEditor;

    @Path("/resourceTypes")
    @GET
    @ApiOperation(value = "Get all resource types")
    public List<ResourceTypeDTO> getAllResourceTypes() throws NotFoundException {
        List<ResourceTypeEntity> resourceTypes = resourceTypeLocator.getAllResourceTypes();
        List<ResourceTypeDTO> resourceTypeDTOs = new ArrayList<>();
        for (ResourceTypeEntity resourceType : resourceTypes) {
            resourceTypeDTOs.add(new ResourceTypeDTO(resourceType));
        }
        return resourceTypeDTOs;
    }

    @Path("/predefinedResourceTypes")
    @GET
    @ApiOperation(value = "Get predefined resource types")
    public List<ResourceTypeDTO> getPredefinedResourceTypes() throws NotFoundException {
        List<ResourceTypeEntity> resourceTypes = resourceTypeLocator.getPredefinedResourceTypes();
        List<ResourceTypeDTO> resourceTypeDTOs = new ArrayList<>();
        for (ResourceTypeEntity resourceType : resourceTypes) {
            resourceTypeDTOs.add(new ResourceTypeDTO(resourceType));
        }
        return resourceTypeDTOs;
    }

    @Path("/rootResourceTypes")
    @GET
    @ApiOperation(value = "Get root resource types")
    public List<ResourceTypeDTO> getRootResourceTypes() throws NotFoundException {
        List<ResourceTypeEntity> resourceTypes = resourceTypeLocator.getRootResourceTypes();
        List<ResourceTypeDTO> resourceTypeDTOs = new ArrayList<>();
        for (ResourceTypeEntity resourceType : resourceTypes) {
            resourceTypeDTOs.add(new ResourceTypeDTO(resourceType));
        }
        return resourceTypeDTOs;
    }

    @Path("/resourceTypes/{resourceTypeId}/hasChildren")
    @GET
    @ApiOperation(value = "Checks if resource type has children")
    public boolean hasChildren(@PathParam("resourceTypeId") int resourceTypeId) {
        return resourceTypeLocator.hasChildren(resourceTypeId);
    }
}
