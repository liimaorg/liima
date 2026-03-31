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

package ch.puzzle.itc.mobiliar.business.resourcerelation.control;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ListApplicationsForAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class ApplicationServerRelationService implements ListApplicationsForAppServerUseCase {

    @Inject
    ResourceLocator resourceLocator;

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = Action.READ)
    public List<ConsumedResourceRelationEntity> listApplications(@NotNull Integer resourceId)
            throws ResourceNotFoundException, ValidationException {

        ResourceEntity resource = resourceLocator.getResourceWithGroupAndRelatedResources(resourceId);
        if (resource == null) throw new ResourceNotFoundException("Resource with ID " + resourceId + " not found");
        if (!resource.getResourceType().isApplicationServerResourceType()) throw new ValidationException("Resource is not an application server");

        // Group applications by resource group to get unique applications
        Map<Integer, List<ConsumedResourceRelationEntity>> applicationsByGroup = new HashMap<>();

        for (ConsumedResourceRelationEntity relation : resource.getConsumedMasterRelations()) {
            if (DefaultResourceTypeDefinition.APPLICATION.name()
                    .equals(relation.getResourceRelationType().getResourceTypeB().getName())) {
                Integer groupId = relation.getSlaveResource().getResourceGroup().getId();
                applicationsByGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(relation);
            }
        }

        // Get best matching relation for each application group
        List<ConsumedResourceRelationEntity> uniqueApplications = new ArrayList<>();
        long currentTime = resource.getRelease() != null && resource.getRelease().getInstallationInProductionAt() != null
                ? resource.getRelease().getInstallationInProductionAt().getTime()
                : System.currentTimeMillis();

        for (List<ConsumedResourceRelationEntity> relations : applicationsByGroup.values()) {
            ConsumedResourceRelationEntity bestMatch = findBestMatchingRelation(relations, currentTime);
            if (bestMatch != null) {
                uniqueApplications.add(bestMatch);
            }
        }

        return uniqueApplications;
    }

    private ConsumedResourceRelationEntity findBestMatchingRelation(
            List<ConsumedResourceRelationEntity> relations, long currentTime) {

        ConsumedResourceRelationEntity bestMatch = findBestMatchingPastRelation(relations, currentTime);
        if (bestMatch == null) {
            bestMatch = findBestMatchingFutureRelation(relations, currentTime);
        }
        return bestMatch;
    }

    private ConsumedResourceRelationEntity findBestMatchingPastRelation(
            List<ConsumedResourceRelationEntity> relations, long currentTime) {

        ConsumedResourceRelationEntity bestMatch = null;
        for (ConsumedResourceRelationEntity relation : relations) {
            if (relation != null && relation.getSlaveResource().getRelease() != null
                    && relation.getSlaveResource().getRelease().getInstallationInProductionAt() != null) {

                long releaseTime = relation.getSlaveResource().getRelease().getInstallationInProductionAt().getTime();
                Long bestMatchTime = bestMatch != null && bestMatch.getSlaveResource().getRelease() != null
                        && bestMatch.getSlaveResource().getRelease().getInstallationInProductionAt() != null
                        ? bestMatch.getSlaveResource().getRelease().getInstallationInProductionAt().getTime()
                        : null;

                if (releaseTime <= currentTime && (bestMatchTime == null || releaseTime > bestMatchTime)) {
                    bestMatch = relation;
                }
            }
        }
        return bestMatch;
    }

    private ConsumedResourceRelationEntity findBestMatchingFutureRelation(
            List<ConsumedResourceRelationEntity> relations, long currentTime) {

        ConsumedResourceRelationEntity bestMatch = null;
        for (ConsumedResourceRelationEntity relation : relations) {
            if (relation != null && relation.getSlaveResource().getRelease() != null
                    && relation.getSlaveResource().getRelease().getInstallationInProductionAt() != null) {

                long releaseTime = relation.getSlaveResource().getRelease().getInstallationInProductionAt().getTime();
                Long bestMatchTime = bestMatch != null && bestMatch.getSlaveResource().getRelease() != null
                        && bestMatch.getSlaveResource().getRelease().getInstallationInProductionAt() != null
                        ? bestMatch.getSlaveResource().getRelease().getInstallationInProductionAt().getTime()
                        : null;

                if (releaseTime > currentTime && (bestMatchTime == null || releaseTime < bestMatchTime)) {
                    bestMatch = relation;
                }
            }
        }
        return bestMatch;
    }
}
