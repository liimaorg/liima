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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceReleaseDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestScoped
@Path("/resources")
@Api(value = "/resources", description = "Resources")
public class ResourcesRest {

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    PropertyEditor propertyEditor;


    // TODO: better GET /{resourceId}/properties/name?
    @Path("name/{resourceId}")
    @GET
    @ApiOperation(value = "Get resource name by id")
    public Response getResourceName(@PathParam("resourceId") Integer resourceId) {
        Map<String, String> responseMap = new HashMap<>();
        String resourceName = resourceBoundary.getResourceName(resourceId);
        responseMap.put("name", resourceName);
        return Response.status(Response.Status.OK).entity(responseMap).build();
    }


    // TODO: should be removed and instead handeled with a 404
    @Path("exists/{resourceId}")
    @GET
    @ApiOperation(value = "Checks if a specific Resource still exists - used by Angular")
    public Response isExistingResourceGroup(@PathParam("resourceId") Integer resourceId) {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.ok(false).build();
        }
        return Response.ok(true).build();
    }


    @Path("/resources/{resourceId}")
    @GET
    @ApiOperation(value = "Get a resource")
    public Response getResource(@PathParam("resourceId") Integer resourceId) {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ResourceReleaseDTO result = new ResourceReleaseDTO();
        result.setName(resource.getName());
        result.setReleaseName(resource.getRelease().getName());
        result.setType(resource.getResourceType().getName());
        return Response.ok(result).build();
    }
}
