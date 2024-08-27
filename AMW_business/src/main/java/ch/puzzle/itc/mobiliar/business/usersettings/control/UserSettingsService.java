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

package ch.puzzle.itc.mobiliar.business.usersettings.control;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.FavoriteResourceEntity;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.MyAMWObject;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.UserSettingsEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

@Stateless
public class UserSettingsService implements Serializable {

	@Inject
	protected EntityManager entityManager;

	@Inject
	protected Logger log;

	public Map<Integer, MyAMWObject> loadFavoriteResources(String userName) {
		return new LinkedHashMap<Integer, MyAMWObject>(); // Returns an empty map
	}

	/**
	 * @param userName
	 * @return
	 */
	public List<FavoriteResourceEntity> fetchFavoriteResources(String userName) {
		return new ArrayList<FavoriteResourceEntity>(); // Returns an empty list
	}

	public UserSettingsEntity saveUserSettings(UserSettingsEntity userSettings) {
		return entityManager.merge(userSettings);
	}

	public UserSettingsEntity getUserSettings(String userName) {
		Query q = entityManager.createQuery("from UserSettingsEntity u where u.userName=:name")
				.setParameter("name", userName);
		UserSettingsEntity user;
		try {
			user = (UserSettingsEntity) q.getSingleResult();
		}
		catch (NoResultException e) {
			user = new UserSettingsEntity();
			user.setUserName(userName);
			entityManager.persist(user);
		}
		return user;
	}

	/**
	 * @param groupId
	 * @param userName
	 * @return
	 */
	public UserSettingsEntity removeFavoriteResource(Integer groupId, String userName) {
		Query q = entityManager
				.createQuery(
						"from FavoriteResourceEntity f where f.resourceGroup.id=:groupId and f.user.userName=:name")
				.setParameter("name", userName).setParameter("groupId", groupId);
		try {
			FavoriteResourceEntity r = (FavoriteResourceEntity) q.getSingleResult();
			entityManager.remove(r);
			return r.getUser();
		}
		catch (NoResultException e) {
			log.log(Level.WARNING, "Error loading FavoriteResourceEntity", e);
		}
		return null;
	}

	/**
	 * Returns a List of Usernames, which have registered at least one of the given ids as a favorite
	 * 
	 * @param ids
	 * @return
	 */
	public List<String> getRegisteredUsernamesForResourcesIds(Set<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<String>();
		}

		// Find registered users for ids
		final TypedQuery<String> q = entityManager
				.createQuery(
						"select distinct u.userName from FavoriteResourceEntity f left join f.user u left join f.resourceGroup g where f.email=true and g.id in (:ids)",
						String.class).setParameter("ids", ids);
		return q.getResultList();
	}
}
