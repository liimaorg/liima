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

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTypeDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@RequestScoped
@Path("/resources")
@Api(value = "/resources", description = "ResourceTypes")
public class ResourceTypesRest {

    @Inject
    private ResourceTypeLocator resourceTypeLocator;

    @Inject
    private ResourceTypeDomainService resourceTypeDomainService;

    @Inject
    PropertyEditor propertyEditor;

    @Path("/resourceTypes")
    @GET
    @ApiOperation(value = "Get all resource types")
    public List<ResourceTypeDTO> getAllResourceTypes() {
        return resourceTypeLocator.getAllResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
    }

    @Path("/predefinedResourceTypes")
    @GET
    @ApiOperation(value = "Get predefined resource types")
    public List<ResourceTypeDTO> getPredefinedResourceTypes() {
        return resourceTypeLocator.getPredefinedResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
    }

    @Path("/rootResourceTypes")
    @GET
    @ApiOperation(value = "Get root resource types")
    public List<ResourceTypeDTO> getRootResourceTypes() {
        return resourceTypeLocator.getRootResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
    }

    @DELETE
    @Path("/resourceTypes/{id : \\d+}")
    @ApiOperation(value = "Delete a resource type")
    public Response deleteResourceType(@PathParam("id") Integer id) throws NotAuthorizedException, NotFoundException {
        try {
            if (resourceTypeLocator.getPredefinedResourceTypes().stream()
                    .anyMatch(resourceType -> resourceType.getId().equals(id))) {
                throw new NotAuthorizedException("Predefined resource types cannot be deleted.");
            }
            resourceTypeDomainService.removeResourceType(id);
        } catch (ResourceNotFoundException | ResourceTypeNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
        return Response.status(NO_CONTENT).build();
    }
}
