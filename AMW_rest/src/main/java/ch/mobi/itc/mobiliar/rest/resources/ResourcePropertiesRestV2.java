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

import ch.mobi.itc.mobiliar.rest.dtos.PropertyDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/resourcesV2")
@Tag(name = "/resources/properties", description = "Resource properties (ID-based API)")
public class ResourcePropertiesRestV2 {

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    ContextLocator contextLocator;

    @GET
    @Path("/resource/{id : \\d+}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all properties for a resource")
    public Response getResourceProperties(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId) throws
            ValidationException, NotFoundException {
        
        List<PropertyDTO> resourceProperties = getResourcePropertiesById(resourceId, contextId);
        if (resourceProperties.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(resourceProperties).build();
    }

    List<PropertyDTO> getResourcePropertiesById(Integer resourceId, Integer contextId) throws ValidationException, NotFoundException {
        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        List<PropertyDTO> result = new ArrayList<>();
        
        if (resource != null) {
            ContextEntity context = contextLocator.getContextById(contextId);
            if (context == null) {
                throw new ValidationException("Context with ID " + contextId + " not found");
            }
            
            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResource(
                    resource.getId(), 
                    context.getId());
            
            for (ResourceEditProperty property : properties) {
                result.add(new PropertyDTO(property, context.getName()));
            }
        }
        return result;
    }

    @GET
    @Path("/resourceType/{id : \\d+}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all properties for a resource type")
    public Response getResourceTypeProperties(
            @Parameter(description = "ResourceType ID") @PathParam("id") Integer resourceTypeId,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId) throws
            ValidationException, NotFoundException {

        List<PropertyDTO> resourceProperties = getResourceTypePropertiesById(resourceTypeId, contextId);
        if (resourceProperties.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(resourceProperties).build();
    }

    List<PropertyDTO> getResourceTypePropertiesById(Integer resourceTypeId, Integer contextId) throws ValidationException, NotFoundException {
        ResourceTypeEntity resourceType = resourceBoundary.getResourceType(resourceTypeId);
        List<PropertyDTO> result = new ArrayList<>();

        if (resourceType != null) {
            ContextEntity context = contextLocator.getContextById(contextId);
            if (context == null) {
                throw new ValidationException("Context with ID " + contextId + " not found");
            }

            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResourceType(
                    resourceType.getId(),
                    context.getId());

            for (ResourceEditProperty property : properties) {
                result.add(new PropertyDTO(property, context.getName()));
            }
        }
        return result;
    }

    @Path("/resource/{id : \\d+}/properties/{propertyName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a specific property by name for a resource")
    public Response getResourceProperty(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @PathParam("propertyName") String propertyName,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ValidationException, NotFoundException {
        
        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        if (resource != null) {
            ContextEntity context = contextLocator.getContextById(contextId);
            if (context == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Context not found").build();
            }
            
            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResource(
                    resource.getId(), 
                    context.getId());
            
            for (ResourceEditProperty property : properties) {
                if (property.getTechnicalKey().equals(propertyName)) {
                    return Response.ok(new PropertyDTO(property, context.getName())).build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("/resource/{id : \\d+}/properties/{propertyName}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a single property value by resource ID and context ID")
    public Response updateResourceProperty(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @PathParam("propertyName") String propertyName,
            @Parameter(description = "New property value") String value,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ValidationException, NotFoundException {
        
        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Resource not found").build();
        }
        
        ContextEntity context = contextLocator.getContextById(contextId);
        if (context == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Context not found").build();
        }
        
        propertyEditor.setPropertyValueOnResourceForContext(
                resource.getName(), 
                resource.getRelease().getName(), 
                context.getName(), 
                propertyName, 
                value);
        
        return Response.status(Response.Status.OK).build();
    }

    @Path("/resource/{id : \\d+}/properties/{propertyName}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Reset a property value to null")
    public Response resetResourceProperty(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @PathParam("propertyName") String propertyName,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ValidationException, NotFoundException {
        
        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Resource not found").build();
        }
        
        ContextEntity context = contextLocator.getContextById(contextId);
        if (context == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Context not found").build();
        }
        
        propertyEditor.resetPropertyValueOnResourceForContext(
                resource.getName(), 
                resource.getRelease().getName(), 
                context.getName(), 
                propertyName);
        
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/resource/{id : \\d+}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Bulk update multiple property values using resource ID and context ID")
    public Response bulkUpdateResourceProperties(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            List<PropertyDTO> properties,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ValidationException, NotFoundException {
        
        if (properties == null || properties.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Properties list cannot be empty")
                    .build();
        }
        
        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Resource not found")
                    .build();
        }
        
        ContextEntity context = contextLocator.getContextById(contextId);
        if (context == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Context not found")
                    .build();
        }
        
        for (PropertyDTO property : properties) {
            if (property.getName() == null || property.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Property name cannot be null or empty")
                        .build();
            }
            
            propertyEditor.setPropertyValueOnResourceForContext(
                    resource.getName(), 
                    resource.getRelease().getName(), 
                    context.getName(), 
                    property.getName(), 
                    property.getValue());
        }
        
        return Response.status(Response.Status.OK).build();
    }
}
