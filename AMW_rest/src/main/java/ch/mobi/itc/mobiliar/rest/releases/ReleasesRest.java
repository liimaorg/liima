/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.mobi.itc.mobiliar.rest.releases;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestScoped
@Path("/releases")
@Api(value = "/releases", description = "Releases")
public class ReleasesRest {

    @Inject
    private PermissionBoundary permissionBoundary;
    @Inject
    private ReleaseMgmtService releaseMgmtService;
    @Inject
    private ReleaseLocator releaseLocator;

    @GET
    @ApiOperation(value = "Get releases", notes = "Returns all releases")
    public List<ReleaseEntity> getReleases() {
        return releaseMgmtService.loadAllReleases(true);
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Get a release", notes = "Returns the specifed release")
    public Response getRelease(@PathParam("id") int id) {
        ReleaseEntity release = releaseLocator.getReleaseById(id);
        if(release == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(release).build();
    }


    @GET
    @ApiOperation(value = "Get releases for management", notes = "Returns all releases for management")
    public List<ReleaseEntity> loadReleasesForMgmt(@QueryParam("start") int start, @QueryParam("limit") int limit) {
        return releaseMgmtService.loadReleasesForMgmt(start, limit, true);
    }

    @GET()
    @Path("/default")
    @ApiOperation(value = "Get default release", notes = "Returns the default release entity")
    public ReleaseEntity getDefaultRelease() {
        return releaseMgmtService.getDefaultRelease();
    }


    @GET
    @Path("/canCreateRelease")
    @ApiOperation(value = "Checks if the caller is allowed to create a release")
    public Response canCreateRelease() {
        return Response.ok(permissionBoundary.hasPermission(Permission.RELEASE, Action.CREATE)).build();
    }

    @GET
    @Path("/canUpdateRelease")
    @ApiOperation(value = "Checks if the caller is allowed to update a release")
    public Response canUpdateRelease() {
        return Response.ok(permissionBoundary.hasPermission(Permission.RELEASE, Action.UPDATE)).build();
    }

    @GET
    @Path("/canDeleteRelease")
    @ApiOperation(value = "Checks if the caller is allowed to delete a release")
    public Response canDeleteRelease() {
        return Response.ok(permissionBoundary.hasPermission(Permission.RELEASE, Action.DELETE)).build();
    }
}
