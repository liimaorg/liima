package ch.mobi.itc.mobiliar.rest.functions;

import ch.puzzle.itc.mobiliar.business.globalfunction.boundary.GlobalFunctionsBoundary;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
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
    @ApiOperation(value = "Add new function")
    public Response addGlobalFunction(GlobalFunctionEntity request) {
        try {
            globalFunctionsBoundary.saveGlobalFunction(request);
            return Response.status(Response.Status.CREATED).build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @ApiOperation(value = "Modify existing function")
    public Response modifyGlobalFunction(GlobalFunctionEntity request) {
        if (request.getId() == null || !globalFunctionsBoundary.isExistingId(request.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("message", "Only existing functions can be modified"))
                    .build();
        }
        try {
            globalFunctionsBoundary.saveGlobalFunction(request);
            return Response.status(Response.Status.OK).build();
        } catch (ValidationException e) {
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

    @GET
    @Path("/{id}/revisions")
    @ApiOperation(value = "Get all revisions of a specific function")
    public Response getFunctionRevisions(@PathParam("id") int id) {
        // returns empty array if nothing is found; change this behaviour
        // NotFoundException?
        List<RevisionInformation> revisions = globalFunctionsBoundary.getFunctionRevisions(id);
        return Response.ok(revisions).build();
    }
}
