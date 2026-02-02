package ch.mobi.itc.mobiliar.rest.resources;


import ch.mobi.itc.mobiliar.rest.dtos.PropertyDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
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

        List<PropertyDTO> resourceProperties = getResourceTypePropertiesById(resourceTypeId, contextId);
        return Response.ok(resourceProperties).build();
    }

    List<PropertyDTO> getResourceTypePropertiesById(Integer resourceTypeId, Integer contextId) throws NotFoundException {
        ResourceTypeEntity resourceType = resourceBoundary.getResourceType(resourceTypeId);
        List<PropertyDTO> result = new ArrayList<>();

        if (resourceType != null) {
            ContextEntity context = contextLocator.getById(contextId);

            List<ResourceEditProperty> properties = propertyEditor.getPropertiesForResourceType(
                    resourceType.getId(),
                    context.getId());

            for (ResourceEditProperty property : properties) {
                result.add(new PropertyDTO(property, context.getName()));
            }
        }
        return result;
    }
}
