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

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;

public class ResourceFactory {
	
	/**
     * Set default AMW owner
	 * @param name
	 * @return the created resource entity
	 */
	public static ResourceEntity createNewResource(String name){
		return createNewResourceForOwner(name, ForeignableOwner.getSystemOwner());
	}
	
	/**
     * Set default AMW owner
	 * @param resourceGroup
	 * @return the created resource entity
	 */
	public static ResourceEntity createNewResource(ResourceGroupEntity resourceGroup){
		return createNewResourceForOwner(resourceGroup, ForeignableOwner.getSystemOwner());
	}
	
	
	/**
     * Set default AMW owner
	 * @return the created resource entity
	 */
	public static ResourceEntity createNewResource(){
		return createNewResourceForOwner(ForeignableOwner.getSystemOwner());
	}

    /**
     * @return the created resource entity for owner
     */
    public static ResourceEntity createNewResourceForOwner(String name, ForeignableOwner owner){
        ResourceEntity entity = createNewResourceForOwner(owner);
        entity.setName(name);
        return entity;
    }

    /**
     * @param resourceGroup
     * @return the created resource entity for owner
     */
    public static ResourceEntity createNewResourceForOwner(ResourceGroupEntity resourceGroup, ForeignableOwner owner){
        ResourceEntity entity = new ResourceEntity(owner);
        entity.setResourceGroup(resourceGroup);
        entity.updateName(resourceGroup.getName());
        resourceGroup.getResources().add(entity);
        return entity;
    }


    /**
     * @return the created resource entity for owner
     */
    public static ResourceEntity createNewResourceForOwner(ForeignableOwner owner){
        return createNewResourceForOwner(new ResourceGroupEntity(), owner);
    }


}
