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

import ch.mobi.itc.mobiliar.rest.dtos.PropertyBulkUpdateDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyExtendedDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
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

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources/properties", description = "Resource properties")
public class ResourcePropertiesRest {

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ContextLocator contextLocator;

    @GET
    @Path("/{id : \\d+}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all properties for a resource")
    public Response getById(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId) throws
            NotFoundException {

        List<PropertyExtendedDTO> resourceProperties = getResourcePropertiesById(resourceId, contextId);
        return Response.ok(resourceProperties).build();
    }

    List<PropertyExtendedDTO> getResourcePropertiesById(Integer resourceId, Integer contextId) throws NotFoundException {
        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        List<PropertyExtendedDTO> result = new ArrayList<>();

        if (resource != null) {
            ContextEntity context = contextLocator.getById(contextId);

            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResource(
                    resource.getId(),
                    context.getId());

            for (ResourceEditProperty property : properties) {
                result.add(new PropertyExtendedDTO(property, context.getName(), contextId));
            }
        }
        return result;
    }

    @GET
    @Path("/{id : \\d+}/properties/{propertyName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a specific property by name for a resource")
    public Response getResourceProperty(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @PathParam("propertyName") String propertyName,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws NotFoundException {

        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        if (resource != null) {
            ContextEntity context = contextLocator.getById(contextId);
            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResource(
                    resource.getId(),
                    context.getId());

            for (ResourceEditProperty property : properties) {
                if (property.getTechnicalKey().equals(propertyName)) {
                    return Response.ok(new PropertyExtendedDTO(property, context.getName(), contextId)).build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{id : \\d+}/properties/{propertyName}")
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
        ContextEntity context = contextLocator.getById(contextId);

        propertyEditor.setPropertyValueOnResourceForContext(
                resource.getName(),
                resource.getRelease().getName(),
                context.getName(),
                propertyName,
                value);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // TODO Fully delete property


    @PUT
    @Path("/{id : \\d+}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Bulk update/ reset multiple property values using resource ID and context ID")
    public Response bulkUpdateResourceProperties(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            PropertyBulkUpdateDTO bulkRequest,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ValidationException, NotFoundException {

        if (bulkRequest == null || isRequestEmpty(bulkRequest)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        ResourceEntity resource = resourceBoundary.getResource(resourceId);
        ContextEntity context = contextLocator.getById(contextId);

        if (bulkRequest.getUpdates() != null) {
            for (PropertyDTO property : bulkRequest.getUpdates()) {
                validateProperty(property);
                propertyEditor.setPropertyValueOnResourceForContext(
                        resource.getName(),
                        resource.getRelease().getName(),
                        context.getName(),
                        property.getName(),
                        property.getValue());
            }
        }

        if (bulkRequest.getResets() != null) {
            for (PropertyDTO property : bulkRequest.getResets()) {
                validateProperty(property);
                propertyEditor.resetPropertyValueOnResourceForContext(
                        resource.getName(),
                        resource.getRelease().getName(),
                        context.getName(),
                        property.getName());
            }
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private void validateProperty(PropertyDTO property) throws ValidationException {
        if (property == null || property.getName() == null || property.getName().trim().isEmpty()) {
            throw new ValidationException("Property name cannot be null or empty");
        }
    }

    private boolean isRequestEmpty(PropertyBulkUpdateDTO request) {
        boolean updatesEmpty = request.getUpdates() == null || request.getUpdates().isEmpty();
        boolean resetsEmpty = request.getResets() == null || request.getResets().isEmpty();
        return updatesEmpty && resetsEmpty;
    }

    @GET
    @Path("/{resourceGroupName}/{releaseName}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all properties for a resource in a specific release")
    public Response getResourceProperties(
            @Parameter(description = "Resource group name") @PathParam("resourceGroupName") String resourceGroupName,
            @Parameter(description = "Release name") @PathParam("releaseName") String releaseName,
            @DefaultValue("Global") @QueryParam("env") String environment) throws ValidationException {

        List<PropertyDTO> resourceProperties = getPropertiesByResourceGroupNameAndReleaseName(resourceGroupName, releaseName, environment);
        if (resourceProperties.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(resourceProperties).build();
    }

    List<PropertyDTO> getPropertiesByResourceGroupNameAndReleaseName(String resourceGroupName, String releaseName, String environment) throws ValidationException {
        ResourceEntity resource = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        List<PropertyDTO> result = new ArrayList<>();
        if (resource != null) {
            ContextEntity context = contextLocator.getContextByName(environment);
            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResource(resource.getId(),
                    context.getId());
            for (ResourceEditProperty property : properties) {
                result.add(new PropertyDTO(property, context.getName()));
            }
        }
        return result;
    }


    @GET
    @Path("/{resourceGroupName}/{releaseName}/properties/{propertyName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the property including its value for a resource in a specific release")
    public Response getResourcePropertyValueForEnvironment(
            @Parameter(description = "Resource group name") @PathParam("resourceGroupName") String resourceGroupName,
            @Parameter(description = "Release name") @PathParam("releaseName") String releaseName,
            @PathParam("propertyName") String propertyName,
            @DefaultValue("Global") @QueryParam("env") String environment) throws ValidationException {
        ResourceEntity resource = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        if (resource != null) {
            ContextEntity context = contextLocator.getContextByName(environment);
            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResource(resource.getId(),
                    context.getId());
            for (ResourceEditProperty property : properties) {
                if (property.getTechnicalKey().equals(propertyName)) {
                    return Response.ok(new PropertyDTO(property, context.getName())).build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{resourceGroupName}/{releaseName}/properties/{propertyName}")
    @Consumes(TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Set the value of an existing property on a resource in a specific release")
    public Response updateResourceProperty(
            @Parameter(description = "Resource group name") @PathParam("resourceGroupName") String resourceGroupName,
            @Parameter(description = "Release name") @PathParam("releaseName") String releaseName,
            @Parameter(description = "the new value of the property") String value,
            @PathParam("propertyName") String propertyName,
            @DefaultValue("Global") @QueryParam("env") String environment) throws ValidationException {
        propertyEditor.setPropertyValueOnResourceForContext(resourceGroupName, releaseName, environment, propertyName, value);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{resourceGroupName}/{releaseName}/properties/{propertyName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Reset the value of the given property in the specified context to null")
    public Response resetResourceProperty(
            @Parameter(description = "Resource group name") @PathParam("resourceGroupName") String resourceGroupName,
            @Parameter(description = "Release name") @PathParam("releaseName") String releaseName,
            @PathParam("propertyName") String propertyName,
            @DefaultValue("Global") @QueryParam("env") String environment) throws ValidationException {
        propertyEditor.resetPropertyValueOnResourceForContext(resourceGroupName, releaseName, environment, propertyName);
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{resourceGroupName}/{releaseName}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Bulk update multiple property values for a resource in a specific release")
    public Response bulkUpdateResourceProperties(
            @Parameter(description = "Resource group name") @PathParam("resourceGroupName") String resourceGroupName,
            @Parameter(description = "Release name") @PathParam("releaseName") String releaseName,
            List<PropertyDTO> properties, @DefaultValue("Global")
            @QueryParam("env") String environment) throws ValidationException {
        if (properties == null || properties.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Properties list cannot be empty").build();
        }

        for (PropertyDTO property : properties) {
            if (property.getName() == null || property.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Property name cannot be null or empty").build();
            }
            propertyEditor.setPropertyValueOnResourceForContext(
                    resourceGroupName,
                    releaseName,
                    environment,
                    property.getName(),
                    property.getValue());
        }

        return Response.status(Response.Status.OK).build();
    }
}
