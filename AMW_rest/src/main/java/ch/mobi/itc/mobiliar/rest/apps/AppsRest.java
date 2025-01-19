package ch.mobi.itc.mobiliar.rest.apps;

import ch.mobi.itc.mobiliar.rest.dtos.AppAppServerDTO;
import ch.mobi.itc.mobiliar.rest.dtos.AppServerDTO;
import ch.puzzle.itc.mobiliar.business.apps.boundary.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;


@RequestScoped
@Path("/apps")
@Api(value = "/apps", description = "Application servers and apps")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class AppsRest {

    @Inject
    private ListAppsUseCase listAppsUseCase;


    @Inject
    private AddAppUseCase addAppUseCase;

    @Inject
    private AddAppWithServerUseCase addAppWithServerUseCase;

    @Inject
    private AddAppServerUseCase addAppServerUseCase;


    @GET
    @ApiOperation(value = "Get applicationservers and apps", notes = "Returns all apps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApps(@QueryParam("start") Integer start,
                            @QueryParam("limit") Integer limit,
                            @QueryParam("appServerName") String filter,
                            @NotNull @QueryParam("releaseId") Integer releaseId) throws NotFoundException {
        Tuple<List<ResourceWithRelations>, Long> result = listAppsUseCase.appsFor(start, limit, filter, releaseId);

        return Response.status(OK).entity(appServersToResponse(result.getA())).header("X-total-count", result.getB()).build();
    }

    private List<AppServerDTO> appServersToResponse(List<ResourceWithRelations> apps) {
        List<AppServerDTO> appServerList = new ArrayList<>(apps.size());
        for (ResourceWithRelations app : apps) {
            appServerList.add(new AppServerDTO(app));
        }
        return appServerList;
    }

    @POST
    @ApiOperation(value = "Add a application")
    public Response addApp(@NotNull @QueryParam("appName") String appName, @NotNull @QueryParam("releaseId") Integer releaseId) throws NotFoundException, IllegalArgumentException, IllegalStateException {
        AddAppCommand addAppCommand = new AddAppCommand(appName, releaseId);
        return Response.status(CREATED).entity(addAppUseCase.add(addAppCommand)).build();
    }

    @Path("/appServer")
    @POST
    @ApiOperation(value = "Add a applicationserver")
    public Response addAppServer(@NotNull @QueryParam("appServerName") String name, @NotNull @QueryParam("releaseId") Integer releaseId) throws NotFoundException, IllegalArgumentException, IllegalStateException {
        AppServerCommand appServerCommand = new AppServerCommand(name, releaseId);
        return Response.status(CREATED).entity(addAppServerUseCase.add(appServerCommand)).build();
    }


    @Path("/appWithServer")
    @POST
    @ApiOperation(value = "Add a application with appServer")
    public Response addAppWithAppServer(@NotNull @ApiParam() AppAppServerDTO app) throws NotFoundException, IllegalArgumentException, IllegalStateException {
        AddAppWithServerCommand addAppWithServerCommand =
                new AddAppWithServerCommand(app.getAppName(), app.getAppReleaseId(), app.getAppServerId(), app.getAppServerReleaseId());
        return Response.status(CREATED).entity(addAppWithServerUseCase.add(addAppWithServerCommand)).build();
    }
}
