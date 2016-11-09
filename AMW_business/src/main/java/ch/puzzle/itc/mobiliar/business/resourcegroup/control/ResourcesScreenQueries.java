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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ResourcesScreenQueries {
	
	@Inject
	private EntityManager entityManager;
	
	public Query searchResourceTypeByName(String resourceTypeName) {

		Query query = entityManager.createQuery("select resType from ResourceTypeEntity resType where lower(resType.name)=:resourceTypeName").setParameter("resourceTypeName", resourceTypeName.toLowerCase());
		
		return query;
	}
	
	public Query searchResourceByName(String resourceName) {
		Query query = entityManager.createQuery("from ResourceEntity as res where res.name like '" + resourceName + "'");
		
		return query;
	}

	public Query searchResourceBySoftlinkIdAndHasNotResourceGroupId(String softlinkId, Integer resourceGroupId){
		Query query = null;
		if(resourceGroupId != null){
			query = entityManager.createQuery("FROM ResourceEntity r where LOWER(r.softlinkId)=:softlinkId AND RESOURCEGROUP_ID!=:resourceGroupId");
			query.setParameter("softlinkId", softlinkId.toLowerCase()).setParameter("resourceGroupId", resourceGroupId);
		}else{
			query = entityManager.createQuery("FROM ResourceEntity r where LOWER(r.softlinkId)=:softlinkId");
			query.setParameter("softlinkId", softlinkId.toLowerCase());
		}
		return query;
	}

}
