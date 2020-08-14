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

package ch.puzzle.itc.mobiliar.presentation.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import ch.puzzle.itc.mobiliar.business.usersettings.entity.MyAMWObject;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.usersettings.control.UserSettingsService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.UserSettingsEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

@Named
@SessionScoped
public class UserSettings implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	UserSettingsService service;

	@Inject
	PermissionService permissionService;

	@Inject
	@TestingMode
	Event<Boolean> testingModeEvent;

	/**
	 * Loaded at initialization time
	 */
	private UserSettingsEntity userSetting;

	/**
	 * Loaded lazily on first access
	 */
	private Map<Integer, MyAMWObject> favoriteResources;

	private boolean testingMode;

	@PostConstruct
	public void init() {
		userSetting = service.getUserSettings(getUserName());
	}

	public List<Integer> getMyAMWFilter() {
		if (isMyAMWMode()) {
			List<Integer> result = getFavoriteResources();
			if (result == null || result.isEmpty()) {
				// Hibernate doesn't like to filter with an empty list.
				// therefore set -1 as an id which will never match.
				return Arrays.asList(-1);
			}
			else {
				return result;
			}
		}
		return null;
	}

	public boolean isMyAMWMode() {
		return userSetting != null ? userSetting.isMyAmwEnabled() : false;
	}

	public void setMyAMWMode(boolean myAMWMode) {
		if (myAMWMode != isMyAMWMode()) {
			userSetting = service.getUserSettings(getUserName());
			userSetting.setMyAmwEnabled(myAMWMode);
			userSetting = service.saveUserSettings(userSetting);
		}
	}

	/**
	 * @param groupId
	 * @return
	 */
	public boolean getFavorite(Integer groupId) {
		return getFavoriteResources().contains(groupId);
	}

	/**
	 * @param groupId
	 * @param name
	 * @param resourceType
	 * @throws ResourceNotFoundException
	 */
	public void setFavorite(Integer groupId, String name, String resourceType)
			throws ResourceNotFoundException {
		if (groupId != null && StringUtils.isNotBlank(name)) {
			if (!getFavoriteResources().contains(groupId)) {
				try {
					addFavoriteResource(groupId, name, resourceType);
					GlobalMessageAppender.addSuccessMessage("Resource " + name + " added to favorites.");
				}
				catch (ResourceNotFoundException e) {
					GlobalMessageAppender
							.addErrorMessage("Resource not found - was not able to add resource to favorites");
				}
			}
			else {
				removeFavoriteResource(groupId);
				GlobalMessageAppender.addSuccessMessage("Resource " + name + " removed from favorites.");
			}
		}
	}

	public String getUserName() {
		return permissionService.getCurrentUserName();
	}

	public List<Integer> getFavoriteResources() {
		if (favoriteResources == null) {
			favoriteResources = service.loadFavoriteResources(getUserName());
		}
		return new ArrayList<Integer>(favoriteResources.keySet());
	}

	/**
	 * @param groupId
	 * @param name
	 * @param resourceType
	 * @throws ResourceNotFoundException
	 */
	public void addFavoriteResource(Integer groupId, String name, String resourceType)
			throws ResourceNotFoundException {
		userSetting = service.addFavoriteResource(groupId, getUserName());
		favoriteResources = service.loadFavoriteResources(getUserName());
	}

	/**
	 * @param groupId
	 */
	public void removeFavoriteResource(Integer groupId) {
		userSetting = service.removeFavoriteResource(groupId, getUserName());
		favoriteResources = service.loadFavoriteResources(getUserName());
	}

	public List<MyAMWObject> getMyAMWObjects() {
		favoriteResources = service.loadFavoriteResources(getUserName());
		return new ArrayList<MyAMWObject>(favoriteResources.values());
	}

	public List<MyAMWObject> getMyAMWASandApps() {
		List<MyAMWObject> result = new ArrayList<MyAMWObject>();
		if (favoriteResources != null) {
			for (MyAMWObject o : favoriteResources.values()) {
				if (o.isAsOrApp()) {
					result.add(o);
				}
			}
		}
		return result;
	}

    /**
     * Switches the email notification setting for the given MyAMWObject
     * @param object
     */
    public void switchEmailNotification(MyAMWObject object) {
		try {
			userSetting = service.setEmail(object.getGroupId(), getUserName(), !object.isEmail());
			GlobalMessageAppender.addSuccessMessage("eMail settings for " + object.getName()
					+ " successfully saved");
		}
		catch (Exception e) {
			GlobalMessageAppender.addErrorMessage("eMail settings were not saved: " + e.getMessage());
		}
	}

	@Produces
	@TestingMode
	public boolean isTestingMode() {
		return testingMode;
	}

    /**
     * Publishes testing mode change when mode changed
     */
	public void setTestingMode(boolean testingMode) {
		if (testingMode != this.testingMode) {
			this.testingMode = testingMode;
			testingModeEvent.fire(testingMode);
		}
	}

	/**
	 * Filters the testing properties if the current session is not in testing mode
	 * 
	 * @param listWithTestingProperties
	 */
	public List<ResourceEditProperty> filterTestingProperties(
			List<ResourceEditProperty> listWithTestingProperties) {
		if (!isTestingMode()) {
			Iterator<ResourceEditProperty> iterator = listWithTestingProperties.iterator();
			while (iterator.hasNext()) {
				ResourceEditProperty p = iterator.next();
				if (p.isTesting()) {
					iterator.remove();
				}
			}
		}
		return listWithTestingProperties;
	}

	/**
	 * Filters the testing properties if the current session is not in testing mode
	 * 
	 * @param listWithTestingProperties
	 */
	public List<PropertyDescriptorEntity> filterTestingPropertyDescriptors(
			List<PropertyDescriptorEntity> listWithTestingProperties) {
		if (!isTestingMode()) {
			Iterator<PropertyDescriptorEntity> iterator = listWithTestingProperties.iterator();
			while (iterator.hasNext()) {
				PropertyDescriptorEntity p = iterator.next();
				if (p.isTesting()) {
					iterator.remove();
				}
			}
		}
		return listWithTestingProperties;
	}
}
