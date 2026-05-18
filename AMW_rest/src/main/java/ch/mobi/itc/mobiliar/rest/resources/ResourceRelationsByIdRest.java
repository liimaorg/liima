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
import ch.mobi.itc.mobiliar.rest.dtos.ResourceRelationDTO;
import ch.mobi.itc.mobiliar.rest.dtos.UnresolvedRelationDTO;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceIdCommand;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
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
import java.util.*;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources/relations", description = "Resource relations by ID")
public class ResourceRelationsByIdRest {

    @Inject
    GetResourceUseCase getResourceUseCase;

    @Inject
    ResourceEditService resourceEditService;

    @Inject
    ResourceRelationService resourceRelationService;

    @GET
    @Path("/{id : \\d+}/relations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get grouped relations (runtime, consumed, provided, unresolved) of a resource by ID")
    public Response getResourceRelationsById(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId)
            throws ValidationException, ResourceNotFoundException {

        ResourceEntity resource = getResourceUseCase.getResourceById(new ResourceIdCommand(resourceId));

        Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> relationsByMode =
                resourceEditService.loadResourceRelationsForEdit(resourceId);

        List<ResourceEditRelation> consumedRaw = relationsByMode.get(ResourceEditRelation.Mode.CONSUMED);
        List<ResourceEditRelation> providedRaw = relationsByMode.get(ResourceEditRelation.Mode.PROVIDED);
        List<ResourceEditRelation> typeRaw = relationsByMode.get(ResourceEditRelation.Mode.TYPE);

        GroupedResourceRelationsDTO response = new GroupedResourceRelationsDTO();
        response.setRuntime(buildRuntimeRelationDtos(consumedRaw, resource));
        response.setConsumed(buildConsumedRelationDtos(consumedRaw, resource));
        response.setProvided(buildProvidedRelationDtos(providedRaw, resource));
        // Unresolved relations are only shown for non-default resource types
        // (AppServer, Application, Node, Runtime are default types and always suppress this section)
        if (!isDefaultResourceType(resource)) {
            response.setUnresolved(buildUnresolvedRelationDtos(typeRaw, consumedRaw, providedRaw));
        }

        return Response.ok(response).build();
    }

    /**
     * Consumed relations excluding APPLICATION and RUNTIME slave types.
     * Groups by (slaveGroupId, qualifiedIdentifier) and picks the best-matching release per group.
     */
    private List<ResourceRelationDTO> buildConsumedRelationDtos(List<ResourceEditRelation> consumedRelations,
                                                                ResourceEntity masterResource) {
        return buildBestMatchingDtos(consumedRelations, masterResource, relation -> {
            String slaveTypeName = relation.getSlaveTypeName();
            return !DefaultResourceTypeDefinition.APPLICATION.name().equals(slaveTypeName)
                    && !DefaultResourceTypeDefinition.RUNTIME.name().equals(slaveTypeName);
        });
    }

    /**
     * Consumed relations with slave type RUNTIME.
     */
    private List<ResourceRelationDTO> buildRuntimeRelationDtos(List<ResourceEditRelation> consumedRelations,
                                                               ResourceEntity masterResource) {
        return buildBestMatchingDtos(consumedRelations, masterResource,
                relation -> DefaultResourceTypeDefinition.RUNTIME.name().equals(relation.getSlaveTypeName()));
    }

    /**
     * Provided relations (no type filtering).
     */
    private List<ResourceRelationDTO> buildProvidedRelationDtos(List<ResourceEditRelation> providedRelations,
                                                                ResourceEntity masterResource) {
        return buildBestMatchingDtos(providedRelations, masterResource, relation -> true);
    }

    /**
     * Common grouping + best-matching-release logic used by all three concrete-relation categories.
     */
    private List<ResourceRelationDTO> buildBestMatchingDtos(List<ResourceEditRelation> relations,
                                                            ResourceEntity masterResource,
                                                            java.util.function.Predicate<ResourceEditRelation> includeFilter) {
        List<ResourceRelationDTO> dtos = new ArrayList<>();
        if (relations == null) {
            return dtos;
        }

        Map<String, List<ResourceEditRelation>> grouped = new HashMap<>();
        for (ResourceEditRelation relation : relations) {
            if (!includeFilter.test(relation)) {
                continue;
            }
            String key = relation.getSlaveGroupId() + "::" + relation.getQualifiedIdentifier();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(relation);
        }

        for (List<ResourceEditRelation> group : grouped.values()) {
            ResourceEditRelation best = resourceRelationService.getBestMatchingRelationRelease(group, masterResource);
            if (best != null) {
                dtos.add(new ResourceRelationDTO(best));
            }
        }
        return dtos;
    }

    private boolean isDefaultResourceType(ResourceEntity resource) {
        return resource.getResourceType() != null
                && DefaultResourceTypeDefinition.contains(resource.getResourceType().getName());
    }

    /**
     * Type relations that have no concrete resource instance in consumed or provided
     * (mirrors ResourceRelationModel.mapUnresolvedRelations).
     */
    private List<UnresolvedRelationDTO> buildUnresolvedRelationDtos(List<ResourceEditRelation> typeRelations,
                                                                    List<ResourceEditRelation> consumedRelations,
                                                                    List<ResourceEditRelation> providedRelations) {
        List<UnresolvedRelationDTO> dtos = new ArrayList<>();
        if (typeRelations == null) {
            return dtos;
        }

        Set<Integer> resolvedTypeIds = new HashSet<>();
        if (consumedRelations != null) {
            for (ResourceEditRelation rel : consumedRelations) {
                resolvedTypeIds.add(rel.getResRelTypeId());
            }
        }
        if (providedRelations != null) {
            for (ResourceEditRelation rel : providedRelations) {
                resolvedTypeIds.add(rel.getResRelTypeId());
            }
        }

        for (ResourceEditRelation rel : typeRelations) {
            if (!resolvedTypeIds.contains(rel.getResRelTypeId())) {
                dtos.add(new UnresolvedRelationDTO(rel.getSlaveTypeName(), rel.getDisplayName()));
            }
        }
        return dtos;
    }
}
