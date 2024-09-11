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

package ch.puzzle.itc.mobiliar.presentation.propertyEdit;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import lombok.Getter;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.events.ChangeSelectedRelationEvent;
import ch.puzzle.itc.mobiliar.presentation.util.TestingMode;

@CompositeBackingBean
public class EditPropertiesTable implements Serializable {

	private ch.puzzle.itc.mobiliar.business.utils.Identifiable resourceOrResourceType;

	@Getter
	private Integer relationId;


	/**
	 * Observes if currently editing resource instance
	 */
	public void onChangedResource(@Observes ResourceEntity resourceEntity) {
		if (resourceEntity != null) {
			resourceOrResourceType = resourceEntity;
		}
	}

	/**
	 * Observes if currently editing resource type
	 */
	public void onChangedResourceType(@Observes ResourceTypeEntity resourceTypeEntity) {
		if (resourceTypeEntity != null) {
			resourceOrResourceType = resourceTypeEntity;
		}
	}

	public void onChangedRelation(@Observes ChangeSelectedRelationEvent relationEvent) {
		if (relationEvent != null && relationEvent.getRelation() != null) {
			ResourceEditRelation relation = relationEvent.getRelation();
			if (relation.isResourceTypeRelation()) {
				relationId = relation.getResRelTypeId();
			}
			else {
				relationId = relation.getResRelId();
			}
		}
	}

	/**
	 * @return true if the descriptor of the property is defined on resource and the current focus is
	 *         {@link ResourceEntity} or if the descriptor of the property is defined on resource type and
	 *         the current focus is {@link ResourceTypeEntity}
	 */
	public boolean isEditable(ResourceEditProperty property) {
		boolean isEditable = false;
		ResourceEditProperty.Origin propertyDescriptorOrigin = property.getPropertyDescriptorOrigin();

		switch (propertyDescriptorOrigin) {
		case INSTANCE:
			isEditable = isCurrentFocusOnResource();
			break;
		case TYPE:
			isEditable = isCurrentFocusOnResourceType() && !property.isDescriptorDefinedOnSuperResourceType();
			break;
		}

		return isEditable;
	}

	/**
	 * @return true if current focus is of {@link ResourceTypeEntity}; If not yet set then return false as
	 *         default
	 */
	private boolean isCurrentFocusOnResourceType() {
		return resourceOrResourceType != null && resourceOrResourceType instanceof ResourceTypeEntity;
	}

	/**
	 * @return true if current focus is of {@link ResourceEntity}; If not yet set then return true as default
	 */
	private boolean isCurrentFocusOnResource() {
		return resourceOrResourceType != null && resourceOrResourceType instanceof ResourceEntity;
	}

	public boolean isPropertyDisplayable(ResourceEditProperty property) {
		return property != null
				&& (property.getCardinalityProperty() == null || property.getCardinalityProperty() != -1);
	}

	public Integer getResourceTypeId() {
		if (isCurrentFocusOnResourceType()) {
			return resourceOrResourceType.getId();
		}
		return null;
	}

	public Integer getResourceId() {
		if (isCurrentFocusOnResource()) {
			return resourceOrResourceType.getId();
		}
		return null;
	}

}
