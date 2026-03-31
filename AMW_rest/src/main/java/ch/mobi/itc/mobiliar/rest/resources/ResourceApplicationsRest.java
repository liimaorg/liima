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
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ListApplicationsForAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
}
