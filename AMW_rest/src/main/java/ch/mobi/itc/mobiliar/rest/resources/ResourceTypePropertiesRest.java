package ch.mobi.itc.mobiliar.rest.resources;


import ch.mobi.itc.mobiliar.rest.dtos.PropertyBulkUpdateDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyExtendedDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.DecryptionException;
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
@Path("/resourceTypes")
@Tag(name = "/resourceTypes/properties", description = "ResourceTypes properties (ID-based API)")
public class ResourceTypePropertiesRest {

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    ContextLocator contextLocator;

    @GET
    @Path("/{id : \\d+}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all properties for a resource type")
    public Response getResourceTypeProperties(
            @Parameter(description = "ResourceType ID") @PathParam("id") Integer resourceTypeId,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId) throws
            NotFoundException {

        List<PropertyExtendedDTO> resourceProperties = getResourceTypePropertiesById(resourceTypeId, contextId);
        return Response.ok(resourceProperties).build();
    }

    List<PropertyExtendedDTO> getResourceTypePropertiesById(Integer resourceTypeId, Integer contextId) throws NotFoundException, DecryptionException {
        ResourceTypeEntity resourceType = resourceBoundary.getResourceType(resourceTypeId);
        List<PropertyExtendedDTO> result = new ArrayList<>();

        if (resourceType != null) {
            ContextEntity context = contextLocator.getById(contextId);

            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResourceType(
                    resourceType.getId(),
                    context.getId());

            for (ResourceEditProperty property : properties) {
                result.add(new PropertyExtendedDTO(property, context.getName(), context.getId(), getOverwriteInfos(resourceType, contextId, property)));
            }
        }
        return result;
    }

    private List<PropertyEditingService.DifferingProperty> getOverwriteInfos(ResourceTypeEntity resource, Integer contextId, ResourceEditProperty property) {
        List<ContextEntity> contexts = contextLocator.getChildren(contextId);
        return propertyEditor.getPropertyOverviewForResourceType(resource, property, contexts);
    }

    @PUT
    @Path("/{id : \\d+}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Bulk update/ reset multiple property values using resourceType ID and context ID")
    public Response bulkUpdateResourceProperties(
            @Parameter(description = "ResourceType ID") @PathParam("id") Integer resourceTypeId,
            PropertyBulkUpdateDTO bulkRequest,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ValidationException, NotFoundException {

        if (bulkRequest == null || isRequestEmpty(bulkRequest)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        if (resourceTypeId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ResourceType ID cannot be null").build();
        }

        if (contextId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Context ID cannot be null").build();
        }

        ResourceTypeEntity resourceType = resourceBoundary.getResourceType(resourceTypeId);
        if (resourceType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ContextEntity context = contextLocator.getById(contextId);
        if (context == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Context not found").build();
        }

        if (bulkRequest.getUpdates() != null) {
            for (PropertyDTO property : bulkRequest.getUpdates()) {
                validateProperty(property);
                propertyEditor.setPropertyValueOnResourceTypeForContext(
                        resourceType,
                        context,
                        property.getName(),
                        property.getValue());
            }
        }

        if (bulkRequest.getResets() != null) {
            for (PropertyDTO property : bulkRequest.getResets()) {
                validateProperty(property);
                propertyEditor.resetPropertyValueOnResourceTypeForContext(
                        resourceType,
                        context,
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
}
