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

package ch.puzzle.itc.mobiliar.business.shakedown.control;

import ch.puzzle.itc.mobiliar.business.generator.control.ShakedownTestGeneratorDomainService;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownStpEntity;
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
public class ShakedownStpServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@InjectMocks
	ShakedownStpService service;

	@Mock
	Logger log;

	@Before
	public void before() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void test_getSTPsWithoutSTS() {
		// given
		ShakedownStpEntity foo = new ShakedownStpEntity();
		foo.setStpName("foo");
		entityManager.persist(foo);

		ShakedownStpEntity sts = new ShakedownStpEntity();
		sts.setStpName(ShakedownTestGeneratorDomainService.STS_NAME);
		entityManager.persist(sts);

		// when
		List<ShakedownStpEntity> result = service.getSTPsWithoutSTS();

		// then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(foo, result.get(0));
	}

}
