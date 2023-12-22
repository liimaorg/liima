package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Stateless
@Path("/deployments")
@Api(value = "/deployments", description = "Deployment log files")
public class DeploymentsLogRest {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Inject
    Logger log;

    @GET
    @Path("/{id : \\d+}/logs/{fileName}")
    @ApiOperation(value = "get the log file content as plain text for a given deployment and file name")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDeploymentLogFileContent(@ApiParam("Deployment ID") @PathParam("id") Integer id, @PathParam("fileName") String fileName) {
        List<String> availableLogFiles = Arrays.asList(deploymentBoundary.getLogFileNames(id));
        if (!availableLogFiles.contains(fileName)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No logfile with name " + fileName + " for deployment with id " + id).build();
        }

        String logfileContent = "";
        try {
            logfileContent = deploymentBoundary.getDeploymentLog(fileName);
        } catch (IllegalAccessException e) {
            String msg = "error: unable to get contents of logfile " + fileName;
            log.info(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        return Response.ok(logfileContent).build();
    }

    @GET
    @Path("/{deploymentId : \\d+}/logs")
    @ApiOperation(value = "get list of log files meta data for a given deployment id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeploymentLogFileNames(@PathParam("deploymentId") Integer deploymentId) {
        String[] fileNames = deploymentBoundary.getLogFileNames(deploymentId);
        List<DeploymentLog> deploymentLogFiles = Arrays.stream(fileNames).map(fileName -> new DeploymentLog(deploymentId, fileName)).collect(
                Collectors.toList());
        return Response.ok(deploymentLogFiles).build();
    }

}
