package ch.mobi.itc.mobiliar.rest.environments;

import ch.mobi.itc.mobiliar.rest.dtos.EnvironmentDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.control.EnvironmentsScreenDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.*;

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
    public List<EnvironmentDTO> getEnvironments(@ApiParam("Also returns Environment groups if set to true") @QueryParam("includingGroups")
                                                            boolean includingGroups) {

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
    @ApiOperation(value = "Add a context")
    public Response addContext(@ApiParam() EnvironmentDTO request) throws ElementAlreadyExistsException, ResourceNotFoundException {
        //TODO: EnvironmentsScreenDomainService is deprecated.
        ContextEntity newContextEntity = environmentsScreenDomainService.createContextByName(request.getName(), request.getParentId());
        return Response.status(CREATED)
                .location(URI.create("/settings/environments/contexts" + newContextEntity.getId()))
                .build();
    }
}
