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

import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.CreateTagUseCase;
import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.TagConfiguration;
import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Stateless
public class TagConfigurationService implements CreateTagUseCase {

	@Inject
	private EntityManager entityManager;

	@Inject
	PermissionService permissionService;

	@Inject
	private ResourceLocator resourceLocator;

	@Override
	@HasPermission(permission = Permission.RESOURCE, action =  Action.UPDATE)
	public ResourceTagEntity createTag(TagConfiguration tagConfiguration) throws NotFoundException {
		ResourceEntity resource = resourceLocator.getResourceById(tagConfiguration.getResourceId());
		if (resource == null) {
			throw new NotFoundException("Resource not found for resource id " + tagConfiguration.getResourceId());
		}

		List<ResourceTagEntity> existingTags = this.loadTagLabelsForResource(resource);
        if (labelExists(tagConfiguration, existingTags)) {
			throw new IllegalArgumentException("Tag '" + tagConfiguration.getTag() + "' already exists for resource id " + tagConfiguration.getResourceId());
		}

		return this.tagConfiguration(tagConfiguration.getResourceId(), tagConfiguration.getTag(), tagConfiguration.getDate());
	}

	@Override
	public ResourceTagEntity tagConfiguration(int resourceId, String tag, Date date) {
		ResourceEntity currentResource = entityManager.find(ResourceEntity.class, resourceId);
		permissionService.checkPermissionAndFireException(Permission.RESOURCE, null, Action.UPDATE,
				currentResource.getResourceGroup(), null, null);
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

	@Override
	@HasPermission(permission = Permission.RESOURCE, action =  Action.READ)
	public List<ResourceTagEntity> loadTagLabelsForResource(ResourceEntity resource) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ResourceTagEntity> q = cb.createQuery(ResourceTagEntity.class);
		Root<ResourceTagEntity> r = q.from(ResourceTagEntity.class);
		q.where(cb.equal(r.get("resource"), resource));

		return entityManager.createQuery(q).getResultList();
	}

	private boolean labelExists(TagConfiguration tagConfiguration, List<ResourceTagEntity> existingTags) {
		return existingTags.stream().anyMatch(tag -> tag.getLabel().trim().equalsIgnoreCase(tagConfiguration.getTag().trim()));
	}
}
