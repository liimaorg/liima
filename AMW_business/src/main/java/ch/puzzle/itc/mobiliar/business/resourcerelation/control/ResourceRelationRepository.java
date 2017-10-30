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

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;

public class ResourceRelationRepository {

    @Inject
    EntityManager entityManager;

    public List<ConsumedResourceRelationEntity> getResourceRelationBySlaveResourceGroupName(
            ResourceEntity masterResource, String slaveResourceGroupName) {
        return entityManager
                .createQuery(
                        "select rel from ConsumedResourceRelationEntity rel left join rel.slaveResource r left join r.resourceGroup rg where rel.masterResource=:master and LOWER(rg.name)=:name",
                        ConsumedResourceRelationEntity.class)
                .setParameter("master", masterResource).setParameter("name", slaveResourceGroupName.toLowerCase())
                .getResultList();
    }

    public List<ProvidedResourceRelationEntity> getResourceRelationOfOtherMasterResourceGroupsBySlaveResourceGroupName(
            ResourceEntity masterResource, String slaveResourceGroupName) {
        return entityManager
                .createQuery(
                        "select rel from ProvidedResourceRelationEntity rel left join rel.slaveResource r left join r.resourceGroup rg where LOWER(rg.name)=:name and rel.masterResource<>:master",
                        ProvidedResourceRelationEntity.class)
                .setParameter("master", masterResource).setParameter("name", slaveResourceGroupName.toLowerCase())
                .getResultList();
    }

    /**
     * Für JavaBatch Monitor
     * 
     * @param masterResource
     * @param slaveResource
     * @return
     */
    public ConsumedResourceRelationEntity getResourceRelation(ResourceEntity masterResource,
            ResourceEntity slaveResource) {
        return entityManager
                .createQuery(
                        "select rel from ConsumedResourceRelationEntity rel where rel.masterResource=:master and rel.slaveResource=:slave",
                        ConsumedResourceRelationEntity.class)
                .setParameter("master", masterResource).setParameter("slave", slaveResource).getSingleResult();
    }

    /**
     * Für JavaBatch Monitor: gibt Liste zurück, da mehr als 1 'StandardJob' pro App möglich
     * 
     * @param masterResource
     * @param slaveResource
     * @return
     */
    public List<ConsumedResourceRelationEntity> getResourceRelationList(ResourceEntity masterResource,
            ResourceEntity slaveResource) {
        return entityManager
                .createQuery(
                        "select rel from ConsumedResourceRelationEntity rel where rel.masterResource=:master and rel.slaveResource=:slave",
                        ConsumedResourceRelationEntity.class)
                .setParameter("master", masterResource).setParameter("slave", slaveResource).getResultList();
    }

}
