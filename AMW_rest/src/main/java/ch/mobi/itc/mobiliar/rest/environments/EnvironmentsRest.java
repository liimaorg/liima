package ch.mobi.itc.mobiliar.rest.environments;

import ch.mobi.itc.mobiliar.rest.dtos.EnvironmentDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
@Path("/environments")
@Api(value = "/environments", description = "Environments")
public class EnvironmentsRest {

    @Inject
    ContextLocator contextLocator;

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

}
