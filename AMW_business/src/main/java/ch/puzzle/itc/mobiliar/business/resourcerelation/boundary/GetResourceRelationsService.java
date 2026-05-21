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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceIdCommand;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;

@Stateless
public class GetResourceRelationsService implements GetResourceRelationsUseCase {

    @Inject
    GetResourceUseCase getResourceUseCase;

    @Inject
    ResourceEditService resourceEditService;

    @Inject
    ResourceRelationService resourceRelationService;

    @Override
    public GroupedRelations getGroupedRelations(Integer resourceId) throws ResourceNotFoundException {
        ResourceEntity resource = getResourceUseCase.getResourceById(new ResourceIdCommand(resourceId));

        Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> byMode =
                resourceEditService.loadResourceRelationsForEdit(resourceId);

        List<ResourceEditRelation> consumedRaw = byMode.get(ResourceEditRelation.Mode.CONSUMED);
        List<ResourceEditRelation> providedRaw = byMode.get(ResourceEditRelation.Mode.PROVIDED);
        List<ResourceEditRelation> typeRaw = byMode.get(ResourceEditRelation.Mode.TYPE);

        List<GroupedRelations.RelationGroup> runtime = bestMatching(consumedRaw, resource,
                r -> DefaultResourceTypeDefinition.RUNTIME.name().equals(r.getSlaveTypeName()));
        List<GroupedRelations.RelationGroup> consumed = bestMatching(consumedRaw, resource,
                r -> !DefaultResourceTypeDefinition.APPLICATION.name().equals(r.getSlaveTypeName())
                        && !DefaultResourceTypeDefinition.RUNTIME.name().equals(r.getSlaveTypeName()));
        List<GroupedRelations.RelationGroup> provided = bestMatching(providedRaw, resource, r -> true);
        List<ResourceEditRelation> unresolved = isDefaultResourceType(resource)
                ? Collections.emptyList()
                : buildUnresolved(typeRaw, consumedRaw, providedRaw);

        return new GroupedRelations(runtime, consumed, provided, unresolved);
    }

    private List<GroupedRelations.RelationGroup> bestMatching(List<ResourceEditRelation> relations,
                                                               ResourceEntity master,
                                                               Predicate<ResourceEditRelation> filter) {
        if (relations == null) {
            return Collections.emptyList();
        }
        Map<String, List<ResourceEditRelation>> grouped = new LinkedHashMap<>();
        for (ResourceEditRelation rel : relations) {
            if (!filter.test(rel)) {
                continue;
            }
            String key = rel.getSlaveGroupId() + "::" + rel.getQualifiedIdentifier();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(rel);
        }
        List<GroupedRelations.RelationGroup> result = new ArrayList<>();
        for (List<ResourceEditRelation> group : grouped.values()) {
            ResourceEditRelation best = resourceRelationService.getBestMatchingRelationRelease(group, master);
            if (best != null) {
                group.sort(ResourceEditRelation.releaseComparator());
                result.add(new GroupedRelations.RelationGroup(best, group));
            }
        }
        return result;
    }

    private List<ResourceEditRelation> buildUnresolved(List<ResourceEditRelation> typeRelations,
                                                        List<ResourceEditRelation> consumedRelations,
                                                        List<ResourceEditRelation> providedRelations) {
        if (typeRelations == null) {
            return Collections.emptyList();
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
        List<ResourceEditRelation> unresolved = new ArrayList<>();
        for (ResourceEditRelation rel : typeRelations) {
            if (!resolvedTypeIds.contains(rel.getResRelTypeId())) {
                unresolved.add(rel);
            }
        }
        return unresolved;
    }

    private boolean isDefaultResourceType(ResourceEntity resource) {
        return resource.getResourceType() != null
                && DefaultResourceTypeDefinition.contains(resource.getResourceType().getName());
    }
}
