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

package ch.puzzle.itc.mobiliar.business.resourceactivation.boundary;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourceactivation.control.ResourceActivation;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationContextRepository;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ResourceActivationService {

    @Inject
    EntityManager entityManager;
    @Inject
    ResourceActivation resourceActivation;
    @Inject
    ResourceRelationContextRepository resourceRelationContextRepository;

    public List<ResourceActivationEntity> loadResourceActivations(Integer resourceRelationId,
              Integer contextId)
              throws ResourceNotFoundException {
        ContextEntity contextEntity = entityManager.find(ContextEntity.class, contextId);
        ConsumedResourceRelationEntity consumedResourceRelationEntity = entityManager.find(ConsumedResourceRelationEntity.class, resourceRelationId);
        return resourceActivation.getMostRelevantResourceActivationEntities(consumedResourceRelationEntity, contextEntity);
    }

    public void activateDeactivateResources(Integer resourceRelationId, Integer contextId, List<Integer> deactivationResourceGroupIds, List<Integer> activationResourceGroupIds){
        ConsumedResourceRelationEntity consumedResourceRelation = entityManager.find(ConsumedResourceRelationEntity.class, resourceRelationId);
        ContextEntity context = entityManager.find(ContextEntity.class, contextId);
        List<ResourceActivationEntity> resourceActivationEntities = resourceActivation.getMostRelevantResourceActivationEntities(
                  consumedResourceRelation, context);
        ResourceRelationContextEntity currentResourceRelationContext = resourceRelationContextRepository.getResourceRelationContextWithResourceActivations(
                  consumedResourceRelation, context);
        // If the resource relation context doesn't exist yet, we have to create it...
        if (currentResourceRelationContext == null) {
            currentResourceRelationContext = resourceRelationContextRepository.createResourceRelationContext(consumedResourceRelation, context);
        }
        Map<Integer, ResourceActivationEntity> resourceActivationIdMap = new HashMap<>();
        for(ResourceActivationEntity r : resourceActivationEntities){
            resourceActivationIdMap.put(r.getResourceGroup().getId(), r);
        }
        for (Integer deactivationResourceGroupId : deactivationResourceGroupIds) {
            ResourceGroupEntity resourceGroup = entityManager.find(ResourceGroupEntity.class, deactivationResourceGroupId);
            resourceActivation.setResourceActivation(currentResourceRelationContext, resourceGroup, resourceActivationIdMap.get(deactivationResourceGroupId), false);
        }
        for (Integer activationResourceGroupId : activationResourceGroupIds) {
            ResourceGroupEntity resourceGroup = entityManager.find(ResourceGroupEntity.class, activationResourceGroupId);
            resourceActivation.setResourceActivation(currentResourceRelationContext, resourceGroup, resourceActivationIdMap.get(activationResourceGroupId), true);
        }
    }

}
