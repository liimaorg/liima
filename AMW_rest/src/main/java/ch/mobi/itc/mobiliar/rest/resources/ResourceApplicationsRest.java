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

package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.ApplicationRelationDTO;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.AddApplicationCommand;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.AddApplicationToAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ListApplicationsForAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RemoveApplicationCommand;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RemoveApplicationFromAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("/resources/{resourceId}/applications")
@Tag(name = "/resources/{resourceId}/applications", description = "Application server applications")
public class ResourceApplicationsRest {

    @Inject
    ListApplicationsForAppServerUseCase listApplicationsUseCase;

    @Inject
    AddApplicationToAppServerUseCase addApplicationUseCase;

    @Inject
    RemoveApplicationFromAppServerUseCase removeApplicationUseCase;

    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get all applications for an application server resource")
    public Response getApplications(
            @Parameter(description = "The application server resource ID", required = true)
            @PathParam("resourceId") @NotNull Integer resourceId) 
            throws ResourceNotFoundException, ValidationException {
        
        List<ConsumedResourceRelationEntity> applications = listApplicationsUseCase.listApplications(resourceId);
        
        List<ApplicationRelationDTO> dtos = applications.stream()
                .map(ApplicationRelationDTO::from)
                .collect(Collectors.toList());
        
        return Response.ok(dtos).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Add an application to an application server")
    public Response addApplication(
            @Parameter(description = "The application server resource ID", required = true)
            @PathParam("resourceId") @NotNull Integer resourceId,
            @Parameter(description = "The application resource group ID", required = true)
            @QueryParam("applicationGroupId") @NotNull Integer applicationGroupId)
            throws ResourceNotFoundException, ValidationException, ElementAlreadyExistsException {

        AddApplicationCommand command = new AddApplicationCommand(resourceId, applicationGroupId);
        addApplicationUseCase.addApplication(command);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{relationId}")
    @Operation(summary = "Remove an application from an application server")
    public Response removeApplication(
            @Parameter(description = "The application server resource ID", required = true)
            @PathParam("resourceId") @NotNull Integer resourceId,
            @Parameter(description = "The relation ID to remove", required = true)
            @PathParam("relationId") @NotNull Integer relationId)
            throws ResourceNotFoundException, ValidationException {

        RemoveApplicationCommand command = new RemoveApplicationCommand(resourceId, relationId);
        removeApplicationUseCase.removeApplication(command);
        return Response.noContent().build();
    }
}
