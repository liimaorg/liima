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

package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ResourceGroupLocator {

	// TODO check permissions

	@Inject
	protected Logger log;

	@Inject
	ResourceGroupRepository resourceGroupControl;

	public List<ResourceGroupEntity> getResourceGroups() {
		return resourceGroupControl.getResourceGroups();
	}

	public ResourceGroupEntity getResourceGroupByName(String name)  {
		return resourceGroupControl.getResourceGroupByName(name);
	}

	public ResourceGroupEntity getResourceGroupById(Integer groupId)  {
		return resourceGroupControl.getResourceGroupById(groupId);
	}
	
	public ResourceGroupEntity getResourceGroupForCreateDeploy(Integer groupeId) {
		return resourceGroupControl.getResourceGroupForCreateDeploy(groupeId);
	}

		
    /**
     * @param resourceTypeName
     * @param myAmw
     * @param fetchResources determines if resources fetched
     * @return a list of Groups
     * @throws ch.puzzle.itc.mobiliar.common.exception.GeneralDBException
     */    
	public List<ResourceGroupEntity> getGroupsForType(String resourceTypeName, List<Integer> myAmw, boolean fetchResources, boolean sorted) {
		return resourceGroupControl.getGroupsForType(resourceTypeName, myAmw, fetchResources, sorted);
	}
	
	public List<ResourceGroupEntity> getGroupsForType(String resourceTypeName, List<Integer> myAmw, boolean fetchResources) {
		return resourceGroupControl.getGroupsForType(resourceTypeName, myAmw, fetchResources);
	}
	
	public List<ResourceGroupEntity> getGroupsForType(int resourceTypeId, List<Integer> myAmw, boolean fetchResources, boolean sorted) {
		return resourceGroupControl.getGroupsForType(resourceTypeId, myAmw, fetchResources, sorted);
	}
	
	public List<ResourceGroupEntity> getGroupsForType(int resourceTypeId, List<Integer> myAmw, boolean fetchResources) {
		return resourceGroupControl.getGroupsForType(resourceTypeId, myAmw, fetchResources);
	}
}
