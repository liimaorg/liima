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

package ch.puzzle.itc.mobiliar.business.releasing.control;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.utils.BaseRepository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

public class ReleaseRepository extends BaseRepository<ReleaseEntity> {

    @Inject
    EntityManager entityManager;

    public List<ReleaseEntity> getReleasesForResourceGroup(ResourceGroupEntity resgrp){
        return entityManager.createQuery("select r from ReleaseEntity r left join r.resources res left join res.resourceGroup resgrp where resgrp=:resgrp", ReleaseEntity.class).setParameter("resgrp", resgrp).getResultList();
    }

    public void removeRelease(ReleaseEntity release) {
        final Integer releaseId = release.getId();
        release = getReleaseWithDeployments(releaseId);
        final Set<DeploymentEntity> deployments = release.getDeployments();
        for (DeploymentEntity deployment : deployments) {
            deployment.setExReleaseId(releaseId);
            deployment.setRelease(null);
            entityManager.merge(deployment);
        }
        entityManager.remove(release);
    }

    private ReleaseEntity getReleaseWithDeployments(Integer releaseId){
        return entityManager.createQuery("select r from ReleaseEntity r left join r.deployments where r.id =:releaseId", ReleaseEntity.class).setParameter("releaseId", releaseId).getSingleResult();
    }
}
