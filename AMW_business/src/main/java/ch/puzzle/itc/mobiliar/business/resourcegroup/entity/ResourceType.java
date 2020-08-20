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

import ch.puzzle.itc.mobiliar.business.utils.DomainObject;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ResourceType domain object
 */
public class ResourceType extends DomainObject<ResourceTypeEntity>
		implements Comparable<ResourceType>, ResourceOrType {
	private String relResTypeIdentifier;

	public ResourceType() {
		super(null);
	}

	@Override
	public DomainObject<ResourceTypeEntity> wrap(ResourceTypeEntity entity) {
		setEntity(entity);
		setRelResTypeIdentifier(entity.getName());
		return this;
	}

	@Override
	public String getName(){
		return getEntity().getName();
	}

	@Override
	public void setName(String name) {
		getEntity().setName(name);
	}

	@Override
	public Integer getId(){
		return getEntity().getId();
	}

	/**
	 * @return true if type is one of {@link DefaultResourceTypeDefinition},
	 *         false otherwise
	 */
	public boolean isDefaultResourceType() {
		return getEntity().isDefaultResourceType();
	}

	/**
	 * @return true if type is
	 *         {@link DefaultResourceTypeDefinition#APPLICATION}
	 */
	public boolean isApplicationResourceType(){
		return getEntity().isApplicationResourceType();
	}

	/**
	 * @return true if type is {@link DefaultResourceTypeDefinition#APPLICATIONSERVER}
	 */
	public boolean isApplicationServerResourceType() {
		return getEntity().isApplicationServerResourceType();
	}

	/**
	 * @return true if type is {@link DefaultResourceTypeDefinition#NODE}
	 */
	public boolean isNodeResourceType() {
		return getEntity().isNodeResourceType();
	}

	/**
	 * @return list of resourceType children
	 */
	public List<ResourceType> getChildren(){
		List<ResourceType> result = new ArrayList<ResourceType>();
		for(ResourceTypeEntity t : getEntity().getChildrenResourceTypes()){
			ResourceType type = new ResourceType();
			type.wrap(t);
			result.add(type);
		}
		Collections.sort(result, new Comparator<ResourceType>() {

			@Override
			public int compare(ResourceType o1, ResourceType o2) {
				if(o1==null) {
					return -1;
				}
				if(o2==null) {
					return 1;
				}
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return result;
	}

	/**
	 * @param r
	 * @return created ResourceType
	 */
	public static ResourceType createByResourceType(ResourceTypeEntity r, String relResTypeIdentifier) {
		ResourceType resCat = new ResourceType();
		if (r == null){
			r = new ResourceTypeEntity();
		}
		resCat.wrap(r);
		if (!StringUtils.isBlank(relResTypeIdentifier)) {
			resCat.setRelResTypeIdentifier(relResTypeIdentifier);
		}
		return resCat;
	}

	/**
	 * @param name
	 * @param globalContext
	 * @return created ResourceType
	 */
	public static ResourceType createByName(String name, ContextEntity globalContext) {
		ResourceType resCat = createByResourceType(null, null);
		resCat.setName(name);
		return resCat;
	}

	@Override
	public int compareTo(ResourceType o) {
		if (relResTypeIdentifier == null) {
			return -1;
		}
		if (o == null) {
			return 1;
		}
		return relResTypeIdentifier.compareToIgnoreCase(o.getRelResTypeIdentifier());
	}

	public String getRelResTypeIdentifier() {
		return relResTypeIdentifier;
	}

	public void setRelResTypeIdentifier(String relResTypeIdentifier) {
		this.relResTypeIdentifier = relResTypeIdentifier;
	}

	@Override
	public String toString() {
		return "ResourceType [id=" + getEntity().getId() + " name=" + getEntity().getName() + "]";
	}
}
