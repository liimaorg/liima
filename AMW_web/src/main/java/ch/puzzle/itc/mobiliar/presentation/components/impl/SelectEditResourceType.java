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

package ch.puzzle.itc.mobiliar.presentation.components.impl;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.presentation.common.ResourceTypeDataProvider;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectEditResourceType {

	private Integer selectedResourceTypeId;
	private String newResourceTypeName;
	private Integer newResourceTypeParentId;

	protected abstract ResourceTypeDataProvider getResourceTypeDataProvider();

	public List<ResourceType> getPredefinedResourceTypes() {
		return getResourceTypeDataProvider().getPredefinedResourceTypes();
	}

	public List<ResourceType> getRootResourceTypes() {
		return getResourceTypeDataProvider().getRootResourceTypes();
	}

	public List<ResourceType> getResourceTypeChildren(Integer id) {
		// TODO: how is it possible that id is 0?
		if (id == 0) {
			return null;
		}
		return getResourceTypeDataProvider().getResourceTypeById(id).getChildren();
	}

	public void setSelectedResourceTypeId(Integer id) {
		boolean changed = false;
		if (getSelectedResourceType() != null && !getSelectedResourceType().getId().equals(id)) {
			changed = true;
		}
		this.selectedResourceTypeId = id;
		if (changed) {
			onResourceTypeChanged();
		}
	}

	public Integer getSelectedResourceTypeId() {
		return this.selectedResourceTypeId;
	}

	private ResourceType getResourceTypeById(Integer id) {
		for (ResourceType type : getRootResourceTypeList()) {
			if (type.getId().equals(id)) {
				return type;
			}
			for (ResourceType subType : type.getChildren()) {
				if (subType.getId().equals(id)) {
					return subType;
				}
			}
		}
		return null;
	}

	public boolean isSelectedResourceType(Integer id) {
		return getSelectedResourceType() != null ? getSelectedResourceType().getId().equals(id) : false;
	}

    	public boolean isSelectedSubResourceType(Integer id) {
	    ResourceType type = getSelectedResourceType();
	    return type!=null && type.getEntity().getParentResourceType()!=null && type.getEntity().getParentResourceType().getId().equals(id) ? true : false;
	}

	public ResourceType getSelectedResourceType() {
		ResourceType resType = getResourceTypeById(getSelectedResourceTypeId());
		if (resType == null) {
			resType = getFirstResourceType();
		}
		return resType;
	}

	private ResourceType getFirstResourceType() {
		if (getPredefinedResourceTypes() != null && !getPredefinedResourceTypes().isEmpty()) {
			return getPredefinedResourceTypes().get(0);
		}
		else if (getRootResourceTypes() != null && !getRootResourceTypes().isEmpty()) {
			return getRootResourceTypes().get(0);
		}
		return null;
	}

	// --------------------------------------------

	public void setNewResourceTypeName(String newResourceTypeName) {
		this.newResourceTypeName = newResourceTypeName;
	}

	public String getNewResourceTypeName() {
		return this.newResourceTypeName;
	}

	public Integer getNewResourceTypeParentId() {
		return this.newResourceTypeParentId;
	}

	public void setNewResourceTypeParentId(Integer newResourceTypeParentId) {
		this.newResourceTypeParentId = newResourceTypeParentId;
	}

	public List<SelectItem> getResourceTypesSelectionList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (ResourceType rt : getResourceTypeDataProvider().getRootResourceTypes()) {
			result.add(new SelectItem(rt.getId(), rt.getName()));
		}
		return result;
	}

	protected void clearPopupContent() {
		newResourceTypeName = null;
		newResourceTypeParentId = null;
	}

	public void initResourceTypeSelectionWithFirstItem() {
		if (!getRootResourceTypeList().isEmpty()) {
			if (getSelectedResourceTypeId() == null) {
				setSelectedResourceTypeId(getAllResourceTypes().get(0).getId());
			}
			else {
				setSelectedResourceTypeId(getSelectedResourceType().getId());
			}
		}
	}

	public List<ResourceType> getRootResourceTypeList() {
		return getResourceTypeDataProvider().getAllMainResourceTypes();
	}

	public void createResourceType() {
		ResourceType result = getResourceTypeDataProvider().createResourceType(getNewResourceTypeName(),
				getNewResourceTypeParentId());
		if (result != null) {
			setSelectedResourceTypeId(result.getId());
		}
		clearPopupContent();
	}

	public void removeResourceType() {
		if (getResourceTypeDataProvider().removeResourceType(getSelectedResourceType())) {
			setSelectedResourceTypeId(null);
			initResourceTypeSelectionWithFirstItem();
		}
	}

	public List<ResourceType> getAllResourceTypes() {
		return getResourceTypeDataProvider().getAllResourceTypes();
	}

	protected void onResourceTypeChanged() {

	}
}
