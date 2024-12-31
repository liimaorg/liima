package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.FunctionDTO;
import ch.puzzle.itc.mobiliar.business.function.boundary.*;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
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
@Path("/resources")
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

    @Inject
    private UpdateFunctionUseCase updateFunctionUseCase;

    @Inject
    private OverwriteFunctionUseCase overwriteFunctionUseCase;

    @GET
    @Path("/functions/{id : \\d+}")
    @ApiOperation(value = "Get a resource function by id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunction(@ApiParam("Function ID") @PathParam("id") Integer id) throws NotFoundException {
        AmwFunctionEntity entity = getFunctionUseCase.getFunction(id);
        return Response.status(OK).entity(new FunctionDTO(entity)).build();
    }


    @GET
    @Path("/resource/{id : \\d+}/functions")
    @ApiOperation(value = "Get all functions for a specific resource")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceFunctions(@ApiParam("Resource ID") @PathParam("id") Integer resourceId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResource(resourceId);
        return Response.status(OK).entity(functionsToResponse(entity)).build();
    }

    @GET
    @Path("/resourceType/{id : \\d+}/functions")
    @ApiOperation(value = "Get all functions for a specific resourceType")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceTypeFunctions(@ApiParam("ResourceType ID") @PathParam("id") Integer resourceTypeId) throws NotFoundException {
        List<AmwFunctionEntity> entity = listFunctionsUseCase.functionsForResourceType(resourceTypeId);
        return Response.status(OK).entity(functionsToResponse(entity)).build();
    }


    @GET
    @Path("functions/{id}/revisions")
    @ApiOperation(value = "Get all revisions of a specific resource function")
    public Response getFunctionRevisions(@PathParam("id") int id) throws NotFoundException {
        List<RevisionInformation> revisions = listRevisions.getRevisions(id);
        if (revisions.isEmpty()) {
            throw new NotFoundException("No function revisions found");
        }
        return Response.ok(revisions).build();
    }

    @GET
    @Path("functions/{id}/revisions/{revisionId}")
    @ApiOperation(value = "Get a specific revision of a resource function")
    public Response getFunctionByIdAndRevision(@PathParam("id") int id, @PathParam("revisionId") int revisionId)
            throws NotFoundException {
        AmwFunctionEntity function = getFunctionRevision.getFunctionRevision(id, revisionId);
        return Response.ok(new FunctionDTO(function)).build();
    }

    @POST
    @Path("/resource/{id : \\d+}/functions")
    @ApiOperation(value = "Add new resource function")
    public Response addNewResourceFunction(@ApiParam("Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(id, request.getName(), request.getMiks(), request.getContent());
        return Response.status(Response.Status.CREATED).entity(addFunctionUseCase.addForResource(functionCommand)).build();

    }

    @POST
    @Path("/resourceType/{id : \\d+}/functions")
    @ApiOperation(value = "Add new resourceType function")
    public Response addNewResourceTypeFunction(@ApiParam("Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(id, request.getName(), request.getMiks(), request.getContent());
        return Response.status(Response.Status.CREATED).entity(addFunctionUseCase.addForResourceType(functionCommand)).build();
    }

    @PUT
    @Path("functions/{id : \\d+}")
    @ApiOperation(value = "Modify existing function")
    public Response modifyFunction(@ApiParam("Function ID") @PathParam("id") Integer id, String content) throws ValidationException, NotFoundException {
        UpdateFunctionCommand functionCommand =
                new UpdateFunctionCommand(id, content);
        updateFunctionUseCase.update(functionCommand);
        return Response.status(OK).build();
    }

    @POST
    @Path("/resource/{id : \\d+}/functions/overwrite")
    @ApiOperation(value = "Overwrite resource function")
    public Response overwriteResourceFunction(@ApiParam("Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(id, request.getName(), request.getMiks(), request.getContent());
        return Response.status(Response.Status.CREATED).entity(addFunctionUseCase.addForResource(functionCommand)).build();

    }

    @POST
    @Path("/resourceType/{id : \\d+}/functions/overwrite")
    @ApiOperation(value = "Overwrite resourceType function")
    public Response overwriteResourceTypeFunction(@ApiParam("Resource ID") @PathParam("id") Integer id, FunctionDTO request)
            throws ValidationException, NotFoundException {
        AddFunctionCommand functionCommand =
                new AddFunctionCommand(id, request.getName(), request.getMiks(), request.getContent());
        return Response.status(Response.Status.CREATED).entity(addFunctionUseCase.addForResourceType(functionCommand)).build();
    }


    private Object functionsToResponse(List<AmwFunctionEntity> entity) {
        List<FunctionDTO> dtos = new ArrayList<>(entity.size());
        for (AmwFunctionEntity entityItem : entity) {
            dtos.add(new FunctionDTO(entityItem));
        }
        return dtos;
    }


}
