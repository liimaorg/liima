package ch.mobi.itc.mobiliar.rest.apps;

import ch.mobi.itc.mobiliar.rest.dtos.AppServerDTO;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceRelations;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;


@RequestScoped
@Path("/apps")
@Api(value = "/apps", description = "Application servers and apps")
public class AppsRest {

    @Inject
    private ResourceRelations resourceRelations;

    @Inject
    private ReleaseLocator releaseLocator;


    @GET
    @ApiOperation(value = "Get applicationservers and apps", notes = "Returns all apps")
    public Response getApps(@QueryParam("start") Integer start,
                            @QueryParam("limit") Integer limit,
                            @QueryParam("appServerName") String filter,
                            @NotNull @QueryParam("releaseId") Integer releaseId) throws NotFoundException {
        if (releaseId == null) { return Response.status(BAD_REQUEST).build(); }
        ReleaseEntity release = releaseLocator.getReleaseById(releaseId);

        return appServersToResponse(resourceRelations.getAppServersWithApplications(start, limit, filter, release));
    }

    private Response appServersToResponse(List<ResourceWithRelations> apps) {
        List<AppServerDTO> appServerList = new ArrayList<>(apps.size());
        for (ResourceWithRelations app : apps) {
            appServerList.add(new AppServerDTO(app));
        }

        return Response.status(OK).entity(appServerList).build();
    }
}
