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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.builders.ContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.DeploymentEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ShakedownTestOrderBuilder;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestOrder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.database.control.SequencesService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;

@RunWith(PersistenceTestRunner.class)
public class ShakedownTestServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@Spy
	ResourceDependencyResolverService dependencyResolverService;

	@Mock
	SequencesService sequencesService;

	@Mock
	ResourceTypeProvider resourceTypeProvider;

	@InjectMocks
	ShakedownTestService service;

	private DeploymentEntityBuilder deploymentEntityBuilder;
	private ReleaseEntityBuilder releaseEntityBuilder;
	private ResourceEntityBuilder resourceEntityBuilder;
	private ContextEntityBuilder contextEntityBuilder;


	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		int trackingId = 200;
		when(sequencesService.getNextValueAndUpdate(DeploymentEntity.SEQ_NAME)).thenReturn(trackingId++);
		when(sequencesService.getNextValueAndUpdate(ShakedownTestEntity.SEQ_NAME)).thenReturn(trackingId++);

		deploymentEntityBuilder = new DeploymentEntityBuilder();
		releaseEntityBuilder = new ReleaseEntityBuilder();
		resourceEntityBuilder = new ResourceEntityBuilder();
		contextEntityBuilder = new ContextEntityBuilder();
	}

	@Test
	public void test_createShakedownTestOrderReturnsTrackingId() {
		// given
		ContextEntity context = new ContextEntity();
		context.setName("B");
		entityManager.persist(context);

		ResourceTypeEntity type = new ResourceTypeEntity();
		type.setName("applicationServer");
		entityManager.persist(type);

		ReleaseEntity release = new ReleaseEntity();
		release.setName("rel-1.0");
		release.setInstallationInProductionAt(new Date());
		entityManager.persist(release);

		ResourceEntity appServer = ResourceFactory.createNewResource();
		appServer.setRelease(release);
		entityManager.persist(appServer);

		when(resourceTypeProvider.getFromDB(type.getName())).thenReturn(type);

		ShakedownTestOrder order = ShakedownTestOrderBuilder.mockShakedownTestOrder(context, release,
				Collections.singletonList(appServer.getResourceGroup()));

		// when
		Integer result = service.createShakedownTestOrderReturnsTrackingId(Collections.singletonList(order));

		// then
		assertNotNull(result);
	}

	@Test
	public void test_updateShakedownInfo() {
		// given
		ShakedownTestEntity st = new ShakedownTestEntity();
		Date testDate = new Date(System.currentTimeMillis() - 1);
		st.setTestDate(testDate);
		entityManager.persist(st);
		entityManager.flush();

		// when
		ShakedownTestEntity test = service.updateShakedownInfo(st.getId(), "success", shakedownTest_state.success);

		// then
		assertNotNull(test);
		assertEquals(shakedownTest_state.success.getDisplayName(), test.getShakedownTestState().getDisplayName());
		assertEquals("success", test.getTestResult());
		assertTrue(testDate.before(test.getTestDate()));
	}

	@Test
	public void test_createShakedownTestOrderForDeploymentOrder() {
		// given
		Integer trackingId = 300;
		ReleaseEntity rel = releaseEntityBuilder.buildReleaseEntity("rl2.3", new Date(), false);
		ResourceEntity as = resourceEntityBuilder.buildAppServerEntity("as1", null, rel, false);
		ContextEntity contextDev = contextEntityBuilder.buildContextEntity("DEV", null, null, false);
		ContextEntity contextInt = contextEntityBuilder.buildContextEntity("INT", null, null, false);
		DeploymentEntity deployment1 = deploymentEntityBuilder.buildDeploymentEntity(trackingId, rel, as.getResourceGroup(), as, true, contextDev, false);
		DeploymentEntity deployment2 = deploymentEntityBuilder.buildDeploymentEntity(trackingId, rel, as.getResourceGroup(), as, true, contextInt, false);

		entityManager.persist(rel);
		entityManager.persist(as);
		entityManager.persist(contextDev);
		entityManager.persist(contextInt);
		entityManager.persist(deployment1);
		entityManager.persist(deployment2);

		// when
		service.createShakedownTestOrderForDeploymentOrder(deployment1);
		service.createShakedownTestOrderForDeploymentOrder(deployment2);

		// then
		List<ShakedownTestEntity> result = service.getShakedownTestsByTrackingId(trackingId);
		assertNotNull(result);
		assertEquals(2, result.size());

	}

}
