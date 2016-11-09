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

package ch.puzzle.itc.mobiliar.test.testrunner.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;

@RunWith(PersistenceTestRunner.class)
public class ExamplePersistenceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Before
	public void setup() throws Exception {
		PropertyDescriptorEntity property = new PropertyDescriptorEntity();
		property.setPropertyComment("comment");
		entityManager.persist(property);
	}

	@Test
	public void shouldFindPropertyDescriptorEntity() {
		// when
		PropertyDescriptorEntity result = entityManager.find(PropertyDescriptorEntity.class, 1);

		// then
		assertNotNull(result);
		assertEquals("comment", result.getPropertyComment());
	}
}
