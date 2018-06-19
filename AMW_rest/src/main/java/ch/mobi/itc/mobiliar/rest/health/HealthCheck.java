package ch.mobi.itc.mobiliar.rest.health;

import ch.puzzle.itc.mobiliar.business.database.control.DatabaseConnectionTest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/")
@Api(value = "/healthCheck", description = "Health checks")
public class HealthCheck {

    @Inject
    DatabaseConnectionTest databaseConnectionTest;

    @GET
    @Path("alive")
    @ApiOperation(value = "Checks if Liima is alive")
    public Response isAlive() {
        return Response.status(OK).build();
    }

    @GET
    @Path("ready")
    @ApiOperation(value = "Checks if Liima is ready")
    public Response isReady() {
        try {
            databaseConnectionTest.testConnection();
        } catch (Throwable th) {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(OK).build();
    }

}
