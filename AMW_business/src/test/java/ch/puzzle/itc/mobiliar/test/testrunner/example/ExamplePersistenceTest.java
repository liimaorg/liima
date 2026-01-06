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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

@ExtendWith(PersistenceTestExtension.class)
public class ExamplePersistenceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
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
