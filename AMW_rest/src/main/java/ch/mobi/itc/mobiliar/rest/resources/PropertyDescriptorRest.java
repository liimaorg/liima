package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyDescriptorDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyTagEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
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
            @Parameter(description = "Property Descriptor ID") @PathParam("id") Integer descriptorId) {
        
        if (descriptorId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property descriptor ID cannot be null")
                    .build();
        }

        PropertyDescriptorEntity descriptor = propertyEditor.getPropertyDescriptor(descriptorId);
        if (descriptor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Property descriptor not found")
                    .build();
        }

        return Response.ok(new PropertyDescriptorDTO(descriptor)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property descriptor for a resource")
    public Response createPropertyDescriptorForResource(
            @Parameter(description = "Resource ID") @QueryParam("resourceId") Integer resourceId,
            PropertyDescriptorDTO descriptorDTO) {

        if (resourceId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Resource ID is required")
                    .build();
        }

        if (descriptorDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property descriptor data is required")
                    .build();
        }

        try {
            PropertyDescriptorEntity entity = descriptorDTO.toEntity();
            String tagsString = convertTagsToString(descriptorDTO.getPropertyTags());
            
            PropertyDescriptorEntity saved = propertyEditor.savePropertyDescriptorForResource(
                    resourceId, entity, tagsString);
            
            return Response.status(Response.Status.CREATED)
                    .entity(new PropertyDescriptorDTO(saved))
                    .build();
        } catch (AMWException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/resourceType")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property descriptor for a resource type")
    public Response createPropertyDescriptorForResourceType(
            @Parameter(description = "Resource Type ID") @QueryParam("resourceTypeId") Integer resourceTypeId,
            PropertyDescriptorDTO descriptorDTO) {

        if (resourceTypeId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Resource type ID is required")
                    .build();
        }

        if (descriptorDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property descriptor data is required")
                    .build();
        }

        try {
            PropertyDescriptorEntity entity = descriptorDTO.toEntity();
            String tagsString = convertTagsToString(descriptorDTO.getPropertyTags());
            
            PropertyDescriptorEntity saved = propertyEditor.savePropertyDescriptorForResourceType(
                    resourceTypeId, entity, tagsString);
            
            return Response.status(Response.Status.CREATED)
                    .entity(new PropertyDescriptorDTO(saved))
                    .build();
        } catch (AMWException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
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
            PropertyDescriptorDTO descriptorDTO) {

        if (descriptorId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property descriptor ID cannot be null")
                    .build();
        }

        if (descriptorDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property descriptor data is required")
                    .build();
        }

        if (resourceId == null && resourceTypeId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Either resourceId or resourceTypeId must be provided")
                    .build();
        }

        try {
            // Load the existing entity to preserve version and other managed fields
            PropertyDescriptorEntity existingEntity = propertyEditor.getPropertyDescriptor(descriptorId);
            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Property descriptor not found")
                        .build();
            }
            
            // Update the fields from DTO
            existingEntity.setPropertyName(descriptorDTO.getName());
            existingEntity.setDisplayName(descriptorDTO.getDisplayName());
            existingEntity.setValidationLogic(descriptorDTO.getValidationRegex());
            existingEntity.setNullable(descriptorDTO.isNullable());
            existingEntity.setOptional(descriptorDTO.isOptional());
            existingEntity.setEncrypt(descriptorDTO.isEncrypted());
            existingEntity.setMachineInterpretationKey(descriptorDTO.getMik());
            existingEntity.setDefaultValue(descriptorDTO.getDefaultValue());
            existingEntity.setExampleValue(descriptorDTO.getExampleValue());
            existingEntity.setPropertyComment(descriptorDTO.getComment());
            
            String tagsString = convertTagsToString(descriptorDTO.getPropertyTags());
            
            PropertyDescriptorEntity saved;
            if (resourceId != null) {
                saved = propertyEditor.savePropertyDescriptorForResource(resourceId, existingEntity, tagsString);
            } else {
                saved = propertyEditor.savePropertyDescriptorForResourceType(resourceTypeId, existingEntity, tagsString);
            }
            
            return Response.ok(new PropertyDescriptorDTO(saved)).build();
        } catch (AMWException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a property descriptor")
    public Response deletePropertyDescriptor(
            @Parameter(description = "Property Descriptor ID") @PathParam("id") Integer descriptorId,
            @Parameter(description = "Resource ID (for resource-level descriptors)") @QueryParam("resourceId") Integer resourceId,
            @Parameter(description = "Resource Type ID (for resource type-level descriptors)") @QueryParam("resourceTypeId") Integer resourceTypeId,
            @Parameter(description = "Force delete even if property values exist") @DefaultValue("false") @QueryParam("force") boolean forceDelete) {

        if (descriptorId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property descriptor ID cannot be null")
                    .build();
        }

        if (resourceId == null && resourceTypeId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Either resourceId or resourceTypeId must be provided")
                    .build();
        }

        try {
            PropertyDescriptorEntity descriptor = propertyEditor.getPropertyDescriptor(descriptorId);
            if (descriptor == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Property descriptor not found")
                        .build();
            }

            if (resourceId != null) {
                propertyEditor.deletePropertyDescriptorForResource(resourceId, descriptor, forceDelete);
            } else {
                propertyEditor.deletePropertyDescriptorForResourceType(resourceTypeId, descriptor, forceDelete);
            }

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (PropertyDescriptorNotDeletableException e) {
            String additionalInfo = "If you force the deletion, all those property values will be deleted as well";
            String errorMessage = String.format("%s. %s", e.getMessage(), additionalInfo);
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorMessage)
                    .build();
        } catch (AMWException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
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
