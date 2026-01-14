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

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

public class ResourceFactory {
	
	/**
     * Create a new resource with given name
	 * @param name
	 * @return the created resource entity
	 */
	public static ResourceEntity createNewResource(String name){
		ResourceEntity entity = createNewResource();
		entity.setName(name);
		return entity;
	}
	
	/**
     * Create a new resource in the given resource group
	 * @param resourceGroup
	 * @return the created resource entity
	 */
	public static ResourceEntity createNewResource(ResourceGroupEntity resourceGroup){
		ResourceEntity entity = new ResourceEntity();
		entity.setResourceGroup(resourceGroup);
		entity.updateName(resourceGroup.getName());
		resourceGroup.getResources().add(entity);
		return entity;
	}

	/**
     * Create a new resource
	 * @return the created resource entity
	 */
	public static ResourceEntity createNewResource(){
		return createNewResource(new ResourceGroupEntity());
	}

}
