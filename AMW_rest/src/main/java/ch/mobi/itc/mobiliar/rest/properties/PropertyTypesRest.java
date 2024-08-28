package ch.mobi.itc.mobiliar.rest.properties;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyTypeDTO;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTypeService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;

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

        List<PropertyTypeEntity> propertyTypes = propertyTypeService.getPropertyTypes();
        List<PropertyTypeDTO> propertyTypeDTOS = propertyTypes.stream()
                .map(PropertyTypeDTO::new)
                .collect(Collectors.toList());
        return Response.status(OK).entity(propertyTypeDTOS).build();
    }
}
