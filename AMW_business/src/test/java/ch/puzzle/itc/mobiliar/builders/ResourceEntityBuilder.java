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

package ch.puzzle.itc.mobiliar.builders;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Builds {@link ResourceEntityBuilder} for testing.
 * 
 * @author cweber
 */
public class ResourceEntityBuilder extends BaseEntityBuilder {

	private ResourceGroupEntityBuilder groupEntityBuilder = new ResourceGroupEntityBuilder();

	private ResourceTypeEntityBuilder resourceTypeEntityBuilder = new ResourceTypeEntityBuilder();

	private String name;
	private Integer id;
	private ResourceTypeEntity type;
	private String resourceTypeName;
	private ResourceGroupEntity group;
	private ReleaseEntity release;
    private ForeignableOwner owner;
	private Set<AmwFunctionEntity> functions = new HashSet<>();
	private boolean isDeletable;
	private String softlinkId;
	private SoftlinkRelationEntity softlinkRelation;
	private Set<ResourceContextEntity> contexts;
	private String localPortId;

	/**
	 * @param name
	 *             if group is not null and groupName is set the groupname will be used. If both groupName
	 *             and name are empty a default name will be created
	 * @param group
	 *             if group is null a group will be created
	 * @param type
	 *             resource will be added to type resourcelist
	 * @param release
	 * @return mock of a resource entity with the given values.
	 */
	public ResourceEntity mockResourceEntity(String name, ResourceGroupEntity group, ResourceTypeEntity type, ReleaseEntity release, ResourceGroupEntity runtime) {
		ResourceEntity mock = Mockito.mock(ResourceEntity.class);
		int id = getNextId();
		when(mock.getId()).thenReturn(id);
		if (group != null && !StringUtils.isEmpty(group.getName())) {
			when(mock.getName()).thenReturn(group.getName());
		} else if (StringUtils.isEmpty(name)) {
			when(mock.getName()).thenReturn(name);
		} else {
			when(mock.getName()).thenReturn("resourceEntity" + id);
		}

		if (group != null) {
			when(mock.getResourceGroup()).thenReturn(group);
		} else {
			Set<ResourceEntity> resources = new HashSet<ResourceEntity>();
			resources.add(mock);
			groupEntityBuilder.mockResourceGroupEntity(mock.getName(), resources);
		}

		when(mock.getResourceType()).thenReturn(type);
		if (type != null && Mockito.mockingDetails(type).isMock()) {
			Set<ResourceEntity> resources = type.getResources() != null ? type.getResources() : new HashSet<ResourceEntity>();
			resources.add(mock);
			when(type.getResources()).thenReturn(resources);
		}
		when(mock.getRelease()).thenReturn(release);
		when(mock.getConsumedMasterRelations()).thenReturn(new HashSet<ConsumedResourceRelationEntity>());
		when(mock.getConsumedSlaveRelations()).thenReturn(new HashSet<ConsumedResourceRelationEntity>());
		when(mock.getProvidedMasterRelations()).thenReturn(new HashSet<ProvidedResourceRelationEntity>());
		when(mock.getProvidedSlaveRelations()).thenReturn(new HashSet<ProvidedResourceRelationEntity>());
		when(mock.getRuntime()).thenReturn(runtime);

		return mock;
	}

	/**
	 * @param name
	 *             if group is not null and groupName is set the groupname will be used. If both groupName
	 *             and name are empty a default name will be created
	 * @param group
	 *             if group is null a group will be created
	 * @param type
	 *             resource will be added to type resourcelist
	 * @param release
	 * @param withId
	 *             if true an id will be generated
	 * @return resource entity with the given values.
	 */
	public ResourceEntity buildResourceEntity(String name, ResourceGroupEntity group, ResourceTypeEntity type, ReleaseEntity release, boolean withId) {
		ResourceEntity entity;

		if (group != null) {
			entity = ResourceFactory.createNewResource(group);
		} else {
			entity = ResourceFactory.createNewResource(name);
		}

		if (withId) {
			Integer id = getNextId();
			entity.setId(id);
		}

		entity.setResourceType(type);
		if (type != null) {
			Set<ResourceEntity> resources = type.getResources() != null ? type.getResources() : new HashSet<ResourceEntity>();
			resources.add(entity);
			type.setResources(resources);
		}

		entity.setRelease(release);

		entity.setConsumedMasterRelations(new HashSet<ConsumedResourceRelationEntity>());
		entity.setConsumedSlaveRelations(new HashSet<ConsumedResourceRelationEntity>());
		entity.setProvidedMasterRelations(new HashSet<ProvidedResourceRelationEntity>());
		entity.setProvidedSlaveRelations(new HashSet<ProvidedResourceRelationEntity>());

		return entity;
	}

