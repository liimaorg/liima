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
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.utils.BaseRepository;


public class ResourceTypeRepository extends BaseRepository<ResourceTypeEntity> {

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

    public ResourceTypeEntity getByName(String resourceTypeName) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<ResourceTypeEntity> criteriaQuery = criteriaBuilder.createQuery(ResourceTypeEntity.class);
            Root<ResourceTypeEntity> srt = criteriaQuery.from(ResourceTypeEntity.class);
            Path<String> name = srt.get("name");
            Predicate namePredicate = criteriaBuilder.like(name, resourceTypeName);
            criteriaQuery.where(namePredicate);
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
