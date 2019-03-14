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

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public class ResourceRelationContextRepository {

    @Inject
    EntityManager entityManager;

    public List<ResourceRelationContextEntity> getResourceRelationContextEntitiesByContextIds(ConsumedResourceRelationEntity consumedResourceRelationEntity, List<Integer> contextIds) {
        return entityManager
                .createQuery("select c from ResourceRelationContextEntity c left join fetch c.resourceActivationEntities "
                        + "where c.consumedResourceRelation.id=:resRelId and c.context.id in(:contextIds)", ResourceRelationContextEntity.class)
                .setParameter("contextIds", contextIds)
                .setParameter("resRelId", consumedResourceRelationEntity.getId()).getResultList();
    }

    public ResourceRelationContextEntity createResourceRelationContext(AbstractResourceRelationEntity consumedResourceRelationEntity, ContextEntity context) {
        ResourceRelationContextEntity resourceRelationContextEntity = new ResourceRelationContextEntity();
        resourceRelationContextEntity.setContextualizedObject(consumedResourceRelationEntity);
        resourceRelationContextEntity.setContext(context);
        entityManager.persist(resourceRelationContextEntity);
        return resourceRelationContextEntity;
    }

    public ResourceRelationContextEntity getResourceRelationContextWithResourceActivations(ConsumedResourceRelationEntity consumedResourceRelationEntity, ContextEntity context) {
        String query = "select distinct c from ResourceRelationContextEntity c left join fetch c.resourceActivationEntities "
                + "where c.consumedResourceRelation.id=:resRelId and c.context.id=:contextId";
        return getResourceRelationContext(consumedResourceRelationEntity, context, query);
    }

    public ResourceRelationContextEntity getResourceRelationContext(ConsumedResourceRelationEntity consumedResourceRelationEntity, ContextEntity context) {
        String query = "select distinct c from ResourceRelationContextEntity c where c.consumedResourceRelation.id=:resRelId and c.context.id=:contextId";
        return getResourceRelationContext(consumedResourceRelationEntity, context, query);
    }

    private ResourceRelationContextEntity getResourceRelationContext(ConsumedResourceRelationEntity consumedResourceRelationEntity, ContextEntity context, String query) {
        List<ResourceRelationContextEntity> rel = entityManager
                .createQuery(query, ResourceRelationContextEntity.class)
                .setParameter("contextId", context.getId())
                .setParameter("resRelId", consumedResourceRelationEntity.getId()).getResultList();
        switch (rel.size()) {
            case 0:
                return null;
            case 1:
                return rel.get(0);
            default:
                throw new AMWRuntimeException("The resource relation context for relation " + consumedResourceRelationEntity.getId() + " and context " + context.getId() + " is defined " + rel.size() + " times - this is a data incosistency!");
        }
    }

}
