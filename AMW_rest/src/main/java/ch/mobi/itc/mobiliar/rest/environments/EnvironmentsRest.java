package ch.mobi.itc.mobiliar.rest.environments;

import ch.mobi.itc.mobiliar.rest.dtos.EnvironmentDTO;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/environments")
@Api(value = "/environments", description = "Environments")
public class EnvironmentsRest {

    @Inject
    ContextDomainService contextDomainService;

    @GET
    @ApiOperation(value = "Get environments", notes = "Returns the available environments")
    public List<EnvironmentDTO> getEnvironments() {

        List<EnvironmentDTO> environments = new ArrayList<>();
        List<ContextEntity> contexts = contextDomainService.getEnvironments();
        if (contexts != null) {
            for (ContextEntity context : contexts) {
                if (context.isEnvironment()) {
                    environments.add(new EnvironmentDTO(context));
                }
            }
        }
        return environments;
    }

}
