package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.FunctionDTO;
import ch.puzzle.itc.mobiliar.business.function.boundary.GetFunctionUseCase;
import ch.puzzle.itc.mobiliar.business.function.boundary.ListFunctionsUseCase;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@RequestScoped
@Path("/resources/functions")
@Api(value = "/resources/functions/", description = "Resource functions")
public class ResourceFunctionsRest {

    @Inject
    private GetFunctionUseCase getFunctionUseCase;

    @Inject
    private ListFunctionsUseCase listFunctionsUseCase;

    @GET
    @Path("/{id : \\d+}")
    @ApiOperation(value = "Get a resource function by id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunction(@ApiParam("Function ID") @PathParam("id") Integer id) throws NotFoundException {
        AmwFunctionEntity entity = getFunctionUseCase.get(id);
        return Response.status(OK).entity(new FunctionDTO(entity)).build();
    }


    @GET
    @Path("/{resourceId : \\d+}")
    @ApiOperation(value = "Get all functions for a specific resource")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceFunctions(@ApiParam("Resource ID") @PathParam("resourceId") Integer resourceId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResource(resourceId);
        return Response.status(OK).entity(functionsToResponse(entity)).build();
    }

    @GET
    @Path("/{resourceTypeId : \\d+}")
    @ApiOperation(value = "Get all functions for a specific resourceType")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceTypeFunctions(@ApiParam("ResourceType ID") @PathParam("resourceTypeId") Integer resourceTypeId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResourceType(resourceTypeId);
        return Response.status(OK).entity(functionsToResponse(entity)).build();
    }


    private Object functionsToResponse(List<AmwFunctionEntity> entity) {
        List<FunctionDTO> dtos = new ArrayList<>(entity.size());
        for (AmwFunctionEntity entityItem : entity) {
            dtos.add(new FunctionDTO(entityItem));
        }
        return dtos;
    }




}
