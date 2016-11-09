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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;


public class ResourceTypeRepository {

    @Inject
    EntityManager entityManager;

    public ResourceTypeEntity loadWithFunctionsAndMiksForId(Integer id) {
        List<ResourceTypeEntity> entity =  entityManager
                .createQuery("select r from ResourceTypeEntity r " +
                        "left join fetch r.functions f " +
                        "left join fetch f.miks m " +
                        "where r.id=:resTypeId", ResourceTypeEntity.class)
                .setParameter("resTypeId", id).setMaxResults(1).getResultList();
        return entity.isEmpty() ? null : entity.get(0);
    }


    public ResourceTypeEntity findById(Integer resourceTypeId){
        return entityManager.find(ResourceTypeEntity.class, resourceTypeId);
    }

}
