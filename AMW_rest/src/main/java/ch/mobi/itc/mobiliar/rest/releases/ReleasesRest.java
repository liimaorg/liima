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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.*;

@RequestScoped
@Path("/releases")
@Tag(name = "/releases", description = "Releases")
public class ReleasesRest {

    @Inject
    private ReleaseLocator releaseLocator;
    @Inject
    private ResourceDependencyResolverService resourceDependencyResolverService;

    @GET
    @Operation(summary = "Get releases", description = "Returns all releases")
    public List<ReleaseEntity> getReleases(@QueryParam("start") Integer start, @QueryParam("limit") Integer limit) {
        if (start == null && limit == null) {
            return releaseLocator.loadAllReleases(true);
        } else {
            return releaseLocator.loadReleasesForMgmt(start, limit, true);
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a release", description = "Returns the specifed release")
    public Response getRelease(@PathParam("id") int id) throws NotFoundException {
        ReleaseEntity release = releaseLocator.getReleaseById(id);
        return Response.ok(release).build();
    }


    @GET()
    @Path("/count")
    @Operation(summary = "Get the number of releases", description = "Returns the total amount of release entities")
    public int getCount() {
        return releaseLocator.countReleases();
    }


    @GET()
    @Path("/default")
    @Operation(summary = "Get default release", description = "Returns the default release entity")
    public ReleaseEntity getDefaultRelease() {
        return releaseLocator.getDefaultRelease();
    }

    @GET()
    @Path("/upcomingRelease")
    @Operation(summary = "Get upcoming release")
    public ReleaseEntity getUpcomingRelease() throws NotFoundException {
        List<ReleaseEntity> allReleases = releaseLocator.loadAllReleases(false);
        if (allReleases.isEmpty())
            throw new NotFoundException("No releases found");
        return resourceDependencyResolverService.findMostRelevantRelease(
                new TreeSet<ReleaseEntity>(allReleases),
                new Date());
    }

    @POST
    @Operation(summary = "Add a release")
    public Response addRelease(@Parameter() ReleaseEntity request) {
        if (request.getId() != null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Id must be null")).build();
        }
        ReleaseEntity existingRelease = releaseLocator.getReleaseByName(request.getName());
        if (existingRelease != null) {
            return Response.status(CONFLICT).entity(new ExceptionDto("Release with name " + request.getName() + " already exists")).build();
        }
        releaseLocator.create(request);
        return Response.status(CREATED).entity(request).build();
    }

    @PUT
    @Path("/{id : \\d+}")
    // support digit only
    @Produces("application/json")
    @Operation(summary = "Update a release")
    public Response updateRelease(@Parameter(description = "Release ID") @PathParam("id") Integer id, ReleaseEntity request) throws NotFoundException, ConcurrentModificationException {
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
    @Operation(summary = "Remove a release")
    public Response deleteRelease(@Parameter(description = "Release ID") @PathParam("id") Integer id) throws NotFoundException {
        ReleaseEntity release = releaseLocator.getReleaseById(id);
        if (!releaseLocator.loadResourcesAndDeploymentsForRelease(id).keySet().isEmpty()) {
            return Response.status(CONFLICT).entity(new ExceptionDto("Constraint violation. Cascade-delete is not supported. ")).build();
        }
        releaseLocator.delete(release);

        return Response.status(NO_CONTENT).build();
    }

    @GET
    @Path("/{id : \\d+}/resources")
    @Operation(summary = "Get resources of a release", description = "Returns all resources for a release by id")
    public Response getResourcesForRelease(@Parameter(description = "Release ID") @PathParam("id") Integer id) {
        return Response.status(OK)
                .entity(
                        new GenericEntity<>(releaseLocator.loadResourcesAndDeploymentsForRelease(id)) {
        }).build();
    }
}
