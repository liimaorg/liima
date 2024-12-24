package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.FunctionDTO;
import ch.puzzle.itc.mobiliar.business.function.boundary.*;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@RequestScoped
@Path("/resources/functions")
@Api(value = "/resources/functions/")
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

    @GET
    @Path("/{id : \\d+}")
    @ApiOperation(value = "Get a resource function by id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunction(@ApiParam("Function ID") @PathParam("id") Integer id) throws NotFoundException {
        AmwFunctionEntity entity = getFunctionUseCase.get(id);
        return Response.status(OK).entity(new FunctionDTO(entity)).build();
    }


    @GET
    @Path("/resource/{id : \\d+}")
    @ApiOperation(value = "Get all functions for a specific resource")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceFunctions(@ApiParam("Resource ID") @PathParam("id") Integer resourceId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResource(resourceId);
        return Response.status(OK).entity(functionsToResponse(entity)).build();
    }

    @GET
    @Path("/resourceType/{id : \\d+}")
    @ApiOperation(value = "Get all functions for a specific resourceType")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceTypeFunctions(@ApiParam("ResourceType ID") @PathParam("id") Integer resourceTypeId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResourceType(resourceTypeId);
        return Response.status(OK).entity(functionsToResponse(entity)).build();
    }


    @GET
    @Path("/{id}/revisions")
    @ApiOperation(value = "Get all revisions of a specific resource function")
    public Response getFunctionRevisions(@PathParam("id") int id) throws NotFoundException {
        List<RevisionInformation> revisions = listRevisions.getRevisions(id);
        if (revisions.isEmpty()) {
            throw new NotFoundException("No function revisions found");
        }
        return Response.ok(revisions).build();
    }

    @GET
    @Path("/{id}/revisions/{revisionId}")
    @ApiOperation(value = "Get a specific revision of a resource function")
    public Response getFunctionByIdAndRevision(@PathParam("id") int id, @PathParam("revisionId") int revisionId) throws NotFoundException {
        AmwFunctionEntity function = getFunctionRevision.getFunctionRevision(id, revisionId);
        return Response.ok(new FunctionDTO(function)).build();
    }

    @POST
    @ApiOperation(value = "Add new resource function")
    public Response addNewFunction(FunctionDTO request) {
        // TODO use set instead of array
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(request.getName(), request.getMiks().toArray(new String[0]), request.getContent());
        return Response.status(Response.Status.CREATED).entity(addFunctionUseCase.add(functionCommand)).build();

    }

    @PUT
    @ApiOperation(value = "Modify existing resource function")
    public Response modifyFunction(FunctionDTO request) {
        // TODO implement
        return Response.status(OK).build();
    }


    private Object functionsToResponse(List<AmwFunctionEntity> entity) {
        List<FunctionDTO> dtos = new ArrayList<>(entity.size());
        for (AmwFunctionEntity entityItem : entity) {
            dtos.add(new FunctionDTO(entityItem));
        }
        return dtos;
    }


}
