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

package ch.puzzle.itc.mobiliar.presentation.resourcelist;

import ch.puzzle.itc.mobiliar.business.foreignable.boundary.ForeignableBoundary;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableAttributesDTO;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.common.ApplicationCreatorDataProvider;
import ch.puzzle.itc.mobiliar.presentation.common.ReleaseSelectionDataProvider;
import ch.puzzle.itc.mobiliar.presentation.common.ReleaseSelector;
import ch.puzzle.itc.mobiliar.presentation.common.ResourceTypeDataProvider;
import ch.puzzle.itc.mobiliar.presentation.components.impl.SelectEditResourceType;
import ch.puzzle.itc.mobiliar.presentation.resourcesedit.CreateResourceController;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
//TODO Migrate to new structure but be careful: The page does not keep the selected resource type when defined as viewscoped.
public class ResourceListDataProvider implements Serializable, ApplicationCreatorDataProvider {

	private static final long serialVersionUID = 1L;

	@Inject
	ResourceListController resourcesController;

	@Inject
	CreateResourceController createResourceController;

	@Inject
	ResourceTypeDataProvider resourceTypeDataProvider;

	@Inject
	PermissionService permissionService;

	@Inject
    PermissionBoundary permissionBoundary;

	@Inject
	UserSettings userSettings;

    @Inject
    ForeignableBoundary foreignableBoundary;

	@Inject
	private ReleaseSelectionDataProvider releaseDataProvider;

	private SelectEditResourceType selectEditResourceTypeComp;

	private List<ResourceGroup> resourceGroups;

	private ResourceGroup selectedResourceGroup;
	private String newResourceName;
	private List<Integer> amwFilter;
	private ResourceType selectedType;
	private ReleaseSelector resourceReleaseSelector;

	public void initView() {
	    if (!FacesContext.getCurrentInstance().isPostback()) {
		   resourceGroups = null;
		   selectedResourceGroup = null;
		   resourceTypeDataProvider.load();
		   getSelectEditResourceTypeComp().initResourceTypeSelectionWithFirstItem();
		   resourceReleaseSelector = new ReleaseSelector(releaseDataProvider.getUpcomingReleaseId(),
				   releaseDataProvider.getReleaseMap());
	    }
	}

	/**
	 * @return list with resourceGroups
	 */
	public List<ResourceGroup> getResourceGroupsForSelectedResourceType() {
		if (resourceGroups == null || !getSelectedResourceType().getId().equals(this.selectedType.getId())
				|| hasAmwFilterChanged()) {
			selectedType = getSelectedResourceType();
			amwFilter = userSettings.getMyAMWFilter();
			resourceGroups = new ArrayList<ResourceGroup>(
					resourcesController.loadResourceGroupsForResourceType(selectedType));
		}
		return resourceGroups;
	}

	/**
	 * Returns true if all elements of this resource group are owned by AMW (in the specific release)
	 */
	public boolean isAmwOnly(ResourceGroup resourceGroup, Integer releaseId) {
        ResourceEntity foreignableResource = resourceGroup.getResourceForRelease(releaseId);

        if (foreignableResource != null){
            return foreignableBoundary.isModifiableByOwner(ForeignableOwner.getSystemOwner(), foreignableResource);
        }
        return false;
	}

    public ForeignableAttributesDTO getForeignableAttribute(ResourceGroup resourceGroup, Integer releaseId){
        ResourceEntity foreignableResource = resourceGroup.getResourceForRelease(releaseId);

        if (foreignableResource != null){
            return new ForeignableAttributesDTO(foreignableResource.getOwner(), foreignableResource.getExternalKey(), foreignableResource.getExternalLink());
        }
        return new ForeignableAttributesDTO();
    }


	private boolean hasAmwFilterChanged() {
		List<Integer> newAmwFilter = userSettings.getMyAMWFilter();
		return (newAmwFilter != null && !newAmwFilter.equals(amwFilter))
				|| (amwFilter != null && !amwFilter.equals(newAmwFilter));
	}

	public ResourceGroup getSelectedResourceGroup() {
	    return selectedResourceGroup;
	}

