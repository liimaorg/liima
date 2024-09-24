package ch.mobi.itc.mobiliar.rest.functions;

import ch.puzzle.itc.mobiliar.business.globalfunction.boundary.GlobalFunctionsBoundary;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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
    public Response getGlobalFunctionById(@PathParam("id") int id) {
        GlobalFunctionEntity function = globalFunctionsBoundary.getFunctionById(id);
        return Response.ok(function).build();
    }

    @POST
    @ApiOperation(value = "Add a new function")
    public Response addGlobalFunction(GlobalFunctionEntity request) throws AMWException {
        globalFunctionsBoundary.saveGlobalFunction(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Remove a function")
    public Response deleteGlobalFunction(@PathParam("id") int id) {
        globalFunctionsBoundary.deleteGlobalFunction(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
