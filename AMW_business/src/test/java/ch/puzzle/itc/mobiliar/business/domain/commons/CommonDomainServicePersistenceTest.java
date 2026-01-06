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

package ch.puzzle.itc.mobiliar.business.domain.commons;

import static ch.puzzle.itc.mobiliar.business.releasing.ReleaseHelper.createRL;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({PersistenceTestExtension.class, MockitoExtension.class})
public class CommonDomainServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	CommonDomainService service;

	@Test
	public void test_getUniqueResourceByName() {
		// given
		ReleaseEntity release1 = createRL("test_getUniqueResourceByName_1", null);
		entityManager.persist(release1);
		ResourceTypeEntity type1 = new ResourceTypeEntity();
		type1.setName("type1");
		entityManager.persist(type1);
		ResourceEntity resource = ResourceFactory.createNewResource("foo");
		resource.setRelease(release1);
		resource.setResourceType(type1);
		entityManager.persist(resource);

		ReleaseEntity release2 = createRL("test_getUniqueResourceByName_2", null);
		entityManager.persist(release2);
		ResourceEntity resource2 = ResourceFactory.createNewResource(resource.getResourceGroup());
		resource2.setResourceType(type1);
		resource2.setRelease(release2);
		entityManager.persist(resource2);

		ResourceEntity resource3 = ResourceFactory.createNewResource("bar");
		resource3.setResourceType(type1);
		resource3.setRelease(release1);
		entityManager.persist(resource3);

		// when
		ResourceEntity result = service.getUniqueResourceByNameAndTypeAndReleaseId("foo", "type1", release1.getId());

		// then
		assertNotNull(result);
		assertEquals(resource.getId(), result.getId());
		assertEquals(resource.getName(), result.getName());
		assertEquals(resource.getRelease().getId(), result.getRelease().getId());
	}

	@Test
	public void test_getUniqueResourceByName_releaseNotExists() {
		// given
		ReleaseEntity release = createRL("test_getUniqueResourceByName_releaseNotExists", null);
		entityManager.persist(release);
		ResourceTypeEntity type1 = new ResourceTypeEntity();
		type1.setName("type1");
		entityManager.persist(type1);
		ResourceEntity resource = ResourceFactory.createNewResource("foo");
		resource.setResourceType(type1);
		resource.setRelease(release);
		entityManager.persist(resource);

		// when
		ResourceEntity result = service.getUniqueResourceByNameAndTypeAndReleaseId("foo", "type1", release.getId() + 1);

		// then
		assertNull(result);
	}

	@Test
	public void test_getUniqueResourceByName_nameNotExists() {
		// given
		ReleaseEntity release = createRL("test_getUniqueResourceByName_nameNotExists", null);
		entityManager.persist(release);
		ResourceTypeEntity type1 = new ResourceTypeEntity();
		type1.setName("type1");
		entityManager.persist(type1);
		ResourceEntity resource = ResourceFactory.createNewResource("foo");
		resource.setResourceType(type1);
		resource.setRelease(release);
		entityManager.persist(resource);

		// when
		ResourceEntity result = service.getUniqueResourceByNameAndTypeAndReleaseId("bar", "type1", release.getId());

		// then
		assertNull(result);
	}

	@Test
	public void test_getUniqueResourceByName_typNotExists() {
		// given
		ReleaseEntity release = createRL("test_getUniqueResourceByName_typNotExists", null);
		entityManager.persist(release);
		ResourceTypeEntity type1 = new ResourceTypeEntity();
		type1.setName("type1");
		entityManager.persist(type1);
		ResourceEntity resource = ResourceFactory.createNewResource("foo");
		resource.setResourceType(type1);
		resource.setRelease(release);
		entityManager.persist(resource);

		// when
		ResourceEntity result = service.getUniqueResourceByNameAndTypeAndReleaseId("foo", "type2", release.getId());

		// then
		assertNull(result);
	}

	@Test
	public void test_getUniqueResourceByNameWithoutReleaseId_typNotExists() {
		// given
		ReleaseEntity release = createRL("test_getUniqueResourceByNameWithoutReleaseId_typNotExists", null);
		entityManager.persist(release);
		ResourceTypeEntity type1 = new ResourceTypeEntity();
		type1.setName("type1");
		entityManager.persist(type1);
		ResourceEntity resource = ResourceFactory.createNewResource("foo");
		resource.setResourceType(type1);
		resource.setRelease(release);
		entityManager.persist(resource);

		// when
		ResourceEntity result = service.getUniqueResourceByNameAndType("foo", "type2");

		// then
		assertNull(result);
	}

}
