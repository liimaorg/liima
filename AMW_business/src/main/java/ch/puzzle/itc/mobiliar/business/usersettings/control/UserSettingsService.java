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
		List<FavoriteResourceEntity> tmpResult = fetchFavoriteResources(userName);
		Map<Integer, MyAMWObject> result = new LinkedHashMap<Integer, MyAMWObject>();

		if (tmpResult != null) {
			for (FavoriteResourceEntity fav : tmpResult) {
				// This is a temporary hack - the resource type will be put on the resource group in the
				// next release, so this is not possible anymore. however for the moment, it still is
				// possible that a resourcegroup exists without resources and therefore without
				// resourcetype...
				ResourceTypeEntity t = fav.getResourceGroup().getResourceType();
				if (t != null) {
					result.put(fav.getResourceGroup().getId(), new MyAMWObject(fav.getResourceGroup()
							.getId(), fav.getResourceGroup().getName(), fav.isEmail(), fav
							.getResourceGroup().getResourceType().getName()));
				}
			}
		}
		return result;
	}

	/**
	 * @param userName
	 * @return
	 */
	public List<FavoriteResourceEntity> fetchFavoriteResources(String userName) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<FavoriteResourceEntity> q = cb.createQuery(FavoriteResourceEntity.class);
		Root<FavoriteResourceEntity> r = q.from(FavoriteResourceEntity.class);
		Join<FavoriteResourceEntity, UserSettingsEntity> userSettings = r.join("user");
		Join<FavoriteResourceEntity, ResourceGroupEntity> resourceGroup = r.join("resourceGroup");
		Predicate userNamePred = cb.like(userSettings.<String> get("userName"), userName);
		q.where(userNamePred);
		q.orderBy(cb.asc(resourceGroup.get("name")));
		r.fetch("resourceGroup", JoinType.LEFT);

		return entityManager.createQuery(q).getResultList();
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

	public UserSettingsEntity setEmail(Integer groupId, String userName, boolean email) {
		Query q = entityManager
				.createQuery(
						"from FavoriteResourceEntity f where f.resourceGroup.id=:groupId and f.user.userName=:name")
				.setParameter("name", userName).setParameter("groupId", groupId);
		FavoriteResourceEntity r = (FavoriteResourceEntity) q.getSingleResult();
		r.setEmail(email);
		entityManager.persist(r);
		return r.getUser();
	}

	/**
	 * @param groupId
	 * @param userName
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public UserSettingsEntity addFavoriteResource(Integer groupId, String userName)
			throws ResourceNotFoundException {
		Query q = entityManager.createQuery(
				"from UserSettingsEntity u left join fetch u.favoriteResources where u.userName=:name")
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

		FavoriteResourceEntity r = new FavoriteResourceEntity();
		r.setUser(user);
		ResourceGroupEntity g = entityManager.find(ResourceGroupEntity.class, groupId);
		if (g == null) {
			throw new ResourceNotFoundException("ResourceGroup with id " + groupId + " not found.");
		}
		r.setResourceGroup(g);
		entityManager.persist(r);

		if (user.getFavoriteResources() == null) {
			user.setFavoriteResources(new HashSet<FavoriteResourceEntity>());
		}
		user.getFavoriteResources().add(r);
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
