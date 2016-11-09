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

package ch.puzzle.itc.mobiliar.business.configurationtag.control;

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Stateless
@Interceptors(HasPermissionInterceptor.class)
public class TagConfigurationService {

	@Inject
	private EntityManager entityManager;

	/**
	 * @param resourceId
	 * @param tag
	 * @param date
	 * @return the new created resourceTag entity
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 * @throws GeneralDBException
	 */
	@HasPermission(permission = Permission.TAG_CURRENT_STATE)
	public ResourceTagEntity tagConfiguration(int resourceId, String tag, Date date) throws ResourceNotFoundException,
			ElementAlreadyExistsException,
			GeneralDBException {
		ResourceEntity currentResource = entityManager.find(ResourceEntity.class, resourceId);
		if (currentResource.getResourceTags() == null) {
			currentResource.setResourceTags(new HashSet<ResourceTagEntity>());
		}
		ResourceTagEntity entity = new ResourceTagEntity();
		entity.setLabel(tag);
		entity.setTagDate(date);
		entity.setResource(currentResource);
		entityManager.persist(entity);
		return entity;
	}

	/**
	 * @param resource
	 * @return a list with resourceTag entities
	 */
	public List<ResourceTagEntity> loadTagLabelsForResource(ResourceEntity resource) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ResourceTagEntity> q = cb.createQuery(ResourceTagEntity.class);
		Root<ResourceTagEntity> r = q.from(ResourceTagEntity.class);
		q.where(cb.equal(r.get("resource"), resource));

		return entityManager.createQuery(q).getResultList();
	}
}