	public void setSelectedResourceGroup(ResourceGroup group) {
	    this.selectedResourceGroup = group;
	}

	public String getNewResourceName() {
		return this.newResourceName;
	}

	public void setNewResourceName(String newResourceName) {
		this.newResourceName = newResourceName;
	}

	public String getOutcomePageForResourceEdit() {
		String outcome = "";
		ResourceType selectedResourceType = getSelectedResourceType();
		if (selectedResourceType != null) {
			if (selectedResourceType.getName().equals(DefaultResourceTypeDefinition.APPLICATION.name())) {
				outcome = "editResourceView";
			}
			else if (selectedResourceType.getName().equals(
					DefaultResourceTypeDefinition.APPLICATIONSERVER.name())) {
				outcome = "editResourceView";
			}
			else if (selectedResourceType.getName().equals(DefaultResourceTypeDefinition.NODE.name())) {
				outcome = "editResourceView";
			}
			else {
				outcome = "editResourceView";
			}
		}
		return outcome;
	}

	public String getOutcomePageForResourceTypeEdit() {
		return "editResourceType";
	}

	public void clearPopupContent() {
		newResourceName = null;
		resourceReleaseSelector.setSelectedReleaseId(null);
	}

	public void createResource() {
		createResourceAction();
	}

	public void deleteResource() {
		deleteResourceAction();
	}

	public void removeResourceType() {
		removeResourceTypeAction();
	}

	public boolean isDefaultResourceType() {
		return (getSelectedResourceType() != null && getSelectedResourceType().isDefaultResourceType());
	}

	public boolean isRuntimeResourceType() {
		return (getSelectedResourceType() != null && getSelectedResourceType().getEntity() != null && getSelectedResourceType()
				.getEntity().isRuntimeType());
	}

	protected void removeResourceTypeAction() {
		selectEditResourceTypeComp.removeResourceType();
		selectEditResourceTypeComp.setSelectedResourceTypeId(null);
		selectEditResourceTypeComp.initResourceTypeSelectionWithFirstItem();
	}

	protected void deleteResourceAction() {
		if (resourcesController.removeResource(getSelectedResourceGroup().getSelectedResourceId(),
				getSelectedResourceGroup().getSelectedResource().getName(), getSelectedResourceGroup()
						.getSelectedResource().getRelease().getName(), isDefaultResourceType())) {
			resourceGroups = null;
		}
	}

	protected void createResourceAction() {
		if (createResourceController.createResource(getNewResourceName(), getSelectEditResourceTypeComp()
				.getSelectedResourceType().getEntity(), resourceReleaseSelector
				.getSelectedRelease())) {
			resourceGroups = null;
		     clearPopupContent();
		}
	}

	public ResourceType getSelectedResourceType() {
		return getSelectEditResourceTypeComp().getSelectedResourceType();
	}

	public boolean getCanCreateResourceInstance() {
		return permissionBoundary.canCreateResourceInstance(getSelectedResourceType().getEntity());

	}

	public boolean getCanRemoveDefaultInstanceOfResType() {
		if (isDefaultResourceType()) {
			return permissionService.hasPermissionToRemoveDefaultInstanceOfResType();
		} else if (getSelectedResourceType() != null) {
			return permissionService.hasPermissionToRemoveInstanceOfResType(getSelectedResourceType().getEntity());
		}
		return false;
	}

	public SelectEditResourceType getSelectEditResourceTypeComp() {
		if (selectEditResourceTypeComp == null) {
			selectEditResourceTypeComp = new SelectEditResourceType() {

				@Override
				protected ResourceTypeDataProvider getResourceTypeDataProvider() {
					return resourceTypeDataProvider;
				}
			};
		}
		return selectEditResourceTypeComp;
	}

	public ReleaseSelectionDataProvider getReleaseDataProvider() {
		return releaseDataProvider;
	}

	public ReleaseSelector getResourceReleaseSelector() {
		return resourceReleaseSelector;
	}

	@Override
	public void afterAddingAppOrAs() {
		resourceGroups = null;
		clearPopupContent();
	}

}
