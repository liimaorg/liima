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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted;

import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceReleaseComparator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * This service contains the logic of
 *
 */
@Stateless
public class ResourceDependencyResolverService {

    @Inject
    ResourceReleaseComparator resourceReleaseComparator;

    @Inject
    ReleaseLocator releaseLocator;

    @Inject
    ResourceGroupLocator resourceGroupLocator;

    static class ReleaseComparator implements Comparator<ReleaseEntity> {
        @Override
        public int compare(ReleaseEntity arg0, ReleaseEntity arg1) {
            if (arg0 == null || arg0.getInstallationInProductionAt() == null) {
                return arg1 == null || arg1.getInstallationInProductionAt() == null ? 0 : -1;
            }
            return arg1 == null || arg1.getInstallationInProductionAt() == null ? 1 : arg0.getInstallationInProductionAt().compareTo(arg1.getInstallationInProductionAt());
        }
    }

    public Set<ConsumedResourceRelationEntity> getConsumedMasterRelationsForRelease(ResourceEntity resource, ReleaseEntity release) {
        Set<ConsumedResourceRelationEntity> relations = resource.getConsumedMasterRelations();
        Set<ConsumedResourceRelationEntity> result = new HashSet<ConsumedResourceRelationEntity>();
        if (relations != null) {
            for (ConsumedResourceRelationEntity r : relations) {
                if (isBestResource(r.getSlaveResource(), release)) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    public Set<ProvidedResourceRelationEntity> getProvidedSlaveRelationsForRelease(ResourceEntity resource, ReleaseEntity release) {
        Set<ProvidedResourceRelationEntity> relations = resource.getProvidedSlaveRelations();
        Set<ProvidedResourceRelationEntity> result = new HashSet<ProvidedResourceRelationEntity>();
        for (ProvidedResourceRelationEntity r : relations) {
            if (isBestResource(r.getMasterResource(), release)) {
                result.add(r);
            }
        }
        return result;
    }

    public Set<ProvidedResourceRelationEntity> getProvidedMasterRelationsForRelease(ResourceEntity resource, ReleaseEntity release) {
        Set<ProvidedResourceRelationEntity> relations = resource.getProvidedMasterRelations();
        Set<ProvidedResourceRelationEntity> result = new HashSet<ProvidedResourceRelationEntity>();
        for (ProvidedResourceRelationEntity r : relations) {
            if (isBestResource(r.getSlaveResource(), release)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Returns best-matching Release. 1. Priority nearest in future 2. Priority nearest in past
     *
     * @param releases    Sorted set of Releases
     * @param currentDate
     * @return Returns ReleaseEntity
     */
    public ReleaseEntity findMostRelevantRelease(SortedSet<ReleaseEntity> releases, Date currentDate) {
        return findMostRelevantRelease(releases, currentDate, true);
    }

    /**
     * Returns best-matching Release. (nearest in past)
     *
     * @param releases    Sorted set of Releases
     * @param currentDate
     * @return Returns ReleaseEntity
     */
    public ReleaseEntity findExactOrClosestPastRelease(SortedSet<ReleaseEntity> releases, Date currentDate) {
        return findMostRelevantRelease(releases, currentDate, false);
    }

    private ReleaseEntity findMostRelevantRelease(SortedSet<ReleaseEntity> releases, Date currentDate, boolean includingFuture) {
        ReleaseEntity bestMatch = null;
        long currentTime = currentDate != null ? currentDate.getTime() : (new Date()).getTime();

        for (ReleaseEntity releaseEntity : releases) {

            long releaseInstallationTime = releaseEntity.getInstallationInProductionAt().getTime();
            Long bestMatchingReleaseTime = bestMatch != null ? bestMatch.getInstallationInProductionAt().getTime() : null;

            if (includingFuture && isBestMatchingFutureReleaseTime(bestMatchingReleaseTime, releaseInstallationTime, currentTime)) {
                bestMatch = releaseEntity;
            }
            if (isBestMatchingPastReleaseTime(bestMatchingReleaseTime, releaseInstallationTime, currentTime)) {
                bestMatch = releaseEntity;
            }
        }
        return bestMatch;
    }

    public boolean isBestMatchingPastReleaseTime(Long bestMatchingReleaseTime, long releaseInstallationTime, long currentTime) {
        boolean isMatchingPastRelease = false;

        if (releaseInstallationTime <= currentTime) {
            // past release found
            if (bestMatchingReleaseTime == null) {
                // take it, it is the only one so far
                isMatchingPastRelease = true;
            } else if ((bestMatchingReleaseTime <= currentTime) && (releaseInstallationTime >= bestMatchingReleaseTime)) {
                // take it, the existing bestMatch was an earlier date
                isMatchingPastRelease = true;
            }
        }

        return isMatchingPastRelease;
    }

    public Boolean isBestMatchingFutureReleaseTime(Long bestMatchingReleaseTime, long releaseInstallationTime, long currentTime) {
        boolean isMatchingFutureRelease = false;

        if (releaseInstallationTime >= currentTime) {
            // future release found
            if (bestMatchingReleaseTime == null) {
                // take it, it is the only one so far
                isMatchingFutureRelease = true;
            } else if (bestMatchingReleaseTime < currentTime) {
                // take it, the existing bestMatch was from past
                isMatchingFutureRelease = true;
            } else if (releaseInstallationTime < bestMatchingReleaseTime) {
                // take it, the existing bestMatch was a later date
                isMatchingFutureRelease = true;
            }
        }
        return isMatchingFutureRelease;
    }

    public ResourceEntity findMostRelevantResource(List<ResourceEntity> resources, Date relevantDate) {
        if (resources == null || relevantDate == null) {
            return null;
        }
        List<ResourceEntity> allReleaseResourcesOrderedByRelease = new ArrayList<>(resources);
        Collections.sort(allReleaseResourcesOrderedByRelease, resourceReleaseComparator);

        SortedSet<ReleaseEntity> releases = new TreeSet<>();
        for (ResourceEntity resourceEntity : allReleaseResourcesOrderedByRelease) {
            releases.add(resourceEntity.getRelease());
        }

        ReleaseEntity mostRelevantRelease = findMostRelevantRelease(releases, relevantDate);

        if (mostRelevantRelease != null) {
            for (ResourceEntity resourceEntity : allReleaseResourcesOrderedByRelease) {
                if (mostRelevantRelease.equals(resourceEntity.getRelease())) {
                    return resourceEntity;
                }
            }
        }
        return null;
    }

    /**
     * @param resources
     * @param limit
     * @return all Resources that are linked to a Release which is after or equal the given limit
     */
    public List<ResourceEntity> getAllFutureReleases(Set<ResourceEntity> resources, ReleaseEntity limit) {
        List<ResourceEntity> allReleaseResourcesOrderedByRelease = new ArrayList<>(resources);
        Collections.sort(allReleaseResourcesOrderedByRelease, resourceReleaseComparator);
        List<ResourceEntity> resourcesBefore = new ArrayList<>();
        for (ResourceEntity resourceEntity : allReleaseResourcesOrderedByRelease) {
            if (limit != null && limit.getInstallationInProductionAt() != null
                    && !limit.getInstallationInProductionAt().after(resourceEntity.getRelease().getInstallationInProductionAt())) {
                resourcesBefore.add(resourceEntity);
            }
        }
        return resourcesBefore;
    }

    /**
     * analyzes if the given resource is the best matching for the given release. returns true if so, false otherwise.
     */
    private boolean isBestResource(@NotNull ResourceEntity resource, @NotNull ReleaseEntity release) {
        return resource.equals(getResourceEntityForRelease(resource.getResourceGroup(), release));
    }


    public ResourceEntity getResourceEntityForRelease(@NotNull ResourceGroupEntity resourceGroup, @NotNull ReleaseEntity release) {
        return getResourceEntityForRelease(resourceGroup.getResources(), release);
    }

    /**
     * Used by Angular-Rest
     * @param resourceGroupId
     * @param releaseId
     * @return
     */
    public ResourceEntity getResourceEntityForRelease(@NotNull Integer resourceGroupId, @NotNull Integer releaseId) throws NotFoundException {
        ResourceGroupEntity resourceGroup = resourceGroupLocator.getResourceGroupForCreateDeploy(resourceGroupId);
        return getResourceEntityForRelease(resourceGroup.getResources(), releaseLocator.getReleaseById(releaseId));
    }

    public ResourceEntity getResourceEntityForRelease(@NotNull Collection<ResourceEntity> resources, @NotNull ReleaseEntity release) {
        ReleaseComparator comparator = new ReleaseComparator();
        ResourceEntity bestResource = null;
        for (ResourceEntity resource : resources) {
            int compareValue = comparator.compare(resource.getRelease(), release);
            //If the resource group contains a matching release, this is the one we would like to use
            if (compareValue == 0) {
                return resource;
            }
            //Otherwise, we're only interested in earlier releases than the requested one
            else if (compareValue < 0) {
                if (comparator.compare(resource.getRelease(), bestResource == null ? null : bestResource.getRelease()) > 0) {
                    //If the release date of the current resource is later than the best release we've found yet, it is better suited and is our new "best resource"
                    bestResource = resource;
                }
            }
        }
        return bestResource;
    }

    /**
     * Expects a set of resource entities which possibly contains multiple instances for one resource group.
     * Returns a subset of the given list of resource entities by extracting the best matching resource entity dependent on the given release
     *
     * @param resourceEntities
     * @param release
     * @return
     */
    public Set<ResourceEntity> getResourceEntitiesByRelease(Collection<ResourceEntity> resourceEntities, ReleaseEntity release) {
        Set<ResourceGroupEntity> handledResourceGroups = new HashSet<ResourceGroupEntity>();
        Set<ResourceEntity> result = new HashSet<ResourceEntity>();
        if (resourceEntities != null) {
            for (ResourceEntity r : resourceEntities) {
                if (!handledResourceGroups.contains(r.getResourceGroup())) {
                    ResourceEntity resourceForRelease = getResourceEntityForRelease(r.getResourceGroup(), release);
                    if (resourceForRelease != null) {
                        result.add(resourceForRelease);
                    }
                    handledResourceGroups.add(r.getResourceGroup());
                }
            }
        }
        return result;
    }


    //TODO extract logic from the resource entity and place it here
    public Set<ResourceEntity> getConsumedRelatedResourcesByResourceType(ResourceEntity resource, DefaultResourceTypeDefinition defaultResourceTypeDefinition, ReleaseEntity release) {
        List<ResourceEntity> resources = resource.getConsumedRelatedResourcesByResourceType(defaultResourceTypeDefinition);
        if (resources == null) {
            return null;
        }
        Set<ResourceEntity> result = new LinkedHashSet<ResourceEntity>();
        for (ResourceEntity r : resources) {
            ResourceEntity resourceEntityForRelease = getResourceEntityForRelease(r.getResourceGroup(), release);
            if (resourceEntityForRelease != null) {
                result.add(resourceEntityForRelease);
            }
        }
        return result;
    }
}
