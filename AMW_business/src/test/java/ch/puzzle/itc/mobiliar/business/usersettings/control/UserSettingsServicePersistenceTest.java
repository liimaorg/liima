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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.FavoriteResourceEntity;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.MyAMWObject;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.UserSettingsEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.test.PersistingEntityBuilder;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(PersistenceTestRunner.class)
public class UserSettingsServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	UserSettingsService service;



	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_addFavoriteResource_single() throws ResourceNotFoundException {
		// given
		ResourceTypeEntity appType = PersistingEntityBuilder.buildResourceType(entityManager, "application");
		ResourceEntity app1 = PersistingEntityBuilder.buildResourceEntityWithRelease(entityManager, appType, "app1", null, null);
		UserSettingsEntity user = new UserSettingsEntity();
		user.setUserName("mobi");
		entityManager.persist(user);

		// when
		UserSettingsEntity resultUser = service.addFavoriteResource(app1.getResourceGroup().getId(), user.getUserName());

		// then
		assertNotNull(resultUser);
		assertNotNull(resultUser.getFavoriteResources());
		assertEquals(1, resultUser.getFavoriteResources().size());
		FavoriteResourceEntity fav = entityManager.find(FavoriteResourceEntity.class, resultUser.getFavoriteResources().iterator().next().getId());
		assertNotNull(fav);
		assertEquals(app1.getResourceGroup().getId(), fav.getResourceGroup().getId());
		assertEquals(user.getId(), fav.getUser().getId());
	}

	@Test
	public void test_addFavoriteResource_sameGroup() throws ResourceNotFoundException {
		// given
		ResourceTypeEntity appType = PersistingEntityBuilder.buildResourceType(entityManager, "application");
		ResourceEntity app1 = PersistingEntityBuilder.buildResourceEntityWithRelease(entityManager, appType, "app1", null, null);
		UserSettingsEntity user = new UserSettingsEntity();
		user.setUserName("mobi");
		entityManager.persist(user);

		// when
		UserSettingsEntity resultUser = service.addFavoriteResource(app1.getResourceGroup().getId(), user.getUserName());
		entityManager.flush();

		// then
		assertNotNull(resultUser);
		assertNotNull(resultUser.getFavoriteResources());
		assertEquals(1, resultUser.getFavoriteResources().size());
		FavoriteResourceEntity fav = entityManager.find(FavoriteResourceEntity.class, resultUser.getFavoriteResources().iterator().next().getId());
		assertNotNull(fav);
		assertEquals(app1.getResourceGroup().getId(), fav.getResourceGroup().getId());
		assertEquals(user.getId(), fav.getUser().getId());

		// when
		boolean trownException = false;
		try {
			service.addFavoriteResource(app1.getResourceGroup().getId(), user.getUserName());
			entityManager.flush();
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				trownException = true;
			}
		}
		assertTrue("ConstraintViolationException expected but not thrown", trownException);
	}

	@Test
	public void test_loadFavoriteResources() throws ResourceNotFoundException {
		// given
		ResourceTypeEntity appType = PersistingEntityBuilder.buildResourceType(entityManager, "application");
		entityManager.persist(appType);
		ResourceTypeEntity wsType = PersistingEntityBuilder.buildResourceType(entityManager, "webservice");
		entityManager.persist(wsType);
		ResourceEntity app1 = PersistingEntityBuilder.buildResourceEntityWithRelease(entityManager, appType, "app1", null, null);
		entityManager.persist(app1);
		ResourceEntity app2 = PersistingEntityBuilder.buildResourceEntityWithRelease(entityManager, appType, "app2", app1.getResourceGroup(), null);
		entityManager.persist(app2);
		ResourceEntity ws1 = PersistingEntityBuilder.buildResourceEntityWithRelease(entityManager, wsType, "ws1", null, null);
		entityManager.persist(ws1);
		UserSettingsEntity user = new UserSettingsEntity();
		user.setUserName("mobi");
		entityManager.persist(user);

		service.addFavoriteResource(app1.getResourceGroup().getId(), user.getUserName());
		service.addFavoriteResource(ws1.getResourceGroup().getId(), user.getUserName());

		// when
		Map<Integer, MyAMWObject> result = service.loadFavoriteResources(user.getUserName());

		// then
		assertNotNull(result);
		assertEquals(2, result.size());
	}

}
