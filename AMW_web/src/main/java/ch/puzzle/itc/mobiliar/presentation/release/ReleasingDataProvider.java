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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import lombok.Getter;
import ch.puzzle.itc.mobiliar.business.foreignable.boundary.ForeignableBoundary;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.Releasing;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourcesScreenDomainService;
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
	ResourcesScreenDomainService resourcesScreenDomainService;

	@Inject
    PermissionBoundary permissionBoundary;

    @Inject
    ForeignableBoundary foreignableBoundary;

    @Inject
	Releasing releasing;

    @Inject
    PropertyEditDataProvider propertyEditDataProvider;

	boolean active;

	@Getter
	List<ReleaseEntity> notDefinedReleases;

	ReleaseEntity newRelease;

	private ResourceEntity currentSelectedResource;

	@Getter
	private Integer targetResourceId;

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

	}

	public void onChangedResourceType(@Observes ResourceTypeEntity resourceTypeEntity) {
		notDefinedReleases = Collections.emptyList();
		currentSelectedResource = null;
	}

	public boolean isCanCreateNewRelease(){
		return permissionBoundary.canCopyFromResource(currentSelectedResource);
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
					newRelease, currentSelectedResource.getRelease(), ForeignableOwner.getSystemOwner());
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
        catch (ForeignableOwnerViolationException e) {
            GlobalMessageAppender.addErrorMessage("Owner "+e.getViolatingOwner()+" is not allowed to copy to create release");
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

	public String removeRelease() {
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
				resourcesScreenDomainService.removeResourceEntityOfDefaultResType(ForeignableOwner.getSystemOwner(), currentSelectedResource.getId());
			}
			else {
			    resourcesScreenDomainService.removeResource(ForeignableOwner.getSystemOwner(), currentSelectedResource.getId());
			}
		    GlobalMessageAppender.addSuccessMessage("Release successfully removed!");
		    	return fallbackRelease==null ? "resourceList?faces-redirect=true" : NavigationUtils.getRefreshOutcomeWithResource(fallbackRelease);
		}
        catch (ForeignableOwnerViolationException e) {
            GlobalMessageAppender.addErrorMessage("Release can not be deleted by owner " + e.getViolatingOwner());
        }
        catch (ResourceNotFoundException | ElementAlreadyExistsException e) {
			GlobalMessageAppender.addErrorMessage("It was not possible to remove the release of the given resource: "
					+ e.getMessage());
		}
	    return null;

	}

    public boolean isCanChangeRelease() {
        return permissionBoundary.hasPermission(Permission.CHANGE_RESOURCE_RELEASE) && currentSelectedResource != null
				&& foreignableBoundary.isModifiableByOwner(ForeignableOwner.getSystemOwner(), currentSelectedResource);
    }

    public boolean isCanRemoveRelease() {
        return currentSelectedResource != null && permissionBoundary.hasPermissionToRemoveInstanceOfResType(currentSelectedResource.getResourceType())
				&& foreignableBoundary.isModifiableByOwner(ForeignableOwner.getSystemOwner(), currentSelectedResource);
    }
}
