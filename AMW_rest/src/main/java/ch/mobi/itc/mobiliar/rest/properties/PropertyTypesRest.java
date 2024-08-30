package ch.mobi.itc.mobiliar.rest.properties;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyTypeDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTypeService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@Stateless
@Path("/settings/propertyTypes")
@Api(value = "/settings/propertyTypes")
@Consumes(APPLICATION_JSON)
@Produces({APPLICATION_JSON})
public class PropertyTypesRest {

    @Inject
    private PropertyTypeService propertyTypeService;

    @GET
    @ApiOperation(value = "Gets all property types")
    public Response getAllPropertyTypes() {

        List<PropertyTypeEntity> propertyTypes = propertyTypeService.loadAll();

        List<PropertyTypeDTO> propertyTypeDTOS = propertyTypes.stream()
                .map(PropertyTypeDTO::new)
                .distinct()
                .collect(Collectors.toList());
        return Response.status(OK).entity(propertyTypeDTOS).build();
    }

    @POST
    @ApiOperation(value = "Add a property type")
    public Response addPropertyType(@ApiParam() PropertyTypeDTO request) throws ValidationException {
        if (request.getId() != null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Id must be null")).build();
        }
        PropertyTypeEntity propertyTypeEntity = new PropertyTypeEntity();
        propertyTypeEntity.setPropertyTypeName(request.getName());
        propertyTypeEntity.setEncrypt(request.isEncrypted());
        propertyTypeEntity.setValidationRegex(request.getValidationRegex());
        propertyTypeEntity.setPropertyTags(request.getPropertyTags().stream().map((propertyTagDTO) ->
                new PropertyTagEntity(propertyTagDTO.getName(),
                        PropertyTagType.valueOf(propertyTagDTO.getType())))
                .collect(Collectors.toList()));

        propertyTypeService.create(propertyTypeEntity);

        return Response.status(CREATED).build();
    }

    @PUT
    @Path("/{id : \\d+}")
    // support digit only
    @Produces("application/json")
    @ApiOperation(value = "Update a property type")
    public Response updatePropertyType(@ApiParam("Property type ID")
                                       @PathParam("id") Integer id, PropertyTypeDTO request)
            throws NotFoundException, ValidationException {

        PropertyTypeEntity propertyTypeEntity = new PropertyTypeEntity();
        propertyTypeEntity.setId(id);
        propertyTypeEntity.setPropertyTypeName(request.getName());
        propertyTypeEntity.setEncrypt(request.isEncrypted());
        propertyTypeEntity.setValidationRegex(request.getValidationRegex());
        propertyTypeEntity.setPropertyTags(request.getPropertyTags().stream()
                .map((propertyTagDTO) -> new PropertyTagEntity(propertyTagDTO.getName(), PropertyTagType.valueOf(propertyTagDTO.getType()))).collect(Collectors.toList()));


        propertyTypeService.update(id, propertyTypeEntity);

        return Response.status(OK).build();
    }

    @DELETE
    @Path("/{id : \\d+}")
    // support digit only
    @ApiOperation(value = "Remove a property type")
    public Response deletePropertyType(@ApiParam("Property type ID") @PathParam("id") Integer id)
            throws NotFoundException, ValidationException {
        propertyTypeService.deleteById(id);

        return Response.status(NO_CONTENT).build();
    }

}
