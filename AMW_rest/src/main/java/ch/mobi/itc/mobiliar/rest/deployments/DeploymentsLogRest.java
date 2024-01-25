package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentCommand;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentUseCase;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.ListDeploymentLogsUseCase;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Stateless
@Path("/deployments")
@Api(value = "/deployments", description = "Deployment log files")
public class DeploymentsLogRest {

    @Inject
    private ListDeploymentLogsUseCase listDeploymentLogsUseCase;

    @Inject
    private DeploymentLogContentUseCase deploymentLogContentUseCase;

    @GET
    @Path("/{id : \\d+}/logs/{fileName}")
    @ApiOperation(value = "get the log file content as plain text for a given deployment and file name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeploymentLogFileContent(@ApiParam("Deployment ID") @PathParam("id") Integer id,
                                                @PathParam("fileName") String fileName) throws ValidationException, IOException {

        DeploymentLogContentCommand deploymentLogContentCommand = new DeploymentLogContentCommand(id, fileName);
        return Response.ok(deploymentLogContentUseCase.getContent(deploymentLogContentCommand)).build();
    }

    @GET
    @Path("/{deploymentId : \\d+}/logs")
    @ApiOperation(value = "Get the list of available log file names for a given deployment id.")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeploymentLogs(
            @PathParam("deploymentId") Integer deploymentId) throws ValidationException, NotFoundException {

        if (deploymentId == null) throw new ValidationException("deployment id must not be null");
        return Response.ok(listDeploymentLogsUseCase.logsFor(deploymentId)).build();
    }
}
