package ch.mobi.itc.mobiliar.rest.properties;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyTagDTO;
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
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
    @ApiOperation(value = "Get all property types")
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
    public Response addPropertyType(@NotNull(message = "Property type must not be null.") @Valid PropertyTypeDTO request) throws ValidationException {
        if (request.getId() != null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Id must be null")).build();
        }
        PropertyTypeEntity propertyTypeEntity = mapDto(request);

        PropertyTypeEntity newEntity = propertyTypeService.create(propertyTypeEntity);

        return Response.status(CREATED).entity(newEntity)
                .location(URI.create("/settings/propertyTypes/" + newEntity.getId()))
                .build();
    }

    @PUT
    @Path("/{id : \\d+}")
    // support digit only
    @Produces("application/json")
    @ApiOperation(value = "Update a property type")
    public Response updatePropertyType(@ApiParam("Property type ID")
                                       @PathParam("id") Integer id, PropertyTypeDTO request)
            throws NotFoundException, ValidationException {
        if (request == null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Property type must not be null.")).build();
        }
        PropertyTypeEntity propertyTypeEntity = mapDto(request);
        propertyTypeEntity.setId(id);

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

    private PropertyTypeEntity mapDto(PropertyTypeDTO request) throws ValidationException {

        PropertyTypeEntity propertyTypeEntity = new PropertyTypeEntity();
        propertyTypeEntity.setPropertyTypeName(request.getName());
        propertyTypeEntity.setEncrypt(request.isEncrypted());
        propertyTypeEntity.setValidationRegex(request.getValidationRegex());

            List<PropertyTagEntity> list = new ArrayList<>();
            for (PropertyTagDTO tagDTO : request.getPropertyTags()) {
                if (tagDTO.getName() == null || tagDTO.getName().isEmpty()) {
                    throw new ValidationException("PropertyTag name must not be null or empty.");
                }
                if (tagDTO.getType() == null || tagDTO.getType().isEmpty()) {
                    throw new ValidationException("PropertyTag type must not be null or empty.");
                }

                PropertyTagEntity apply = new PropertyTagEntity(tagDTO.getName(),
                        PropertyTagType.valueOf(tagDTO.getType()));
                list.add(apply);
            }
            propertyTypeEntity.setPropertyTags(list
            );

        if (checkIfRegexpSyntaxError(request.getValidationRegex())) {
            throw new ValidationException("Invalid property type validation pattern.");
        }
        return propertyTypeEntity;
    }

    private boolean checkIfRegexpSyntaxError(String regexp) {
        try {
            "".matches(regexp);
            return false;
        } catch (PatternSyntaxException e) {
            return true;
        }
    }

}
