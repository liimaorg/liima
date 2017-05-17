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

package ch.puzzle.itc.mobiliar.presentation.security;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;

/**
 * First: Permissions should not be checked directly from jsf (xhtml) but rather through their corresponding view backing beans
 * Second: The permission check logic should be located in business (boundary)
 */
@Named
@SessionScoped // TODO do we need session scope or can we use view scope?
@Deprecated
public class SecurityDataProvider implements Serializable{

	private static final long serialVersionUID = 1L;

	@Inject
	SecurityController controller;

	@Inject
	PermissionBoundary permissionBoundary;

	/**
	 * Use {@link PermissionBoundary#hasPermission(Permission)} instead
	 * @param permissionValue
	 * @return
	 */
	@Deprecated
	public boolean hasPermission(String permissionValue){
		return permissionBoundary.hasPermission(permissionValue);
	}

	/**
	 * @param permissionValue Name of the Permission (must be mappable to the Permission ENUM)
	 * @param actionValue Name of the Action (must be mappable to the Action ENUM)
	 * @return
	 */
	public boolean hasPermission(String permissionValue, String actionValue){
		return permissionBoundary.hasPermission(permissionValue, actionValue);
	}

	/**
	 * @param permissionValue Name of the Permission (must be mappable to the Permission ENUM)
	 * @param actionValue Name of the Action (must be mappable to the Action ENUM)
	 * @param resourceTypeValue Name of a valid ResourceTypeEntity
	 * @return
	 */
	public boolean hasPermissionForResourceType(String permissionValue, String actionValue, String resourceTypeValue){
		return permissionBoundary.hasPermissionForResourceType(permissionValue, actionValue, resourceTypeValue);
	}

	/**
	 * @param permissionValue Name of the Permission (must be mappable to the Permission ENUM)
	 * @param actionValue Name of the Action (must be mappable to the Action ENUM)
	 * @param resourceTypeValue Name of a valid ResourceTypeEntity
	 * @param contextId Id of the actual Context
	 * @return
	 */
	public boolean hasPermissionForResourceType(String permissionValue, String actionValue, String resourceTypeValue, Integer contextId){
		return permissionBoundary.hasPermissionForResourceType(permissionValue, actionValue, resourceTypeValue, contextId);
	}
	
	public boolean hasPermissionToDeploy(){
		return controller.hasPermissionToDeploy();
	}
	
	public boolean hasPermissionToCreateShakedownTest(){
		// TODO: bsc: 13.11.2012: Permission muss erstellt werden hier
		return true;
	}

	public String getUserName() {
		return controller.getUserName();
	}

	public void logout() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		String logoutUrl = ConfigurationService.getProperty(ConfigKey.LOGOUT_URL);
		if (logoutUrl == null) {
			throw new RuntimeException("Please define the property \""+ConfigKey.LOGOUT_URL+"\"");
		}
		FacesContext.getCurrentInstance().getExternalContext().redirect(logoutUrl);
	}

}
