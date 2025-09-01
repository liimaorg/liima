package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentCommand;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentUseCase;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.ListDeploymentLogsUseCase;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "/deployments", description = "Deployment log files")
public class DeploymentsLogRest {

    @Inject
    private ListDeploymentLogsUseCase listDeploymentLogsUseCase;

    @Inject
    private DeploymentLogContentUseCase deploymentLogContentUseCase;

    @GET
    @Path("/{id : \\d+}/logs/{fileName}")
    @Operation(summary = "get the log file content as plain text for a given deployment and file name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeploymentLogFileContent(@Parameter(description = "Deployment ID") @PathParam("id") Integer id,
                                                @PathParam("fileName") String filename) throws ValidationException, IOException {

        DeploymentLogContentCommand deploymentLogContentCommand = new DeploymentLogContentCommand(id, filename);
        return Response.ok(deploymentLogContentUseCase.getContent(deploymentLogContentCommand)).build();
    }

    @GET
    @Path("/{deploymentId : \\d+}/logs")
    @Operation(summary = "Get the list of available log file names for a given deployment id.")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeploymentLogs(
            @PathParam("deploymentId") Long deploymentId) throws ValidationException, NotFoundException {

        if (deploymentId == null) throw new ValidationException("deployment id must not be null");
        return Response.ok(listDeploymentLogsUseCase.logsFor(deploymentId)).build();
    }
}
