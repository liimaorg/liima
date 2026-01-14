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

package ch.puzzle.itc.mobiliar.business.environment.entity;

import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static javax.persistence.CascadeType.ALL;

/**
 * Entity implementation class for Entity: AbstractResourceBase
 */
@MappedSuperclass
@Audited
public abstract class AbstractContext implements Serializable, Identifiable {
	@TableGenerator(name = "contextGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "contextId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "contextGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;
	@OneToMany(cascade = ALL, orphanRemoval = true)
	// IMPORTANT: This property has to be mapped through a join table, since
	// it affects multiple tables - otherwise there would be problems with
	// foreign key constraints
	private Set<PropertyEntity> properties;
	@OneToMany(cascade = ALL, orphanRemoval = true)
	private Set<TemplateDescriptorEntity> templates;

	@OneToMany(cascade = ALL, orphanRemoval = true)
	private Set<PropertyDescriptorEntity> propertyDescriptors;
	@Version
	private long v;

	private static final long serialVersionUID = 1L;

	public AbstractContext() {
		super();
	}

	public Set<PropertyEntity> getProperties() {
		return this.properties;
	}

	public Set<PropertyDescriptorEntity> getPropertyDescriptors() {
		return this.propertyDescriptors;
	}

	public void addProperty(PropertyEntity property) {
		if (properties == null) {
			properties = new HashSet<PropertyEntity>();
		}
		properties.add(property);
	}

	public void removeProperty(PropertyEntity property) {
		for (PropertyEntity p : properties) {
			if (property.getId().equals(p.getId())) {
				properties.remove(p);
				break;
			}
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Set<TemplateDescriptorEntity> getTemplates() {
		return templates;
	}

	public void addTemplate(TemplateDescriptorEntity template) {
		if (this.templates == null) {
			templates = new HashSet<TemplateDescriptorEntity>();
		}
		templates.add(template);
	}

	public void removeTemplate(TemplateDescriptorEntity template) {
		if (this.templates != null) {
			templates.remove(template);
		}
	}

	public void replacePropertyDescriptor(
			PropertyDescriptorEntity propertyDescriptor) {
		if (this.propertyDescriptors != null && propertyDescriptor != null
				&& propertyDescriptor.getId() != null) {
			for (PropertyDescriptorEntity e : propertyDescriptors) {
				if (e.getId().equals(propertyDescriptor.getId())) {
					removePropertyDescriptor(e);
					addPropertyDescriptor(propertyDescriptor);
				}
			}

		}

	}

	public void setPropertyDescriptors(
			Set<PropertyDescriptorEntity> propertyDescriptors) {
		this.propertyDescriptors = propertyDescriptors;
	}

	public void addPropertyDescriptor(
			PropertyDescriptorEntity propertyDescriptor) {
		if (this.propertyDescriptors == null) {
			this.propertyDescriptors = new HashSet<PropertyDescriptorEntity>();
		}
		this.propertyDescriptors.add(propertyDescriptor);
	}

	public void removePropertyDescriptor(
			PropertyDescriptorEntity propertyDescriptor) {
		if (this.propertyDescriptors != null) {
			propertyDescriptors.remove(propertyDescriptor);
		}
	}

	private Map<Integer, PropertyEntity> getPropertyMap() {
		Map<Integer, PropertyEntity> propertyMap = new HashMap<Integer, PropertyEntity>();
		if (properties != null) {
			for (PropertyEntity p : properties) {
				propertyMap.put(p.getDescriptor().getId(), p);
			}
		}
		return propertyMap;
	}

	public void addPropertiesToListByDescriptor(List<PropertyEntity> result,
			PropertyDescriptorEntity propDescr) {
		PropertyEntity p = getPropertyMap().get(propDescr.getId());
		if (p != null) {
			p.setOwningResource(this);
			result.add(p);
		}
	}

	public long getV() {
		return v;
	}

	public PropertyEntity getPropertyForDescriptor(Integer propertyDescriptorId) {
		if (getProperties() != null) {
			for (PropertyEntity p : getProperties()) {
				if (propertyDescriptorId.equals(p.getDescriptor().getId())) {
					return p;
				}
			}
		}
		return null;

	}

	public void removePropertyById(Integer propertyId) {
		if (getProperties() != null) {
			for (PropertyEntity p : getProperties()) {
				if (propertyId.equals(p.getId())) {
					removeProperty(p);
					break;
				}
			}
		}
	}


}
