package ch.mobi.itc.mobiliar.rest.environments;

import ch.mobi.itc.mobiliar.rest.dtos.EnvironmentDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.control.EnvironmentsScreenDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@RequestScoped
@Path("/environments")
@Api(value = "/environments", description = "Environments")
public class EnvironmentsRest {

    @Inject
    ContextLocator contextLocator;

    @Inject
    EnvironmentsScreenDomainService environmentsScreenDomainService;

    @GET
    @ApiOperation(value = "Get environments", notes = "Returns the available environments")
    public List<EnvironmentDTO> getEnvironments(@ApiParam("Also returns Environment groups if set to true") @QueryParam("includingGroups") boolean includingGroups) {

        List<EnvironmentDTO> environments = new ArrayList<>();
        List<ContextEntity> contexts = contextLocator.getAllEnvironments();
        if (contexts != null) {
            for (ContextEntity context : contexts) {
                if (includingGroups && !context.isGlobal()) {
                    environments.add(new EnvironmentDTO(context));
                } else if (context.isEnvironment()) {
                    environments.add(new EnvironmentDTO(context));
                }
            }
        }
        return environments;
    }

    @GET
    @Path("/contexts")
    @ApiOperation(value = "Get all contexts", notes = "Returns all contexts as environments")
    public List<EnvironmentDTO> getContexts() {
        return contextLocator.getAllEnvironments().stream().map(EnvironmentDTO::new).collect(Collectors.toList());
    }

    @POST
    @Path("/contexts")
    @ApiOperation(value = "Add new context")
    public Response addContext(@ApiParam() EnvironmentDTO request) throws ElementAlreadyExistsException, ResourceNotFoundException, ValidationException {
        if(request.getName() == null || request.getName().trim().isEmpty()) throw new ValidationException("Context name must not be null or blank");
        environmentsScreenDomainService.createContextByName(request.getName(), request.getNameAlias(), request.getParentId());
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/contexts/{id : \\d+}")
    @ApiOperation(value = "Update existing context")
    public Response updateContext(@ApiParam("Environment ID") @PathParam("id") Integer id, EnvironmentDTO request) throws ResourceNotFoundException {
        environmentsScreenDomainService.saveEnvironment(id, request.getName(), request.getNameAlias());
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("contexts/{id : \\d+}")
    @ApiOperation(value = "Remove a context")
    public Response deleteContext(@PathParam("id") Integer id) throws AMWException {
        contextLocator.deleteContext(id);
        return Response.status(NO_CONTENT).build();
    }
}
