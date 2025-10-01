package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.FunctionDTO;
import ch.puzzle.itc.mobiliar.business.function.boundary.*;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources/functions/")
public class ResourceFunctionsRest {

    @Inject
    private GetFunctionUseCase getFunctionUseCase;

    @Inject
    private AddFunctionUseCase addFunctionUseCase;

    @Inject
    private ListFunctionsUseCase listFunctionsUseCase;

    @Inject
    private ListFunctionRevisionsUseCase listRevisions;

    @Inject
    private GetFunctionRevisionUseCase getFunctionRevision;

    @Inject
    private UpdateFunctionUseCase updateFunctionUseCase;

    @Inject
    private OverwriteFunctionUseCase overwriteFunctionUseCase;

    @Inject
    private DeleteFunctionUseCase deleteFunctionUseCase;

    @GET
    @Path("/functions/{id : \\d+}")
    @Operation(summary = "Get a resource function by id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunction(@Parameter(description = "Function ID") @PathParam("id") Integer id) throws NotFoundException {
        AmwFunctionEntity entity = getFunctionUseCase.getFunction(id);
        return Response.ok(new FunctionDTO(entity)).build();
    }


    @GET
    @Path("/resource/{id : \\d+}/functions")
    @Operation(summary = "Get all functions for a specific resource")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceFunctions(@Parameter(description = "Resource ID") @PathParam("id") Integer resourceId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResource(resourceId);
        return Response.ok(functionsToResponse(entity)).build();
    }

    @GET
    @Path("/resourceType/{id : \\d+}/functions")
    @Operation(summary = "Get all functions for a specific resourceType")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceTypeFunctions(@Parameter(description = "ResourceType ID") @PathParam("id") Integer resourceTypeId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResourceType(resourceTypeId);
        return Response.ok(functionsToResponse(entity)).build();
    }


    @GET
    @Path("functions/{id}/revisions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all revisions of a specific resource function")
    public Response getFunctionRevisions(@PathParam("id") int id) throws NotFoundException {
        List<RevisionInformation> revisions = listRevisions.getRevisions(id);
        if (revisions.isEmpty()) {
            throw new NotFoundException("No function revisions found");
        }
        return Response.ok(revisions).build();
    }

    @GET
    @Path("functions/{id}/revisions/{revisionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a specific revision of a resource function")
    public Response getFunctionByIdAndRevision(@PathParam("id") int id, @PathParam("revisionId") int revisionId)
            throws NotFoundException {
        AmwFunctionEntity function = getFunctionRevision.getFunctionRevision(id, revisionId);
        return Response.ok(new FunctionDTO(function)).build();
    }

    @POST
    @Path("/resource/{id : \\d+}/functions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add new resource function")
    public Response addNewResourceFunction(@Parameter(description = "Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(id, request.getName(), request.getMiks(), request.getContent());
        return Response.status(CREATED).entity(addFunctionUseCase.addForResource(functionCommand)).build();
    }

    @POST
    @Path("/resourceType/{id : \\d+}/functions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add new resourceType function")
    public Response addNewResourceTypeFunction(@Parameter(description = "Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(id, request.getName(), request.getMiks(), request.getContent());
        return Response.status(CREATED).entity(addFunctionUseCase.addForResourceType(functionCommand)).build();

    }

    @PUT
    @Path("functions/{id : \\d+}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Modify existing function")
    public Response modifyFunction(@Parameter(description = "Function ID") @PathParam("id") Integer id, String content) throws ValidationException, NotFoundException {
        UpdateFunctionCommand functionCommand =
                new UpdateFunctionCommand(id, content);
        int functionId = updateFunctionUseCase.update(functionCommand);
        return Response.created(URI.create("resources/functions/" + functionId)).build();
    }

    @PUT
    @Path("/resource/{id : \\d+}/functions/overwrite")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Overwrite resource function")
    public Response overwriteResourceFunction(@Parameter(description = "Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws AMWException {
        OverwriteFunctionCommand overwriteCommand =
                new OverwriteFunctionCommand(id, request.getId(), request.getContent());
        int functionId = overwriteFunctionUseCase.overwriteForResource(overwriteCommand);
        return Response.created(URI.create("/resources/functions/" + functionId)).build();

    }

    @PUT
    @Path("/resourceType/{id : \\d+}/functions/overwrite")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Overwrite resourceType function")
    public Response overwriteResourceTypeFunction(@Parameter(description = "Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        OverwriteFunctionCommand overwriteCommand =
                new OverwriteFunctionCommand(id, request.getId(), request.getContent());
        int functionId = overwriteFunctionUseCase.overwriteForResourceType(overwriteCommand);
        return Response.created(URI.create("/resources/functions/" + functionId)).build();
    }

    @DELETE
    @Path("functions/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove a function")
    public Response deleteFunction(@PathParam("id") Integer id) throws AMWException {
        deleteFunctionUseCase.deleteFunction(id);
        return Response.status(NO_CONTENT).build();
    }


    private Object functionsToResponse(List<AmwFunctionEntity> entity) {
        List<FunctionDTO> dtos = new ArrayList<>(entity.size());
        for (AmwFunctionEntity entityItem : entity) {
            dtos.add(new FunctionDTO(entityItem));
        }
        return dtos;
    }


}
