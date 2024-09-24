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

import ch.puzzle.itc.mobiliar.business.usersettings.entity.UserSettingsEntity;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.logging.Logger;

@Stateless
public class UserSettingsService implements Serializable {

	@Inject
	protected EntityManager entityManager;

	@Inject
	protected Logger log;

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
}
