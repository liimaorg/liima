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

package ch.puzzle.itc.mobiliar.business.configurationtag.control;

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

/**
 * Persistence tests for {@link ch.puzzle.itc.mobiliar.business.configurationtag.control.TagConfigurationService}
 */
@ExtendWith({MockitoExtension.class, PersistenceTestExtension.class})
public class TagConfigurationServicePersistenceTest {

	@PersistenceContext
	EntityManager entityManager;

	@Mock
	PermissionService permissionService;

	@Mock
	ResourceLocator resourceLocator;

	@Spy
	@InjectMocks
	TagConfigurationService service;

	@Test
	public void test_tagConfiguration() throws ResourceNotFoundException, ElementAlreadyExistsException {
		// given
		ResourceEntity as1 = createAndPersistResourceEntity("appServer1");
		String tagLabel = "foo";
		Date tagDate = new Date();

		// when
		ResourceTagEntity result = service.tagConfiguration(as1.getId(), tagLabel, tagDate);

		// then
		assertNotNull(result);
		assertEquals(tagLabel, result.getLabel());
		assertEquals(tagDate, result.getTagDate());
		assertEquals(as1.getId(), result.getResource().getId());
	}

	@Test
	public void test_getTags(){
		// given
		ResourceEntity as1 = createAndPersistResourceEntity("appServer1");
		createAndPersistTag("tag1", as1);
		createAndPersistTag("tag2", as1);
		ResourceEntity as2 = createAndPersistResourceEntity("appServer2");
		createAndPersistTag("tag3", as2);

		// when
		List<ResourceTagEntity> result1 = service.getTags(as1);
		List<ResourceTagEntity> result2 = service.getTags(as2);
		
		// then
		assertNotNull(result1);
		assertEquals(2, result1.size());
		for (ResourceTagEntity t : result1) {
			assertEquals(as1.getId(), t.getResource().getId());
		}
		
		assertNotNull(result2);
		assertEquals(1, result2.size());
		for (ResourceTagEntity t : result2) {
			assertEquals(as2.getId(), t.getResource().getId());
		}
	}

	@Test
	void getTags_byResourceId() throws NotFoundException {
		// given
		ResourceEntity as1 = createAndPersistResourceEntity("appServer1");
		createAndPersistTag("tag1", as1);
		createAndPersistTag("tag2", as1);
		ResourceEntity as2 = createAndPersistResourceEntity("appServer2");
		createAndPersistTag("tag3", as2);

		doReturn(as1).when(resourceLocator).getResourceById(as1.getId());

		// when
		List<ResourceTagEntity> result = service.getTags(as1.getId());

		// then
		assertEquals(2, result.size());
		assertEquals("tag1", result.get(0).getLabel());
		assertEquals("tag2", result.get(1).getLabel());
	}

	private ResourceEntity createAndPersistResourceEntity(String name) {
		ResourceEntity as1 = ResourceFactory.createNewResource(name);
		entityManager.persist(as1);
		return as1;
	}

	private void createAndPersistTag(String label, ResourceEntity resourceEntity) {
		ResourceTagEntity tag1 = new ResourceTagEntity();
		tag1.setLabel(label);
		tag1.setResource(resourceEntity);
		tag1.setTagDate(new Date());
		entityManager.persist(tag1);
	}
}
