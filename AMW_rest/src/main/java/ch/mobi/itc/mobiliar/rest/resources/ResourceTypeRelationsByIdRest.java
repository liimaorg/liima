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

import ch.mobi.itc.mobiliar.rest.dtos.GroupedResourceRelationsDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyExtendedDTO;
import ch.mobi.itc.mobiliar.rest.dtos.UnresolvedRelationDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.GetResourceTypeRelationPropertiesUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.GetResourceTypeRelationsUseCase;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
@Path("/resourceTypes")
@Tag(name = "/resourceTypes/relations", description = "Resource type relations by ID")
public class ResourceTypeRelationsByIdRest {

    @Inject
    GetResourceTypeRelationsUseCase getResourceTypeRelationsUseCase;

    @Inject
    GetResourceTypeRelationPropertiesUseCase getResourceTypeRelationPropertiesUseCase;

    @Inject
    ContextLocator contextLocator;

    @GET
    @Path("/{id : \\d+}/relations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get grouped relations (unresolved) of a resource type by ID")
    public Response getResourceTypeRelationsById(
            @Parameter(description = "ResourceType ID") @PathParam("id") Integer id) throws NotFoundException {

        List<ResourceEditRelation> typeRelations = getResourceTypeRelationsUseCase.getTypeRelations(id);

        List<UnresolvedRelationDTO> unresolved = new ArrayList<>();
        for (ResourceEditRelation rel : typeRelations) {
            unresolved.add(new UnresolvedRelationDTO(rel.getResRelTypeId(), rel.getSlaveTypeName(), rel.getDisplayName()));
        }
        unresolved.sort(Comparator.comparing(UnresolvedRelationDTO::getName, String.CASE_INSENSITIVE_ORDER));

        GroupedResourceRelationsDTO response = new GroupedResourceRelationsDTO();
        response.setUnresolved(unresolved);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id : \\d+}/relations/{relTypeId : \\d+}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get properties for a specific resource type relation")
    public Response getResourceTypeRelationProperties(
            @Parameter(description = "ResourceType ID") @PathParam("id") Integer resourceTypeId,
            @Parameter(description = "Relation Type ID") @PathParam("relTypeId") Integer relTypeId,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws NotFoundException {

        ContextEntity context = contextLocator.getById(contextId);
        List<ResourceEditProperty> properties =
                getResourceTypeRelationPropertiesUseCase.getPropertiesForTypeRelation(resourceTypeId, relTypeId, contextId);

        List<PropertyExtendedDTO> dtos = properties.stream()
                .map(p -> new PropertyExtendedDTO(p, context.getName(), contextId, null))
                .collect(Collectors.toList());

        return Response.ok(dtos).build();
    }
}
