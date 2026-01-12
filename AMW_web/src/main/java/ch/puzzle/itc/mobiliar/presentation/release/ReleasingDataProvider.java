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

package ch.puzzle.itc.mobiliar.presentation.release;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.presentation.common.context.SessionContext;
import lombok.Getter;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.Releasing;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.propertyEdit.PropertyEditDataProvider;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import ch.puzzle.itc.mobiliar.presentation.util.NavigationUtils;

@CompositeBackingBean
public class ReleasingDataProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	CopyResource copyResource;

	@Inject
	ReleaseMgmtService releaseMgmtService;

	@Inject
	ResourceBoundary resourceBoundary;

	@Inject
    PermissionBoundary permissionBoundary;

    @Inject
	Releasing releasing;

    @Inject
    PropertyEditDataProvider propertyEditDataProvider;

	@Inject
	SessionContext context;

	boolean active;

	@Getter
	List<ReleaseEntity> notDefinedReleases;

	ReleaseEntity newRelease;

	private ResourceEntity currentSelectedResource;

	@Getter
	private Integer targetResourceId;

	@Getter
	private boolean canCreateNewRelease;

	@Getter
	private boolean canChangeRelease;

	@Getter
	private boolean canRemoveRelease;

	public Integer getNewReleaseId() {
		return newRelease != null ? newRelease.getId() : null;
	}

	public void setNewReleaseId(Integer id) {
		// jsf returns 0 for null, therefore we better use -1 to indicate nothing selected
		if (id != null && id >= 0) {
			for (ReleaseEntity rel : notDefinedReleases) {
				if (rel.getId().equals(id)) {
					newRelease = rel;
					return;
				}
			}
		}
		newRelease = null;
	}

	public void onChangedResource(@Observes ResourceEntity resourceEntity) {
		notDefinedReleases = releasing.getNotDefinedReleasesForResource(resourceEntity);
		currentSelectedResource = resourceEntity;
		canCreateNewRelease = permissionBoundary.hasPermission(Permission.RESOURCE, null,
				Action.CREATE, currentSelectedResource, null) 
				&& permissionBoundary.canCopyFromSpecificResource(currentSelectedResource, currentSelectedResource.getResourceGroup());
		canChangeRelease = permissionBoundary.hasPermission(Permission.RELEASE, null,
				Action.UPDATE, currentSelectedResource, null);
		canRemoveRelease = permissionBoundary.hasPermission(Permission.RESOURCE, null,
				Action.DELETE, currentSelectedResource, null);

	}

	public void onChangedResourceType(@Observes ResourceTypeEntity resourceTypeEntity) {
		notDefinedReleases = Collections.emptyList();
		currentSelectedResource = null;
		canCreateNewRelease = false;
		canChangeRelease = false;
		canRemoveRelease = false;
	}

	public String createRelease() {
		if (newRelease == null) {
			String message = "No release selected";
			GlobalMessageAppender.addErrorMessage(message);
			return null;
		}
		if (currentSelectedResource == null) {
			String message = "No resource selected";
			GlobalMessageAppender.addErrorMessage(message);
			return null;
		}
		try {
			CopyResourceResult result = copyResource.doCreateResourceRelease(currentSelectedResource.getResourceGroup(),
					newRelease, currentSelectedResource.getRelease());
			if (result.isSuccess()) {
				GlobalMessageAppender.addSuccessMessage("Copy successful");
				List<String> infos = result.getCopyResultInfosAsHTMLStrings();
				for (String info : infos) {
					GlobalMessageAppender.addSuccessMessage(info);
				}
				targetResourceId = result.getTargetResourceId();
				return NavigationUtils.getRefreshOutcomeWithResource(result.getTargetResourceId());
			}
			else {
				for (String error : result.getExceptions()) {
					GlobalMessageAppender.addErrorMessage(error);
				}
			}
		}
		catch (ResourceNotFoundException e) {
			String message = "The selected resource can not be found.";
			GlobalMessageAppender.addErrorMessage(message);
		}
		catch (AMWException e) {
			GlobalMessageAppender.addErrorMessage(e.getMessage());
		}
		return null;
	}

	public String changeRelease() {
		if (newRelease == null) {
			String message = "No release selected";
			GlobalMessageAppender.addErrorMessage(message);
			return null;
		}
		if (currentSelectedResource == null) {
			String message = "No resource selected";
			GlobalMessageAppender.addErrorMessage(message);
			return null;
		}
		try {
			releaseMgmtService.changeReleaseOfResource(currentSelectedResource, newRelease);
			targetResourceId = currentSelectedResource.getId();
			String message = "Release successfully changed";
			GlobalMessageAppender.addSuccessMessage(message);
			return NavigationUtils.getRefreshOutcomeWithResource(targetResourceId);
		}
		catch (ResourceNotFoundException e) {
			String message = "The selected resource can not be found.";
			GlobalMessageAppender.addErrorMessage(message);
		}
		return null;
	}

	public String removeRelease() throws IOException {
		try {
		    	LinkedHashMap<String, Integer> releaseToResourceMap = propertyEditDataProvider.getGroup()
				    .getReleaseToResourceMap();
		     Integer fallbackRelease = null;
		    	boolean resourceIdFound = false;
		    	for(Integer resourceId : releaseToResourceMap.values()){
			    if(resourceId.equals(currentSelectedResource.getId())){
				   resourceIdFound = true;
				   if(fallbackRelease!=null){
					  break;
				   }
			    }
			    else if(resourceIdFound){
				   fallbackRelease = resourceId;
				   break;
			    }
			    else {
				   fallbackRelease = resourceId;
			    }
			}
			if (currentSelectedResource.getResourceType().isDefaultResourceType()) {
				resourceBoundary.removeResourceEntityOfDefaultResType(currentSelectedResource.getId());
			}
			else {
				resourceBoundary.removeResource(currentSelectedResource.getId());
			}
		    GlobalMessageAppender.addSuccessMessage("Release successfully removed!");
			if (fallbackRelease == null) {
				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.redirect("/AMW_angular/#/resources");			}
			else {
				return NavigationUtils.getRefreshOutcomeWithResource(fallbackRelease);
			}
		}
        catch (ResourceNotFoundException | ElementAlreadyExistsException e) {
			GlobalMessageAppender.addErrorMessage("It was not possible to remove the release of the given resource: "
					+ e.getMessage());
		}
	    return null;
	}
}
