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

package ch.puzzle.itc.mobiliar.presentation.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.util.NameChecker;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;

@ViewScoped
@Named
public class ResourceTypeDataProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ResourceTypeDomainService domainService;

	@Inject
	protected Logger log;

	private List<ResourceType> rootResourceTypes = new ArrayList<ResourceType>();
	private List<ResourceType> predefinedResourceTypes = new ArrayList<ResourceType>();
	private List<ResourceType> allResourceTypes = new ArrayList<ResourceType>();

	@PostConstruct
	public void load(){
		rootResourceTypes.clear();
		predefinedResourceTypes.clear();
		allResourceTypes.clear();

		for (ResourceTypeEntity e : domainService.getResourceTypes()) {
			ResourceType r = ResourceType.createByResourceType(e, null);
			if (e.getParentResourceType() == null) {
				if (r.isDefaultResourceType()) {
					predefinedResourceTypes.add(r);
				}
				else {
					rootResourceTypes.add(r);
				}
			}
			allResourceTypes.add(r);
		}
	}


	public ResourceType getByName(String name){
		for(ResourceType t : getAllResourceTypes()){
			if(t.getName().equals(name)){
				return t;
			}
		}
		return null;
	}

    /**
	* @return all root resource types except default resource types and runtime
	*/
	public List<ResourceType> getRootResourceTypes() {
		return Collections.unmodifiableList(rootResourceTypes);
	}

	public List<ResourceType> getPredefinedResourceTypes() {
		return Collections.unmodifiableList(predefinedResourceTypes);
	}

	public List<ResourceType> getAllResourceTypes(){
        List<ResourceType> resourceTypes = new ArrayList<>(Collections.unmodifiableList(allResourceTypes));
        Collections.sort(resourceTypes);
        return resourceTypes;
	}

	public ResourceType getResourceTypeById(Integer id){
		for(ResourceType t : getAllResourceTypes()){
			if(t.getId().equals(id)) {
				return t;
			}
		}
		return null;
	}


	public List<ResourceType> getAllMainResourceTypes(){
		List<ResourceType> result = new ArrayList<ResourceType>();
	    result.addAll(getRootResourceTypes());
	    result.addAll(predefinedResourceTypes);
		return Collections.unmodifiableList(result);
	}

	/**
	 * CONSUMED
	 * @param newResourceTypeName
	 * @param newResourceTypeParentId
	 * @return
	 */
	public ResourceType createResourceType(String newResourceTypeName, Integer newResourceTypeParentId) {
		ResourceType result = null;
		try {
			if (newResourceTypeName == null) {
				String message = "Could not read name for new resourcetype.";
				GlobalMessageAppender.addErrorMessage(message);
			} else if (newResourceTypeName.isEmpty()) {
				String message = "The name for the resourcetype must not be empty.";
				GlobalMessageAppender.addErrorMessage(message);
			} else if (!NameChecker.isNameValid(newResourceTypeName)){
				GlobalMessageAppender.addErrorMessage(NameChecker.getErrorTextForResourceType(newResourceTypeName));
			} else {
				try{
					result = domainService.addResourceType(newResourceTypeName, newResourceTypeParentId);
					if(result!=null){
						load();
						String message = "Resourcetype " + newResourceTypeName + " successfully created";
						GlobalMessageAppender.addSuccessMessage(message);
					}
				}catch(EJBException e){
					if(e.getCause() instanceof NotAuthorizedException) {
						GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
					} else {
						throw e;
					}
				} catch (ValidationException e) {
					GlobalMessageAppender.addErrorMessage(e.getMessage());
                }
            }
		} catch (ResourceTypeNotFoundException e) {
			String message = "Could not find resourcetype.";
			GlobalMessageAppender.addErrorMessage(message);
		} catch (ElementAlreadyExistsException e) {
			String message = "A resourcetype with the name \"" + e.getExistingObjectName() + "\" already exists";
			GlobalMessageAppender.addErrorMessage(message);
		}
		load();
		return result;
	}


	/**
	 * CONSUMED
	 * @return
	 */
	public boolean removeResourceType(ResourceType selectedResourceType) {
		boolean isSuccessful = false;

		try {
			if (selectedResourceType != null) {
				domainService.removeResourceType(selectedResourceType.getId());
				load();
				String message = "Resourcetype " + selectedResourceType.getName() + " successfully deleted";
				GlobalMessageAppender.addSuccessMessage(message);
				isSuccessful = true;
			}
		} catch (ResourceNotFoundException e) {
			String message = "The selected resource can not be found.";
			GlobalMessageAppender.addErrorMessage(message);
		} catch (ResourceTypeNotFoundException e) {
			String message = "The selected resourcetype can not be found.";
			GlobalMessageAppender.addErrorMessage(message);
		} catch(EJBException e){
			if(e.getCause() instanceof NotAuthorizedException) {
				GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
			} else {
				throw e;
			}
		}
		load();
		return isSuccessful;

	}

}
