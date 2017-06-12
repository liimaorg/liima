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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

/**
 * Use {@link ResourceTypeRepository} instead!
 * TODO Move methods to {@link ResourceTypeRepository}
 */
@Stateless
@Deprecated
public class ResourceTypeProvider {

	@Inject
	EntityManager entityManager;

	public ResourceTypeEntity getOrCreateDefaultResourceType(DefaultResourceTypeDefinition definition) {
		if (definition != null) {
			return getOrCreateResourceType(definition.name());
		}
		return null;
	}

	public ResourceTypeEntity getOrCreateResourceType(String definition) {
		if (definition != null) {
			ResourceTypeEntity type = getFromDB(definition);
			if (type == null) {
				type = new ResourceTypeEntity();
				type.setName(definition);
				entityManager.persist(type);
			}
			return type;
		}
		return null;
	}

	public ContextTypeEntity getOrCreateContextType(String contextName) {
		try {
			return entityManager.createNamedQuery(ContextTypeEntity.LOAD_CONTEXT_TYPE_BY_NAME_QUERY_NAME, ContextTypeEntity.class).setParameter("name", contextName)
					.getSingleResult();
		} catch (NoResultException e) {
			ContextTypeEntity contextType = new ContextTypeEntity();
			contextType.setName(contextName);
			entityManager.persist(contextType);
			return contextType;
		}
	}

	public ResourceRelationTypeEntity getOrCreateResourceRelationTypeIncludingParents(ResourceTypeEntity a, ResourceTypeEntity b, String identifier) {
		ResourceRelationTypeEntity result = getResourceRelationTypeIfAvailableIncludingParents(a, b, identifier);
		if (result == null) {
			result = new ResourceRelationTypeEntity();
			result.setResourceTypes(a, b);
			result.setIdentifier(identifier);
			entityManager.persist(result);
		}
		return result;
	}

	/**
	 * @param a
	 * @param b
	 * @param identifier
	 * @return
	 * @deprecated - for most cases,
	 *             {@link ResourceTypeProvider#getOrCreateResourceRelationTypeIncludingParents(ResourceTypeEntity, ResourceTypeEntity, boolean, String)}
	 *             should be appropriate.
	 */
	@Deprecated
	public ResourceRelationTypeEntity getOrCreateResourceRelationType(ResourceTypeEntity a, ResourceTypeEntity b, String identifier) {
		ResourceRelationTypeEntity result = getResourceRelationTypeIfAvailable(a, b, identifier);
		if (result == null) {
			result = new ResourceRelationTypeEntity();
			result.setResourceTypes(a, b);
			result.setIdentifier(identifier);
			entityManager.persist(result);
		}
		return result;
	}

	/**
	 * Looks up the resource relation type. This method also returns any relation fulfilling the restriction
	 * of the identifier somewhere on the resource type's parents.
	 * 
	 * @param a
	 * @param b
	 * @param identifier
	 * @return
	 */
	public ResourceRelationTypeEntity getResourceRelationTypeIfAvailableIncludingParents(ResourceTypeEntity a, ResourceTypeEntity b, String identifier) {
		ResourceTypeEntity atype = a;
		while (atype != null) {
			ResourceTypeEntity btype = b;
			while (btype != null) {
				ResourceRelationTypeEntity relation = getResourceRelationTypeIfAvailable(atype, btype, identifier);
				if (relation != null) {
					return relation;
				}
				btype = btype.getParentResourceType();
			}
			atype = atype.getParentResourceType();
		}
		return null;
	}

	public ResourceRelationTypeEntity getResourceRelationTypeIfAvailable(ResourceTypeEntity a, ResourceTypeEntity b, String identifier) {
		try {
			List<ResourceRelationTypeEntity> resourceRelations = entityManager.createQuery("from ResourceRelationTypeEntity resRel left join fetch resRel.contexts",
					ResourceRelationTypeEntity.class).getResultList();
			for (ResourceRelationTypeEntity res : resourceRelations) {
				if (res.getResourceTypeA().getId().equals(a.getId()) && res.getResourceTypeB().getId().equals(b.getId())
						&& (StringUtils.isEmpty(identifier) || identifier.equals(res.getIdentifierOrTypeBName()))) {
					return res;
				}
			}
			return null;

		} catch (NoResultException e) {
			return null;
		}
	}

	public ResourceTypeEntity getFromDB(String resourceTypeName) {
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
