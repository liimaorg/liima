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

package ch.puzzle.itc.mobiliar.business.template.control;

import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PersistenceTestRunner.class)
public class TemplatesScreenDomainServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	TemplatesScreenDomainService service;

	@Before
	public void before() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void test_getTemplateDescriptorByName() {
		// given
		TemplateDescriptorEntity template1 = new TemplateDescriptorEntity();
		template1.setName("datasource_db2");

		TemplateDescriptorEntity template2 = new TemplateDescriptorEntity();
		template2.setName("AMW_stp_ws");

		entityManager.persist(template1);
		entityManager.persist(template2);

		// when
		List<TemplateDescriptorEntity> result1 = service.getTemplateDescriptorByName("datasource_db2");
		List<TemplateDescriptorEntity> result2 = service.getTemplateDescriptorByName("AMW_stp_ws");
		List<TemplateDescriptorEntity> result3 = service.getTemplateDescriptorByName("db2");

		// then
		assertNotNull(result1);
		assertEquals(1, result1.size());
		assertEquals(template1, result1.get(0));

		assertNotNull(result2);
		assertEquals(1, result2.size());
		assertEquals(template2, result2.get(0));

		assertNotNull(result3);
		assertEquals(0, result3.size());
	}

	@Test
	public void test_renameTestingTemplates() {
		// given
		TemplateDescriptorEntity template = new TemplateDescriptorEntity();
		template.setName("AMW_stp_ws");
		entityManager.persist(template);

		// when
		service.renameTestingTemplates("AMW_stp_ws", "AMW_stp_ws2");
		entityManager.flush();

		// then
		entityManager.refresh(template);
		assertEquals("AMW_stp_ws2", template.getName());
	}

}
