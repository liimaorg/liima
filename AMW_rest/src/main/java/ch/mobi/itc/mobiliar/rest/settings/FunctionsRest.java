package ch.mobi.itc.mobiliar.rest.settings;

import ch.puzzle.itc.mobiliar.business.globalfunction.boundary.GlobalFunctionsBoundary;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("settings/functions")
@Tag(name = "settings/functions", description = "Functions")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class FunctionsRest {

    @Inject
    private GlobalFunctionsBoundary globalFunctionsBoundary;

    @GET
    @Operation(summary = "Get all global functions")
    public List<GlobalFunctionEntity> getAllFunctions() {
        return globalFunctionsBoundary.getAllGlobalFunctions();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a global function by id")
    public Response getFunctionById(@PathParam("id") int id) throws NotFoundException {
        GlobalFunctionEntity function = globalFunctionsBoundary.getFunctionById(id);
        return Response.ok(function).build();
    }

    @POST
    @Operation(summary = "Add new global function")
    public Response addNewFunction(GlobalFunctionEntity request) {
        try {
            globalFunctionsBoundary.saveGlobalFunction(request);
            return Response.status(Response.Status.CREATED).build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Operation(summary = "Modify existing global function")
    public Response modifyFunction(GlobalFunctionEntity request) {
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
    @Operation(summary = "Remove a global function")
    public Response deleteFunction(@PathParam("id") int id) throws NotFoundException {
        globalFunctionsBoundary.deleteGlobalFunction(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{id}/revisions")
    @Operation(summary = "Get all revisions of a specific global function")
    public Response getFunctionRevisions(@PathParam("id") int id) throws NotFoundException {
        List<RevisionInformation> revisions = globalFunctionsBoundary.getFunctionRevisions(id);
        if (revisions.isEmpty()) {
            throw new NotFoundException("No function revisions found");
        }
        return Response.ok(revisions).build();
    }

    @GET
    @Path("/{id}/revisions/{revisionId}")
    @Operation(summary = "Get a specific revision of a global function")
    public Response getFunctionByIdAndRevision(@PathParam("id") int id, @PathParam("revisionId") int revisionId) throws NotFoundException {
        GlobalFunctionEntity function = globalFunctionsBoundary.getFunctionByIdAndRevision(id, revisionId);
        return Response.ok(function).build();
    }
}
