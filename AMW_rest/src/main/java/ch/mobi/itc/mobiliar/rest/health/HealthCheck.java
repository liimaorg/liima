package ch.mobi.itc.mobiliar.rest.health;

import ch.puzzle.itc.mobiliar.business.database.control.DatabaseConnectionTest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/")
@Tag(name = "/healthCheck", description = "Health checks")
public class HealthCheck {

    @Inject
    DatabaseConnectionTest databaseConnectionTest;

    @GET
    @Path("alive")
    @Operation(summary = "Checks if Liima is alive")
    public Response isAlive() {
        return Response.status(OK).build();
    }

    @GET
    @Path("ready")
    @Operation(summary = "Checks if Liima is ready")
    public Response isReady() {
        try {
            databaseConnectionTest.testConnection();
        } catch (Throwable th) {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(OK).build();
    }

}
