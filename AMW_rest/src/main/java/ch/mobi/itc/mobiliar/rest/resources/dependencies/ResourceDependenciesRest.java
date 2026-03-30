/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2026 by Puzzle ITC
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

package ch.mobi.itc.mobiliar.rest.resources.dependencies;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceDependenciesDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceDependencyDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.GetResourceDependenciesUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources", description = "Resource Dependencies")
public class ResourceDependenciesRest {

    @Inject
    private GetResourceDependenciesUseCase getResourceDependenciesUseCase;

    @Inject
    private GetResourceUseCase getResourceUseCase;

    @GET
    @Path("/{resourceId}/resource-dependencies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all dependencies for a resource")
    public Response getResourceDependencies(
            @Parameter(description = "Resource ID") 
            @PathParam("resourceId") Integer resourceId) throws ResourceNotFoundException {
        GetResourceDependenciesCommand command = new GetResourceDependenciesCommand(resourceId);
        
        ResourceEntity resource = getResourceUseCase.getWithGroupAndRelatedResources(command.getResourceId());
        List<ConsumedResourceRelationEntity> consumedRelations = getResourceDependenciesUseCase.getConsumedRelations(resource);
        List<ProvidedResourceRelationEntity> providedRelations = getResourceDependenciesUseCase.getProvidedRelations(resource);
        
        List<ResourceDependencyDTO> consumedDTOs = consumedRelations.stream()
                .map(ResourceDependencyDTO::new)
                .collect(Collectors.toList());
        
        List<ResourceDependencyDTO> providedDTOs = providedRelations.stream()
                .map(ResourceDependencyDTO::new)
                .collect(Collectors.toList());
        
        ResourceDependenciesDTO dependencies = new ResourceDependenciesDTO(
                resource.getName(),
                consumedDTOs,
                providedDTOs
        );
        
        return Response.ok(dependencies).build();
    }
}
