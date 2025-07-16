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

import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.common.exception.ConcurrentModificationException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.*;

@RequestScoped
@Path("/releases")
@Api(value = "/releases", description = "Releases")
public class ReleasesRest {

    @Inject
    private ReleaseLocator releaseLocator;
    @Inject
    private ResourceDependencyResolverService resourceDependencyResolverService;

    @GET
    @ApiOperation(value = "Get releases", notes = "Returns all releases")
    public List<ReleaseEntity> getReleases(@QueryParam("start") Integer start, @QueryParam("limit") Integer limit) {
        if (start == null && limit == null) {
            return releaseLocator.loadAllReleases(true);
        } else {
            return releaseLocator.loadReleasesForMgmt(start, limit, true);
        }
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Get a release", notes = "Returns the specifed release")
    public Response getRelease(@PathParam("id") int id) throws NotFoundException {
        ReleaseEntity release = releaseLocator.getReleaseById(id);
        return Response.ok(release).build();
    }


    @GET()
    @Path("/count")
    @ApiOperation(value = "Get the number of releases", notes = "Returns the total amount of release entities")
    public int getCount() {
        return releaseLocator.countReleases();
    }


    @GET()
    @Path("/default")
    @ApiOperation(value = "Get default release", notes = "Returns the default release entity")
    public ReleaseEntity getDefaultRelease() {
        return releaseLocator.getDefaultRelease();
    }

    @GET()
    @Path("/upcomingRelease")
    @ApiOperation(value = "Get upcoming release")
    public ReleaseEntity getUpcomingRelease() throws NotFoundException {
        List<ReleaseEntity> allReleases = releaseLocator.loadAllReleases(false);
        if (allReleases.isEmpty())
            throw new NotFoundException("No releases found");
        return resourceDependencyResolverService.findMostRelevantRelease(
                new TreeSet<ReleaseEntity>(allReleases),
                new Date());
    }

    @POST
    @ApiOperation(value = "Add a release")
    public Response addRelease(@ApiParam() ReleaseEntity request) {
        if (request.getId() != null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Id must be null")).build();
        }
        try {
            releaseLocator.getReleaseByName(request.getName());
            return Response.status(CONFLICT).entity(new ExceptionDto("Release with name " + request.getName() + " already exists")).build();
        } catch (NoResultException e) {}
        return Response.status(CREATED).entity(request).build();
    }

    @PUT
    @Path("/{id : \\d+}")
    // support digit only
    @Produces("application/json")
    @ApiOperation(value = "Update a release")
    public Response updateRelease(@ApiParam("Release ID") @PathParam("id") Integer id, ReleaseEntity request) throws NotFoundException, ConcurrentModificationException {
        releaseLocator.getReleaseById(id);
        request.setId(id);
        if (releaseLocator.update(request)) {
            return Response.status(OK).build();
        } else {
            return Response.status(BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/{id : \\d+}")
    // support digit only
    @ApiOperation(value = "Remove a release")
    public Response deleteRelease(@ApiParam("Release ID") @PathParam("id") Integer id) throws NotFoundException {
        ReleaseEntity release = releaseLocator.getReleaseById(id);
        if (!releaseLocator.loadResourcesAndDeploymentsForRelease(id).keySet().isEmpty()) {
            return Response.status(CONFLICT).entity(new ExceptionDto("Constraint violation. Cascade-delete is not supported. ")).build();
        }
        releaseLocator.delete(release);

        return Response.status(NO_CONTENT).build();
    }

    @GET
    @Path("/{id : \\d+}/resources")
    @ApiOperation(value = "Get resources of a release", notes = "Returns all resources for a release by id")
    public Response getResourcesForRelease(@ApiParam("Release ID") @PathParam("id") Integer id) {
        return Response.status(OK)
                .entity(
                        new GenericEntity<>(releaseLocator.loadResourcesAndDeploymentsForRelease(id)) {
        }).build();
    }
}