	/**
	 * @param name
	 *             if group is not null and groupName is set the groupname will be used. If both groupName
	 *             and name are empty a default name will be created
	 * @param group
	 *             if group is null a group will be created
	 * @param typeName
	 *             resourceType with the given name will be mocked
	 * @param release
	 * @return mock of a resource entity with the given values.
	 */
	public ResourceEntity mockResourceEntity(String name, ResourceGroupEntity group, String typeName, ReleaseEntity release) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.mockResourceTypeEntity(typeName, null);
		return mockResourceEntity(name, group, type, release, null);
	}

	/**
	 * @param name
	 *             if group is not null and groupName is set the groupname will be used. If both groupName
	 *             and name are empty a default name will be created
	 * @param group
	 *             if group is null a group will be created
	 * @param release
	 * @param runtime
	 * @return mock of a resource entity with applicationServer type
	 */
	public ResourceEntity mockAppServerEntity(String name, ResourceGroupEntity group, ReleaseEntity release, ResourceGroupEntity runtime) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.mockAppServerResourceTypeEntity(null);
		ResourceEntity resource = mockResourceEntity(name, group, type, release, runtime);
		return resource;
	}

    /**
     * @param name
     *             if group is not null and groupName is set the groupname will be used. If both groupName
     *             and name are empty a default name will be created
     * @param group
     *             if group is null a group will be created
     * @param release
     * @return mock of a resource entity with applicationServer type
     */
    public ResourceEntity mockRuntimeEntity(String name, ResourceGroupEntity group, ReleaseEntity release) {
        ResourceTypeEntity type = resourceTypeEntityBuilder.mockRuntimeResourceTypeEntity(null);
        ResourceEntity resource = mockResourceEntity(name, group, type, release, null);
        return resource;
    }

	/**
	 * @param name
	 *             if group is not null and groupName is set the groupname will be used. If both groupName
	 *             and name are empty a default name will be created
	 * @param group
	 *             if group is null a group will be created
	 * @param release
	 * @return mock of a resource entity with application type
	 */
	public ResourceEntity mockApplicationEntity(String name, ResourceGroupEntity group, ReleaseEntity release) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.mockApplicationResourceTypeEntity(null);
		ResourceEntity resource = mockResourceEntity(name, group, type, release, null);
		return resource;
	}

	/**
	 * @param name
	 *             if group is not null and groupName is set the groupname will be used. If both groupName
	 *             and name are empty a default name will be created
	 * @param group
	 *             if group is null a group will be created
	 * @param release
	 * @return mock of a resource entity with node type
	 */
	public ResourceEntity mockNodeEntity(String name, ResourceGroupEntity group, ReleaseEntity release) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.mockNodeResourceTypeEntity(null);
		ResourceEntity resource = mockResourceEntity(name, group, type, release, null);
		return resource;
	}

	public ResourceEntity buildResourceEntity(String name, ResourceGroupEntity group, String typeName, ReleaseEntity release, boolean withId) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.buildResourceTypeEntity(typeName, null, withId);
		return buildResourceEntity(name, group, type, release, withId);
	}

	public ResourceEntity buildAppServerEntity(String name, ResourceGroupEntity group, ReleaseEntity release, boolean withId) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.buildAppServerResourceTypeEntity(null, withId);
		return buildResourceEntity(name, group, type, release, withId);
	}

	public ResourceEntity buildApplicationEntity(String name, ResourceGroupEntity group, ReleaseEntity release, boolean withId) {
		ResourceTypeEntity type = resourceTypeEntityBuilder.buildApplicationResourceTypeEntity(null, withId);
		return buildResourceEntity(name, group, type, release, withId);
	}

    public ResourceEntityBuilder withName(String name){
        this.name = name;
        return this;
    }

    public ResourceEntityBuilder withOwner(ForeignableOwner owner){
        this.owner = owner;
        return this;
    }

	public ResourceEntityBuilder forResourceGroup(ResourceGroupEntity group){
		this.group = group;
		return this;
	}

	public ResourceEntityBuilder withId(Integer id){
		this.id = id;
		return this;
	}

	public ResourceEntityBuilder withType(ResourceTypeEntity type){
		this.type = type;
		return this;
	}

	public ResourceEntityBuilder withTypeOfName(String typeName){
		this.resourceTypeName = typeName;
		return this;
	}

	public ResourceEntityBuilder withFunctions(Set<AmwFunctionEntity> functions){
		this.functions = functions;
		return this;
	}

	public ResourceEntityBuilder withIsDeletable(boolean isDeletable){
		this.isDeletable = isDeletable;
		return this;
	}

	public ResourceEntityBuilder withSoftlinkId(String softlinkId){
		this.softlinkId = softlinkId;
		return this;
	}

	public ResourceEntityBuilder withSoftlinkRelation(SoftlinkRelationEntity softlinkRelation){
		this.softlinkRelation = softlinkRelation;
		return this;
	}

	public ResourceEntityBuilder withRelease(ReleaseEntity release){
		this.release = release;
		return this;
	}

	public ResourceEntityBuilder withContexts(Set<ResourceContextEntity> contexts) {
		this.contexts = contexts;
		return this;
	}

	public ResourceEntityBuilder withLocalPortId(String localPortId){
		this.localPortId = localPortId;
		return this;
	}

	public ResourceEntity build(){
		ResourceEntity resourceEntity;

		if(StringUtils.isNotEmpty(resourceTypeName)){
			resourceEntity = buildResourceEntity(name, group, resourceTypeName, release, false);
		}else{
			resourceEntity = buildResourceEntity(name, group, type, release, false);
		}

		if (type != null) {
			Set<ResourceEntity> resources = new HashSet<>();
			resources.add(resourceEntity);
			type.setResources(resources);
		}

		resourceEntity.setId(id);

        if (owner != null){
            resourceEntity.setOwner(owner);
        }

		resourceEntity.setFunctions(functions);

		resourceEntity.setDeletable(isDeletable);

		resourceEntity.setSoftlinkId(softlinkId);
		
		resourceEntity.setContexts(contexts);

		resourceEntity.setLocalPortId(localPortId);

		if(softlinkRelation != null){
			throw new UnsupportedOperationException("Softlinkrelation is only applicable in mock");
		}

		return resourceEntity;
	}

	public ResourceEntity mock(){
		ResourceEntity resourceEntity;

		if(StringUtils.isNotEmpty(resourceTypeName)){
			resourceEntity = mockResourceEntity(name, group, resourceTypeName, release);
		}else{
			resourceEntity = mockResourceEntity(name, group, type, release, null);
		}

		when(resourceEntity.getId()).thenReturn(id);
		when(resourceEntity.getOwner()).thenReturn(owner);
		when(resourceEntity.getFunctions()).thenReturn(functions);
		when(resourceEntity.isDeletable()).thenReturn(isDeletable);
		when(resourceEntity.getSoftlinkId()).thenReturn(softlinkId);
		when(resourceEntity.getContexts()).thenReturn(contexts);
		when(resourceEntity.getLocalPortId()).thenReturn(localPortId);
		if(softlinkRelation != null){
			when(resourceEntity.getSoftlinkRelation()).thenReturn(softlinkRelation);
			if (Mockito.mockingDetails(softlinkRelation).isMock()) {
				when(softlinkRelation.getCpiResource()).thenReturn(resourceEntity);
			}else{
				softlinkRelation.setCpiResource(resourceEntity);
			}
		}

		return resourceEntity;
	}

	public static ResourceEntity createResourceEntity(String name, Integer resourceId) {
		ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(resourceId).withName(name).build();
		return resourceEntity;
	}


}

