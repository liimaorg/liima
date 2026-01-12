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

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.utils.DomainObject;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import java.util.*;

public class Resource extends DomainObject<ResourceEntity> implements Comparable<Resource>, ResourceOrType {

	private DefaultResourceTypeDefinition resourceTypeDefinition;
	private ResourceTypeEntity resourceType;
	protected ContextEntity globalContext;

    Map<String, Date> tags;


	public Resource(ResourceTypeProvider resourceTypeProvider, DefaultResourceTypeDefinition resourceType, ContextEntity globalContext) {
		super(resourceTypeProvider);
		this.resourceTypeDefinition = resourceType;
		this.globalContext = globalContext;
	}

	private Resource(ResourceTypeEntity resourceType, ContextEntity globalContext) {
		super(null);
		this.resourceType = resourceType;
		this.globalContext = globalContext;
	}

	public static Resource createByResource(ResourceEntity r, ResourceTypeEntity resourceTypeEntity, ContextEntity globalContext) {
		Resource resource = new Resource(resourceTypeEntity, globalContext);
		if (r == null) {
			r = ResourceFactory.createNewResource();
		}
		resource.wrap(r);
		return resource;
	}

	public boolean isDeletable() {
		return getEntity().isDeletable();
	}

	@Override
	public DomainObject<ResourceEntity> wrap(ResourceEntity entity) {
		setEntity(entity);
		if (entity.getResourceType() == null) {
			if (resourceType != null) {
				entity.setResourceType(resourceType);
			} else {
				entity.setResourceType(resourceTypeProvider.getOrCreateDefaultResourceType(resourceTypeDefinition));
			}
		}
		return this;
	}

	public void setGlobalContext(ContextEntity globalContext){
		this.globalContext = globalContext;
	}

	public ResourceType getResourceType(){
		return resourceType != null ? ResourceType.createByResourceType(resourceType, null) : ResourceType.createByResourceType(getEntity().getResourceType(), null);
	}

	@Override
	public Integer getId() {
		if (getEntity() == null) {
			return null;
		}
		return getEntity().getId();
	}

	@Override
	public String getName() {
		return getEntity().getName();
	}

	@Override
	public void setName(String applicationName) {
		getEntity().setName(applicationName);
	}


	/**
	 * @return alle abhängigen Ressourcen ohne sich selbst und ohne Ressourcen
	 *         mit dem Typ "Applikationsgruppe"
	 */
	public List<Resource> getRelatedResources() {
		List<Resource> result = new ArrayList<Resource>();
		List<ResourceEntity> consumedResources = getEntity().getConsumedRelatedResources();

		for (ResourceEntity resource : consumedResources) {

			Resource amwresource = new Resource(resourceTypeProvider, null, null);

			amwresource.wrap(resource);
			result.add(amwresource);
		}
		return result;
	}


	@Override
	public int compareTo(Resource o) {
		if (getName() == null) {
			return -1;
		}
		if (o == null) {
			return 1;
		}
		return getEntity().getName().compareToIgnoreCase(o.getName());
	}



	public String getDisplayName() {
		if(getEntity().getResourceType().getName().equals(DefaultResourceTypeDefinition.APPLICATIONSERVER.name())){
			return getEntity().getName();
		}
		else{
			return getEntity().getName();
		}
	}

	public Map<String, Date> getTags(){
		if (tags == null) {
			Map<Date, String> tmpResult = new HashMap<Date, String>();
			for(ResourceTagEntity resTag : getEntity().getResourceTags()){
				tmpResult.put(resTag.getTagDate(), resTag.getLabel());
			}
			List<Date> list = new ArrayList<Date>(tmpResult.keySet());
			Collections.sort(list, new Comparator<Date>(){
				@Override
				public int compare(Date arg0, Date arg1) {
					if(arg0!=null && arg1!=null) {
						return arg0.after(arg1) ? -1 : 1;
					} else if(arg1==null) {
						return -1;
					} else {
						return 1;
					}

				}});
			tags = new LinkedHashMap<String, Date>();
			for (Date d : list) {
				tags.put(tmpResult.get(d), d);
			}
		}
		return tags;
	}

	@Override
	public String toString() {
		return "Resource [getEntity()=" + getEntity() + "]";
	}
}
