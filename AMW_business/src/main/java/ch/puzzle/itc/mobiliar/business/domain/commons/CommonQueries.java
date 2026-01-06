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

package ch.puzzle.itc.mobiliar.business.domain.commons;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;

public class CommonQueries {

	@Inject
	private EntityManager entityManager;

	protected Query fetchAllContexts() {
		return entityManager.createQuery("select n from ContextEntity n left join fetch n.children as c left join fetch n.contextType left join fetch c.contextType order by n.name asc");

	}

	public Query searchResourceByNameAndType(String applicationName, String resourceTypeName) {
		return entityManager.createQuery("select resEnt from ResourceEntity resEnt left join fetch resEnt.resourceType as resTyp where resEnt.name=:applicationName and resTyp.name=:resourceTypeName").setParameter("applicationName", applicationName).setParameter("resourceTypeName", resourceTypeName);
	}

	public Query fetchAllRuntimes() {
		return entityManager.createNamedQuery(ResourceGroupEntity.ALLRESOURCESBYTYPE_QUERY, ResourceGroupEntity.class).setParameter("restype", DefaultResourceTypeDefinition.RUNTIME.name());
	}

	public Query fetchAllNodes() {
		return entityManager.createQuery("select distinct r from ResourceEntity r left join fetch r.resourceType rt left join fetch r.resourceGroup rg left join fetch rg.resources where rt.name like :name order by r.name asc").setParameter("name",
				DefaultResourceTypeDefinition.NODE.name());

	}

	public Query searchPropertyTypeByName(String ptyTypeName) {

		Query query = entityManager.createQuery("select propType from PropertyTypeEntity propType where propType.propertyTypeName=:propertyTypeName")
				.setParameter("propertyTypeName", ptyTypeName);
		return query;
	}

	public Query searchPropertyDescriptorByName(String propertyName) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<PropertyDescriptorEntity> cq = cb.createQuery(PropertyDescriptorEntity.class);
		Root<PropertyDescriptorEntity> root = cq.from(PropertyDescriptorEntity.class);
		cq.where(cb.equal(root.get("propertyName"), propertyName));

		return entityManager.createQuery(cq);
	}
}
