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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;

/**
 * Controller for the resourceList screen
 */
@Named
@RequestScoped
public class ResourceListController {

	@Inject
	private ResourceBoundary resourceBoundary;

	@Inject
	private ResourceGroupLocator resourceGroupLocator;

	@Inject
	private ResourceDependencyResolverService dependencyResolver;

	/**
	 * Load all resourceGroups for a specific resourceType
	 *
	 * @param resourceType
	 * @return sorted set of resourceGroups
	 */
	public SortedSet<ResourceGroup> loadResourceGroupsForResourceType(ResourceType resourceType) {
		SortedSet<ResourceGroup> groups = new TreeSet<ResourceGroup>();
		List<ResourceGroupEntity> result = resourceGroupLocator.getGroupsForType(resourceType.getId(), true);
		for (ResourceGroupEntity g : result) {
			groups.add(ResourceGroup.createByResource(g, dependencyResolver));
		}
		return groups;
	}

    /**
	* CONSUMED
	*
	* @return
	*/
    public boolean removeResource(Integer resourceId, String resourceName, String releaseName,
		    boolean isDefaultResourceType) {
	   boolean isSuccessful = false;
	   try {
		  if (resourceId != null) {
			 try {
				if (isDefaultResourceType) {
				    resourceBoundary.removeResourceEntityOfDefaultResType(resourceId);
				}
				else {
				    resourceBoundary.removeResource(resourceId);
				}
				String message = "Resource " + resourceName + " (" + releaseName + ") "
						+ " successfully deleted";
				GlobalMessageAppender.addSuccessMessage(message);
				isSuccessful = true;
			 }
			 catch (EJBException e) {
				if (e.getCause() instanceof NotAuthorizedException) {
				    GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
				}
				else {
				    throw e;
				}
			 }
		  }
		  else {
			 String message = "Systemerror. The selection was empty! Could not delete resource "
					 + resourceId;
			 GlobalMessageAppender.addErrorMessage(message);
		  }
	   }
	   catch (ResourceNotFoundException e) {
		  String message = "Could not find resource.";
		  GlobalMessageAppender.addErrorMessage(message);
	   }
	   catch (Exception e) {
		  String message = "Could not remove resourcetype.";
		  GlobalMessageAppender.addErrorMessage(message);
	   }

	   return isSuccessful;
    }

}
