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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ApplicationServer;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class GetResourceDependenciesService implements GetResourceDependenciesUseCase {

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private ResourceRelationService resourceRelationService;

    @Inject
    private CommonDomainService commonDomainService;

    @Override
    public List<ConsumedResourceRelationEntity> getConsumedRelations(Integer resourceId) throws ResourceNotFoundException {
        ResourceEntity resource = getResource(resourceId);
        List<ConsumedResourceRelationEntity> consumed = resourceRelationService.getConsumedSlaveRelations(resource);
        
        ApplicationServer appWithoutAsContainer = commonDomainService.createOrGetApplicationCollectorServer();
        
        List<ConsumedResourceRelationEntity> filteredRelations = new ArrayList<>();
        for (ConsumedResourceRelationEntity rel : consumed) {
            if (appWithoutAsContainer == null || !rel.getMasterResource().getId().equals(appWithoutAsContainer.getId())) {
                filteredRelations.add(rel);
            }
        }
        
        return filteredRelations;
    }

    @Override
    public List<ProvidedResourceRelationEntity> getProvidedRelations(Integer resourceId) throws ResourceNotFoundException {
        ResourceEntity resource = getResource(resourceId);
        return resourceRelationService.getProvidedSlaveRelations(resource);
    }

    @Override
    public ResourceEntity getResource(Integer resourceId) throws ResourceNotFoundException {
        ResourceEntity resource = resourceLocator.getResourceWithGroupAndRelatedResources(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource with id " + resourceId + " not found");
        }
        return resource;
    }
}
