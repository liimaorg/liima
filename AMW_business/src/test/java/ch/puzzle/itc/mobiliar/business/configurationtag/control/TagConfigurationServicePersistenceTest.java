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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

/**
 * Persistence tests for {@link ch.puzzle.itc.mobiliar.business.configurationtag.control.TagConfigurationService}
 */
@ExtendWith(PersistenceTestExtension.class)
public class TagConfigurationServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@Mock
	PermissionService permissionService;

	@InjectMocks
	TagConfigurationService service;


	@BeforeEach
	public void before() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void test_tagConfiguration() throws ResourceNotFoundException, ElementAlreadyExistsException {
		// given
		ResourceEntity as1 = ResourceFactory.createNewResource("appServer1");
		entityManager.persist(as1);
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
	public void test_loadTagLabelsForResource(){
		// given
		ResourceEntity as1 = ResourceFactory.createNewResource("appServer1");
		entityManager.persist(as1);
		ResourceTagEntity tag1 = new ResourceTagEntity();
		tag1.setLabel("tag1");
		tag1.setResource(as1);
		tag1.setTagDate(new Date());
		entityManager.persist(tag1);
		ResourceTagEntity tag2 = new ResourceTagEntity();
		tag2.setLabel("tag2");
		tag2.setResource(as1);
		tag2.setTagDate(new Date());
		entityManager.persist(tag2);
		
		ResourceEntity as2 = ResourceFactory.createNewResource("appServer2");
		entityManager.persist(as2);
		ResourceTagEntity tag3 = new ResourceTagEntity();
		tag3.setLabel("tag3");
		tag3.setResource(as2);
		tag3.setTagDate(new Date());
		entityManager.persist(tag3);

		// when
		List<ResourceTagEntity> result1 = service.loadTagLabelsForResource(as1);
		List<ResourceTagEntity> result2 = service.loadTagLabelsForResource(as2);
		
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

}
