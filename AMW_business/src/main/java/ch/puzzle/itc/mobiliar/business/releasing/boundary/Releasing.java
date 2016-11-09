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

package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;


@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class Releasing {

    @Inject
    EntityManager entityManager;

    public List<ReleaseEntity> getNotDefinedReleasesForResource(ResourceEntity resource) {
        TypedQuery<ReleaseEntity> q = entityManager
                  .createQuery(
                            "select distinct rel from ReleaseEntity rel where rel NOT IN (select distinct rel from ResourceEntity r left join r.release rel left join r.resourceGroup rg where :resource MEMBER OF rg.resources) order by rel.installationInProductionAt asc",
                            ReleaseEntity.class);
        q.setParameter("resource", resource);
        return q.getResultList();
    }

    public List<ResourceEntity> getAllResourcesWithinResourceGroup(ResourceEntity resource) {
        TypedQuery<ResourceEntity> q = entityManager
                  .createQuery(
                            "select r from ResourceEntity r left join r.resourceGroup rg where :resource MEMBER OF rg.resources",
                            ResourceEntity.class);
        q.setParameter("resource", resource);
        return q.getResultList();
    }

    public List<ReleaseEntity> getReleaseEntities(ResourceEntity resource) {
        TypedQuery<ReleaseEntity> q = entityManager
                  .createQuery(
                            "select distinct rel from ResourceEntity r left join r.release rel left join r.resourceGroup rg where :resource MEMBER OF rg.resources order by rel.installationInProductionAt desc",
                            ReleaseEntity.class);
        q.setParameter("resource", resource);
        return q.getResultList();
    }

    public List<ResourceEntity> getAllResourcesOfGroupWithReleases(ResourceEntity resource) {
        TypedQuery<ResourceEntity> q = entityManager
                  .createQuery(
                            "select r from ResourceEntity r left join fetch r.release left join r.resourceGroup rg where :resource MEMBER OF rg.resources",
                            ResourceEntity.class);
        q.setParameter("resource", resource);
        return q.getResultList();
    }

}
