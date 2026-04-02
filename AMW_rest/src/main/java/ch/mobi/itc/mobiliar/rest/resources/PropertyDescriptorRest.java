package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyDescriptorDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.command.*;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("/propertyDescriptors")
@Tag(name = "/propertyDescriptors", description = "Property descriptors")
public class PropertyDescriptorRest {

    @Inject
    PropertyEditor propertyEditor;

    @GET
    @Path("/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a property descriptor by ID")
    public Response getPropertyDescriptor(
            @Parameter(description = "Property Descriptor ID") @PathParam("id") Integer descriptorId) throws NotFoundException {
        GetPropertyDescriptorCommand command = new GetPropertyDescriptorCommand(descriptorId);
        PropertyDescriptorEntity descriptor = propertyEditor.getDescriptorEntity(command);
        return Response.ok(new PropertyDescriptorDTO(descriptor)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property descriptor for a resource")
    public Response createPropertyDescriptorForResource(
            @Parameter(description = "Resource ID") @QueryParam("resourceId") Integer resourceId,
            PropertyDescriptorDTO descriptorDTO) throws IllegalArgumentException, IllegalStateException {
        CreatePropertyDescriptorCommand command = new CreatePropertyDescriptorCommand(resourceId, null, descriptorDTO.asData());
        PropertyDescriptorEntity saved = propertyEditor.createDescriptorEntity(command);
        return Response.status(Response.Status.CREATED).entity(new PropertyDescriptorDTO(saved)).build();
    }

    @POST
    @Path("/resourceType")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property descriptor for a resource type")
    public Response createPropertyDescriptorForResourceType(
            @Parameter(description = "Resource Type ID") @QueryParam("resourceTypeId") Integer resourceTypeId,
            PropertyDescriptorDTO descriptorDTO) throws IllegalArgumentException, IllegalStateException {
        CreatePropertyDescriptorCommand command = new CreatePropertyDescriptorCommand(null, resourceTypeId, descriptorDTO.asData());
        PropertyDescriptorEntity saved = propertyEditor.createDescriptorEntity(command);
        return Response.status(Response.Status.CREATED).entity(new PropertyDescriptorDTO(saved)).build();
    }

    @PUT
    @Path("/{id : \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update an existing property descriptor")
    public Response updatePropertyDescriptor(
            @Parameter(description = "Property Descriptor ID") @PathParam("id") Integer descriptorId,
            @Parameter(description = "Resource ID (for resource-level descriptors)") @QueryParam("resourceId") Integer resourceId,
            @Parameter(description = "Resource Type ID (for resource type-level descriptors)") @QueryParam("resourceTypeId") Integer resourceTypeId,
            PropertyDescriptorDTO descriptorDTO) throws IllegalArgumentException, NotFoundException {
        UpdatePropertyDescriptorCommand command = new UpdatePropertyDescriptorCommand(descriptorId, resourceId, resourceTypeId, descriptorDTO.asData());
        PropertyDescriptorEntity saved = propertyEditor.updateDescriptorEntity(command);
        return Response.ok(new PropertyDescriptorDTO(saved)).build();
    }

    @DELETE
    @Path("/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a property descriptor")
    public Response deletePropertyDescriptor(
            @Parameter(description = "Property Descriptor ID") @PathParam("id") Integer descriptorId,
            @Parameter(description = "Resource ID (for resource-level descriptors)") @QueryParam("resourceId") Integer resourceId,
            @Parameter(description = "Resource Type ID (for resource type-level descriptors)") @QueryParam("resourceTypeId") Integer resourceTypeId,
            @Parameter(description = "Force delete even if property values exist") @DefaultValue("false") @QueryParam("force") boolean forceDelete) throws IllegalArgumentException, NotFoundException {
        DeletePropertyDescriptorCommand command = new DeletePropertyDescriptorCommand(descriptorId, resourceId, resourceTypeId, forceDelete);
        propertyEditor.deleteDescriptorEntity(command);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
