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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResourceGroupRepository {

    @Inject
    EntityManager entityManager;

    public List<ResourceGroupEntity> getResourceGroups(){
        return entityManager.createQuery("select r from ResourceGroupEntity r left join fetch r.resources res", ResourceGroupEntity.class).getResultList();
    }

    public ResourceGroupEntity getResourceGroupByName(String name){
        name = name.toLowerCase();
        return entityManager.createQuery("select r from ResourceGroupEntity r where LOWER(r.name)=:name", ResourceGroupEntity.class).setParameter(
                "name", name).getSingleResult();
    }

    public ResourceGroupEntity getResourceGroupById(Integer groupId){
        return entityManager.createQuery("select r from ResourceGroupEntity r where r.id=:groupId", ResourceGroupEntity.class).setParameter(
                "groupId", groupId).getSingleResult();
    }
    
    /**
     * Fetches the resourceGroup as required for the create deployment popup of the deploy screen
     */
	public ResourceGroupEntity getResourceGroupForCreateDeploy(Integer groupId) {
		TypedQuery<ResourceGroupEntity> q = entityManager.createQuery("select r from ResourceGroupEntity r"
        		+ " left join fetch r.resources res"
        		+ " left join fetch res.consumedMasterRelations"
        		+ " left join fetch res.resourceTags"
        		+ " where r.id=:id",
        		ResourceGroupEntity.class);
		q.setParameter("id", groupId);
		return q.getSingleResult();
	}
	
    /**
     * @param resourceTypeName
     * @param myAmw
     * @param fetchResources determines if resources fetched
     * @return a list of Groups
     * @throws ch.puzzle.itc.mobiliar.common.exception.GeneralDBException
     */    
	public List<ResourceGroupEntity> getGroupsForType(String resourceTypeName, List<Integer> myAmw, boolean fetchResources, boolean sorted) {
		return getGroupsForType("name", resourceTypeName, myAmw, fetchResources, sorted);
	}
	
	public List<ResourceGroupEntity> getGroupsForType(String resourceTypeName, List<Integer> myAmw, boolean fetchResources) {
		return getGroupsForType("name", resourceTypeName, myAmw, fetchResources, false);
	}
	
	public List<ResourceGroupEntity> getGroupsForType(int resourceTypeId, List<Integer> myAmw, boolean fetchResources, boolean sorted) {
		return getGroupsForType("id", resourceTypeId, myAmw, fetchResources, sorted);
	}
	
	public List<ResourceGroupEntity> getGroupsForType(int resourceTypeId, List<Integer> myAmw, boolean fetchResources) {
		return getGroupsForType("id", resourceTypeId, myAmw, fetchResources, false);
	}

	
	private List<ResourceGroupEntity> getGroupsForType(String typeParam, Object typeParamValue, List<Integer> myAmw, boolean fetchResources, boolean sorted) {
        StringBuilder qString = new StringBuilder();
        String fetchString = StringUtils.EMPTY;
        List<ResourceGroupEntity> result;
        
        if(fetchResources) {
        	fetchString = "fetch ";
        }

        qString.append("SELECT DISTINCT g FROM ResourceGroupEntity g left join " + fetchString + "g.resources r");
        qString.append(" WHERE r.resourceType."+typeParam+"=:typePram");
        qString.append(" AND r.name IS NOT :asContainer");
        if (!CollectionUtils.isEmpty(myAmw)) {
            qString.append(" AND g.id in (:myAmw)");
        }

        TypedQuery<ResourceGroupEntity> q = entityManager.createQuery(qString.toString(),
                ResourceGroupEntity.class);
        q.setParameter("typePram", typeParamValue);
        q.setParameter("asContainer", ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName());
        if (!CollectionUtils.isEmpty(myAmw)) {
            q.setParameter("myAmw", myAmw);
        }
        
        result = q.getResultList();
        
        if(sorted) {
	        Collections.sort(result, new Comparator<ResourceGroupEntity>() {
				@Override
				public int compare(ResourceGroupEntity group1, ResourceGroupEntity group2) {
					return group1.getName().toLowerCase().
							compareTo(group2.getName().toLowerCase());
				}
			});
        }
        return result;

	}

    public ResourceGroupEntity find(Integer resourceGroupId) {
        return entityManager.find(ResourceGroupEntity.class, resourceGroupId);
    }

}
