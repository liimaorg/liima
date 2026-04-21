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

import ch.mobi.itc.mobiliar.rest.dtos.ResourceRelationDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources/relations", description = "Resource relations by ID")
public class ResourceRelationsByIdRest {

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    ResourceRelationService resourceRelationService;

    @GET
    @Path("/{id : \\d+}/relations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all consumed / provided relations of a resource by ID")
    public Response getResourceRelationsById(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Optional filter by slave resource type") @QueryParam("type") String resourceType)
            throws ValidationException, ResourceNotFoundException {

        ResourceEntity resource = resourceRepository.find(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> relationsByMode =
                propertyEditor.getRelationsForResource(resourceId);

        List<ResourceRelationDTO> result = new ArrayList<>();
        result.addAll(buildConsumedRelationDtos(relationsByMode.get(ResourceEditRelation.Mode.CONSUMED),
                resource, resourceType));
        // TODO: provided relations will be added in a follow-up phase

        return Response.ok(result).build();
    }

    /**
     * Builds the list of consumed relations to display, mirroring the JSF ResourceRelationModel:
     *  - Excludes slave types APPLICATION and RUNTIME (these have their own sections in the old GUI)
     *  - Groups by (slaveGroupId, qualifiedIdentifier) and picks the best-matching release per group
     */
    private List<ResourceRelationDTO> buildConsumedRelationDtos(List<ResourceEditRelation> consumedRelations,
                                                                ResourceEntity masterResource,
                                                                String resourceTypeFilter) {
        List<ResourceRelationDTO> dtos = new ArrayList<>();
        if (consumedRelations == null) {
            return dtos;
        }

        // Group by slaveGroupId + qualifiedIdentifier (logical relation across releases)
        Map<String, List<ResourceEditRelation>> groupedRelations = new HashMap<>();
        for (ResourceEditRelation relation : consumedRelations) {
            if (shouldExcludeFromConsumedTab(relation)) {
                continue;
            }
            if (resourceTypeFilter != null && !resourceTypeFilter.equals(relation.getSlaveTypeName())) {
                continue;
            }
            String key = relation.getSlaveGroupId() + "::" + relation.getQualifiedIdentifier();
            groupedRelations.computeIfAbsent(key, k -> new ArrayList<>()).add(relation);
        }

        for (List<ResourceEditRelation> group : groupedRelations.values()) {
            ResourceEditRelation best = resourceRelationService.getBestMatchingRelationRelease(group, masterResource);
            if (best != null) {
                dtos.add(new ResourceRelationDTO(best));
            }
        }
        return dtos;
    }

    private boolean shouldExcludeFromConsumedTab(ResourceEditRelation relation) {
        String slaveTypeName = relation.getSlaveTypeName();
        return DefaultResourceTypeDefinition.APPLICATION.name().equals(slaveTypeName)
                || DefaultResourceTypeDefinition.RUNTIME.name().equals(slaveTypeName);
    }
}
