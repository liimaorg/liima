package ch.mobi.itc.mobiliar.rest.servers;

import ch.puzzle.itc.mobiliar.business.server.boundary.GetServersUseCase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/servers")
@Api(value = "/servers")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ServersRest {

    @Inject
    GetServersUseCase getServersUseCase;

    @GET
    @ApiOperation("Get servers")
    public Response getServers() {
        return Response.status(OK).entity(getServersUseCase.all()).build();
    }
}
