package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyDescriptorDTO;
import ch.mobi.itc.mobiliar.rest.resources.propertyDescriptor.*;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyTagEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.PropertyDescriptorNotDeletableException;
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

    @Inject
    PropertyTagEditor propertyTagEditor;

    @GET
    @Path("/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a property descriptor by ID")
    public Response getPropertyDescriptor(
            @Parameter(description = "Property Descriptor ID") @PathParam("id") Integer descriptorId) throws NotFoundException {
        
        GetPropertyDescriptorCommand command = new GetPropertyDescriptorCommand(descriptorId);
        PropertyDescriptorEntity descriptor = propertyEditor.getPropertyDescriptor(command.getDescriptorId());
        
        if (descriptor == null) {
            throw new NotFoundException("Property descriptor not found");
        }

        return Response.ok(new PropertyDescriptorDTO(descriptor)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property descriptor for a resource")
    public Response createPropertyDescriptorForResource(
            @Parameter(description = "Resource ID") @QueryParam("resourceId") Integer resourceId,
            PropertyDescriptorDTO descriptorDTO) throws AMWException {

        CreatePropertyDescriptorCommand command = new CreatePropertyDescriptorCommand(resourceId, null, descriptorDTO);
        PropertyDescriptorEntity entity = command.getDescriptorDTO().toEntity();
        String tagsString = convertTagsToString(command.getDescriptorDTO().getPropertyTags());
        
        PropertyDescriptorEntity saved = propertyEditor.savePropertyDescriptorForResource(
                command.getResourceId(), entity, tagsString);
        
        return Response.status(Response.Status.CREATED)
                .entity(new PropertyDescriptorDTO(saved))
                .build();
    }

    @POST
    @Path("/resourceType")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property descriptor for a resource type")
    public Response createPropertyDescriptorForResourceType(
            @Parameter(description = "Resource Type ID") @QueryParam("resourceTypeId") Integer resourceTypeId,
            PropertyDescriptorDTO descriptorDTO) throws AMWException {

        CreatePropertyDescriptorCommand command = new CreatePropertyDescriptorCommand(null, resourceTypeId, descriptorDTO);
        PropertyDescriptorEntity entity = command.getDescriptorDTO().toEntity();
        String tagsString = convertTagsToString(command.getDescriptorDTO().getPropertyTags());
        
        PropertyDescriptorEntity saved = propertyEditor.savePropertyDescriptorForResourceType(
                command.getResourceTypeId(), entity, tagsString);
        
        return Response.status(Response.Status.CREATED)
                .entity(new PropertyDescriptorDTO(saved))
                .build();
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
            PropertyDescriptorDTO descriptorDTO) throws AMWException, NotFoundException {

        UpdatePropertyDescriptorCommand command = new UpdatePropertyDescriptorCommand(descriptorId, resourceId, resourceTypeId, descriptorDTO);
        
        PropertyDescriptorEntity existingEntity = propertyEditor.getPropertyDescriptor(command.getDescriptorId());
        if (existingEntity == null) {
            throw new NotFoundException("Property descriptor not found");
        }
        
        // Update the fields from DTO
        PropertyDescriptorDTO dto = command.getDescriptorDTO();
        existingEntity.setPropertyName(dto.getName());
        existingEntity.setDisplayName(dto.getDisplayName());
        existingEntity.setValidationLogic(dto.getValidationRegex());
        existingEntity.setNullable(dto.isNullable());
        existingEntity.setOptional(dto.isOptional());
        existingEntity.setEncrypt(dto.isEncrypted());
        existingEntity.setMachineInterpretationKey(dto.getMik());
        existingEntity.setDefaultValue(dto.getDefaultValue());
        existingEntity.setExampleValue(dto.getExampleValue());
        existingEntity.setPropertyComment(dto.getComment());
        
        String tagsString = convertTagsToString(dto.getPropertyTags());
        
        PropertyDescriptorEntity saved;
        if (command.isForResource()) {
            saved = propertyEditor.savePropertyDescriptorForResource(command.getResourceId(), existingEntity, tagsString);
        } else {
            saved = propertyEditor.savePropertyDescriptorForResourceType(command.getResourceTypeId(), existingEntity, tagsString);
        }
        
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
            @Parameter(description = "Force delete even if property values exist") @DefaultValue("false") @QueryParam("force") boolean forceDelete) throws AMWException, NotFoundException, PropertyDescriptorNotDeletableException {

        DeletePropertyDescriptorCommand command = new DeletePropertyDescriptorCommand(descriptorId, resourceId, resourceTypeId, forceDelete);
        
        PropertyDescriptorEntity descriptor = propertyEditor.getPropertyDescriptor(command.getDescriptorId());
        if (descriptor == null) {
            throw new NotFoundException("Property descriptor not found");
        }

        if (command.isForResource()) {
            propertyEditor.deletePropertyDescriptorForResource(command.getResourceId(), descriptor, command.isForceDelete());
        } else {
            propertyEditor.deletePropertyDescriptorForResourceType(command.getResourceTypeId(), descriptor, command.isForceDelete());
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private String convertTagsToString(java.util.List<ch.mobi.itc.mobiliar.rest.dtos.PropertyTagDTO> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (ch.mobi.itc.mobiliar.rest.dtos.PropertyTagDTO tag : tags) {
            if (tag.getName() != null && !tag.getName().isEmpty()) {
                result.append(tag.getName()).append(",");
            }
        }
        return result.toString();
    }
}
