package ch.mobi.itc.mobiliar.rest.functions;

import ch.puzzle.itc.mobiliar.business.globalfunction.boundary.GlobalFunctionsBoundary;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@RequestScoped
@Path("/functions")
@Api(value = "/functions", description = "Functions")
public class FunctionsRest {

    @Inject
    private GlobalFunctionsBoundary globalFunctionsBoundary;

    @GET
    @ApiOperation(value = "Get all functions")
    public List<GlobalFunctionEntity> getAllGlobalFunctions() {
        return globalFunctionsBoundary.getAllGlobalFunctions();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Get a specific function")
    public Response getGlobalFunctionById(@PathParam("id") int id) throws NotFoundException {
        GlobalFunctionEntity function = globalFunctionsBoundary.getFunctionById(id);
        return Response.ok(function).build();
    }

    @POST
    @ApiOperation(value = "Add a function")
    public Response addGlobalFunction(GlobalFunctionEntity request) {
        try {
            globalFunctionsBoundary.saveGlobalFunction(request);
            return Response.status(Response.Status.CREATED).build();
        } catch (AMWException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("message", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Remove a function")
    public Response deleteGlobalFunction(@PathParam("id") int id) throws NotFoundException {
        globalFunctionsBoundary.deleteGlobalFunction(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
