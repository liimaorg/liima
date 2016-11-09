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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.AppServerRelationProperties;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Data Unit for Template generation.
 * 
 * Holds templates for resource and relations.
 * 
 */
public class GenerationUnit {

	private Set<TemplateDescriptorEntity> resourceTemplates;
	private Set<TemplateDescriptorEntity> relationTemplates;
	private ResourceEntity resource;
	private ResourceEntity slaveResource;
	private AppServerRelationProperties properties;
	private boolean packageGenerationUnit = false;
	@Getter
	@Setter
	private boolean templateGenerationDisabled = false;
	@Getter
	@Setter
	private List<GlobalFunctionEntity> globalFunctionTemplates;

	public GenerationUnit(ResourceEntity slaveResource, ResourceEntity resource, AppServerRelationProperties properties, 
			Set<TemplateDescriptorEntity> resourceTemplates, Set<TemplateDescriptorEntity> relationTemplates) {
		this.slaveResource = slaveResource;
		this.resource = resource;
		this.properties = properties;
		this.resourceTemplates = resourceTemplates;
		this.relationTemplates = relationTemplates;
	}

	public GenerationUnit(ResourceEntity slaveResource, ResourceEntity resource, Set<TemplateDescriptorEntity> resourceTemplates, AppServerRelationProperties properties) {
		this(slaveResource, resource, properties, resourceTemplates, new TreeSet<TemplateDescriptorEntity>());
	}

	public ResourceEntity getSlaveResource() {
		return slaveResource;
	}
	
	public ResourceEntity getResource() {
		return resource;
	}

	public Set<TemplateDescriptorEntity> getTemplates() {
		return resourceTemplates;
	}

	public Set<TemplateDescriptorEntity> getRelationTemplates() {
		return relationTemplates;
	}

	/**
	 * @return s the Properties as Model
	 */
	public AmwResourceTemplateModel getPropertiesAsModel() {
		AmwResourceTemplateModel model = new AmwResourceTemplateModel();
		if (properties != null) {
			model = properties.transformModel();
		}
		return model;
	}

	public AppServerRelationProperties getAppServerRelationProperties() {
		return properties;
	}
	
	public boolean isPackageGenerationUnit() {
		return packageGenerationUnit;
	}

	public void setPackageGenerationUnit(boolean packageGenerationUnit) {
		this.packageGenerationUnit = packageGenerationUnit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime
				* result
				+ ((relationTemplates == null) ? 0 : relationTemplates
						.hashCode());
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		result = prime
				* result
				+ ((resourceTemplates == null) ? 0 : resourceTemplates
						.hashCode());
		result = prime * result
				+ ((slaveResource == null) ? 0 : slaveResource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		} 
		if (getClass() != obj.getClass()) {
			return false;
		}
		GenerationUnit other = (GenerationUnit) obj;
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else if (!properties.equals(other.properties)) {
			return false;
		}		
		if (relationTemplates == null) {
			if (other.relationTemplates != null) {
				return false;
			}
		} else if (!relationTemplates.equals(other.relationTemplates)) {
			return false;
		}
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
			return false;
		}
		if (resourceTemplates == null) {
			if (other.resourceTemplates != null) {
				return false;
			}
		} else if (!resourceTemplates.equals(other.resourceTemplates)) {
			return false;
		}
		if (slaveResource == null) {
			if (other.slaveResource != null) {
				return false;
			}
		} else if (!slaveResource.equals(other.slaveResource)) {
			return false;
		}
		return true;
	}


	// FIXME: [ama] why can't we use equals and have to compare ids?
	private static boolean compareResource(final ResourceEntity a, ResourceEntity b) {
		return b.getId().equals(a.getId());
	}

	public static Set<GenerationUnit> batchFor(Map<ResourceEntity, Set<GenerationUnit>> batches, ResourceEntity application) {
		for (ResourceEntity app : batches.keySet()) {
			if (compareResource(application, app)) {
				return batches.get(app);
			}
		}
		return Sets.newLinkedHashSet();
	}

	
	public static GenerationUnit forResource(Set<GenerationUnit> work, ResourceEntity resource) {
		for (GenerationUnit unit : work) {
			if (unit.getSlaveResource().getId().equals(resource.getId())) {
				return unit;
			}
		}
		return null;
	}

	public static GenerationUnit getRuntimeGenerationUnit(Set<GenerationUnit> work){
		for (GenerationUnit unit : work) {
			if (unit.getSlaveResource().getResourceType().isRuntimeType()) {
				return unit;
			}
		}
		return null;
	}

	public static Set<GenerationUnit> forAppServer(Set<GenerationUnit> work, ResourceEntity node) {
		List<GenerationUnit> current = Lists.newArrayList();

		List<GenerationUnit> reverse = Lists.reverse(Lists.newArrayList(work));
		for (GenerationUnit unit : reverse) {
			// break as soon as we hit app level
			if (unit.getSlaveResource().getResourceType().isApplicationResourceType()) {
				break;
			}
			// skip any nodes that dont match the node passed
			if (unit.getSlaveResource().getResourceType().isNodeResourceType()) {
				if (!compareResource(unit.getSlaveResource(), node)) {
					continue;
				}

			}
			current.add(unit);
		}
		return Sets.newLinkedHashSet(Lists.reverse(current));
	}

	@Override
	public String toString() {
		return "GenerationUnit [resource=" + slaveResource + "]";
	}

	public static GenerationUnit forAppServer(Set<GenerationUnit> work) {
		for (GenerationUnit unit : work) {
			if (unit.getSlaveResource().getResourceType().isApplicationServerResourceType()) {
				return unit;
			}
		}
		throw new RuntimeException("not found type: APPLICATIONSERVER");
	}

	/**
	 * Do not generate packageGenerationUnits for non defaultTypes
	 * 
	 * @return
	 */
	public boolean isGenerateTemplates() {
		if(isPackageGenerationUnit()){
			if(slaveResource != null && !slaveResource.getResourceType().isDefaultResourceType()){
				return false;
			}
		}
		// do not generate Templates if it is not needed
		if(isTemplateGenerationDisabled()){
			return false;
		}
		
		return true;
	}

}
