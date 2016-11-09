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

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file,
 * choose Tools | Templates and open the template in the editor.
 */
package ch.puzzle.itc.mobiliar.business.softlinkRelation.control;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
public class SoftlinkRelationService implements Serializable{

	private static final long serialVersionUID = 1L;

	@Inject
	EntityManager entityManager;

	@Inject
	ResourceDependencyResolverService dependencyResolverService;

	@Inject
	public Logger log;

	public SoftlinkRelationEntity getSoftLinkRelationByCpiAndSoftlinkRef(ResourceEntity cpi, String softlinkRef) {
		try {
			return entityManager
                    .createQuery(
							"select s from SoftlinkRelationEntity s where s.cpiResource.name=:name and s.softlinkRef=:softlinkRef",
							SoftlinkRelationEntity.class).setParameter("name", cpi.getName())
                    .setParameter("softlinkRef", softlinkRef).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	private List<SoftlinkRelationEntity> getSoftlinksForCpi(ResourceEntity cpi){
		if(cpi.getId() != null) {
			return entityManager
					.createQuery(
							"select s from SoftlinkRelationEntity s where s.cpiResource.id=:id",
							SoftlinkRelationEntity.class).setParameter("id", cpi.getId()).getResultList();
		}
		return Collections.emptyList();
	}

    /**
     * Returns cpi resources which reference (consume) a given ppi resource
     * @param ppi
     * @return
     */
    public List<ResourceEntity> getConsumingResources(ResourceEntity ppi) {
        if(ppi.getId() != null) {
            return entityManager
                    .createQuery(
							"select s.cpiResource from SoftlinkRelationEntity s where s.softlinkRef=:softlinkRef",
							ResourceEntity.class).setParameter("softlinkRef", ppi.getSoftlinkId()).getResultList();
        }
        return Collections.emptyList();
    }

    /**
     * Returns the resource by softlinkId reference
     */
    public ResourceEntity getSoftlinkResolvableSlaveResource(String softlinkIdRef, ReleaseEntity release) {
            List<ResourceEntity> ppiList = entityManager
                    .createQuery(
                            "select r from ResourceEntity r " +
                                    "where LOWER(r.softlinkId)=:softlinkId", ResourceEntity.class).setParameter("softlinkId", softlinkIdRef.toLowerCase()).getResultList();
			return dependencyResolverService.getResourceEntityForRelease(ppiList, release);
    }


	/**
	 * Sets the softlinkrelation of a cpi resource. Deletes existing softlinkrelation.
	 * @param cpiResource
	 * @param softlinkRelation
	 */
	public void setSoftlinkRelation(ResourceEntity cpiResource, SoftlinkRelationEntity softlinkRelation) {
		List<SoftlinkRelationEntity> existing = getSoftlinksForCpi(cpiResource);
		cpiResource.clearSoftlinkRelations();
		for (SoftlinkRelationEntity sl : existing) {
			if (!sl.getId().equals(softlinkRelation.getId())) {
				// relation from cpi to ppi is one-to-one, therefore remove existing relations first
				entityManager.remove(sl);
			}
		}
		entityManager.persist(softlinkRelation);
	}

	/**
	 * Removes the softlinkrelation of a cpi resource.
	 * @param cpiResource
	 */
	public void removeSoftlinkRelation(ResourceEntity cpiResource) {
		List<SoftlinkRelationEntity> existing = getSoftlinksForCpi(cpiResource);
		cpiResource.clearSoftlinkRelations();
		for (SoftlinkRelationEntity sl : existing) {
			entityManager.remove(sl);
		}
	}


}
