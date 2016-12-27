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

package ch.puzzle.itc.mobiliar.business.resourcerelation.entity;

import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

/**
 * Entity implementation class for Entity: ResourceType
 */
@Entity
@Audited
@Table(name = "TAMW_resourceRelationType")
public class ResourceRelationTypeEntity extends HasContexts<ResourceRelationTypeContextEntity> implements
		Serializable, Identifiable {

	@Getter
	@Setter
	@TableGenerator(name = "resourceRelationTypeIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "resourceRelationTypeId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "resourceRelationTypeIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@ManyToOne
	private ResourceTypeEntity resourceTypeA;

	@Getter
	@ManyToOne
	private ResourceTypeEntity resourceTypeB;

	@OneToMany(mappedBy = "resourceRelationType", cascade = ALL)
	private Set<ResourceRelationTypeContextEntity> contexts;

	@OneToMany(mappedBy = "resourceRelationType", cascade = ALL)
	private Set<ConsumedResourceRelationEntity> consumedResourceRelations;

	@OneToMany(mappedBy = "resourceRelationType", cascade = ALL)
	private Set<ProvidedResourceRelationEntity> providedResourceRelations;

	private static final long serialVersionUID = 1L;

	@Getter
	@Version
	private long v;

	@Setter
	private String identifier;

	@Override
	public Set<ResourceRelationTypeContextEntity> getContexts() {
		return contexts;
	}

	@Override
	public void setContexts(Set<ResourceRelationTypeContextEntity> contexts) {

		this.contexts = contexts;
	}

	public void setResourceTypes(ResourceTypeEntity resourceTypeA, ResourceTypeEntity resourceTypeB) {
		this.resourceTypeA = resourceTypeA;
		this.resourceTypeB = resourceTypeB;
	}

	@Override
	public ResourceRelationTypeContextEntity createContext() {
		ResourceRelationTypeContextEntity c = new ResourceRelationTypeContextEntity();
		c.setContextualizedObject(this);
		return c;
	}

	public String getIdentifier() {
		return StringUtils.isNotBlank(identifier) ? identifier : resourceTypeB.getName().toLowerCase();
	}

	public Set<ConsumedResourceRelationEntity> getConsumedResourceRelations() {
		return consumedResourceRelations;
	}


	public Set<ProvidedResourceRelationEntity> getProvidedResourceRelations() {
		return providedResourceRelations;
	}

	@Override
	public String toString() {
		return "ResourceRelationTypeEntity [id=" + id + ", identifier=" + identifier + ", resourceTypeA="
				+ resourceTypeA + ", resourceTypeB=" + resourceTypeB + "]";
	}


	public String getRelationIdentifier() {
		return identifier;
	}

	public ResourceEntity getSlaveResource() {
		return null;
	}

}
