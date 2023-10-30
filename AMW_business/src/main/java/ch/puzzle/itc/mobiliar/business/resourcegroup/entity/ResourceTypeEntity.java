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

import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;

/**
 * Entity implementation class for Entity: ResourceType
 *
 */
@Entity
@Audited
@Table(name="TAMW_resourceType")
public class ResourceTypeEntity extends HasContexts<ResourceTypeContextEntity>
		implements Serializable, NamedIdentifiable,
Comparable<ResourceTypeEntity> {

	@Getter
	@Setter
	@TableGenerator(name = "resourceTypeIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "resourceTypeId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "resourceTypeIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	@ManyToOne
	ResourceTypeEntity parentResourceType;

	@Setter
	@OneToMany(mappedBy="parentResourceType", cascade=ALL)
	Set<ResourceTypeEntity> childrenResourceTypes;

	public Set<ResourceTypeEntity> getChildrenResourceTypes() {
		if (childrenResourceTypes == null){
			return new HashSet<>();
		}
		return childrenResourceTypes;
	}

	@Getter
	@Setter
	@OneToMany(mappedBy = "resourceType", cascade = ALL)
	@OrderBy(value="name")
	Set<ResourceEntity> resources;

	@Getter
	@Setter
	@OneToMany(mappedBy = "resourceType", cascade = ALL)
	Set<ResourceGroupEntity> resourceGroups;

	@OneToMany(mappedBy = "resourceTypeEntity", cascade = ALL)
	Set<ResourceTypeContextEntity> contexts;

	@Getter
	@Setter
	@OneToMany(mappedBy = "resourceTypeA", cascade = REMOVE)
	Set<ResourceRelationTypeEntity> resourceRelationTypesA;

	@Getter
	@Setter
	@OneToMany(mappedBy = "resourceTypeB", cascade = REMOVE)
	Set<ResourceRelationTypeEntity> resourceRelationTypesB;

	@OneToMany(mappedBy = "resourceType", cascade = ALL)
	@Setter
	private Set<AmwFunctionEntity> functions;

	@Getter
	@Version
	private long v;

	private static final long serialVersionUID = 1L;


	@Override
	public Set<ResourceTypeContextEntity> getContexts() {
		return contexts;
	}

	@Override
	public void setContexts(Set<ResourceTypeContextEntity> contexts) {
		this.contexts = contexts;
	}

	@Override
	public ResourceTypeContextEntity createContext() {
		ResourceTypeContextEntity context = new ResourceTypeContextEntity();
		context.setContextualizedObject(this);
		return context;
	}

	@Override
	public int compareTo(ResourceTypeEntity o) {
		if (name == null) {
			return -1;
		}
		if (o == null) {
			return 1;
		}
		return name.compareToIgnoreCase(o.getName());
	}

	public boolean isVersioned(){
		return false;
	}

	@Override
	public String toString() {
		return "ResourceTypeEntity [id=" + id + ", name=" + name + "]";
	}


	/**
	 * @return true if type is one of {@link DefaultResourceTypeDefinition},
	 *         false otherwise
	 */
	public boolean isDefaultResourceType() {
		return DefaultResourceTypeDefinition.contains(getName());
	}

	/**
	 * @return true if type is
	 *         {@link DefaultResourceTypeDefinition#APPLICATION}
	 */
	public boolean isApplicationResourceType(){
		return isResourceType(DefaultResourceTypeDefinition.APPLICATION);
	}

	/**
	 * @return true if type is {@link DefaultResourceTypeDefinition#APPLICATIONSERVER}
	 */
	public boolean isApplicationServerResourceType() {
		return isResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER);
	}

	/**
	 * @return true if type is {@link DefaultResourceTypeDefinition#NODE}
	 */
	public boolean isNodeResourceType() {
		return isResourceType(DefaultResourceTypeDefinition.NODE);
	}

	public boolean isRuntimeType() {
		return isResourceType(DefaultResourceTypeDefinition.RUNTIME);
	}

	/**
	 * Checks if the ResourceType is equal to resourceTypeDefinition
	 * 
	 * @param resourceTypeDefinition
	 * @return
	 */
	public boolean isResourceType(DefaultResourceTypeDefinition resourceTypeDefinition){
		if(resourceTypeDefinition!= null){
			try {
				return resourceTypeDefinition.name().equals(
						getName());
			}
			catch (IllegalArgumentException e) {

			}
		}

		return false;
	}

     public boolean hasChildren(){
	    return childrenResourceTypes!=null && !childrenResourceTypes.isEmpty();

	}

	/**
	 * @return true if this resource type is root resource type
	 */
	public boolean isRootResourceType(){
		return parentResourceType == null;
	}

	public Set<AmwFunctionEntity> getFunctions() {
		if (functions == null){
			return new HashSet<>();
		}
		return functions;
	}

	public void addFunction(AmwFunctionEntity function){
		if (functions == null){
			functions = new HashSet<>();
		}
		functions.add(function);
	}

}
