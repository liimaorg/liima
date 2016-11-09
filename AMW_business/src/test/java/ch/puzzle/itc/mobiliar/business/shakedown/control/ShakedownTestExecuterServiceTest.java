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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.builders.ShakedownTestGenerationResultBuilder;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorDomainServiceWithAppServerRelations;
import ch.puzzle.itc.mobiliar.business.generator.control.LockingService;
import ch.puzzle.itc.mobiliar.business.generator.control.ShakedownTestGeneratorDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;

public class ShakedownTestExecuterServiceTest {

	@InjectMocks
	ShakedownTestExecuterService shakedownTestExecuterService;

	@Mock
	ShakedownTestService shakedownTestService;

	@Mock
	ShakedownTestAsynchronousExecuter shakedownTestAsynchronousExecuter;

	@Mock
	private GeneratorDomainServiceWithAppServerRelations generatorDomainServiceWithAppServerRelations;

	@Mock
	private ShakedownTestGeneratorDomainService shakedownTestGeneratorDomainService;

	@Mock
	LockingService locking;

	@Mock
	EntityManager entityManager;

	@Mock
	Logger log;

	ContextEntity contextEntity;

	ResourceEntity applicationServer;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		contextEntity = new ContextEntity();
		contextEntity.setName("testContext");
		contextEntity.setId(Integer.valueOf(2));

		applicationServer =  ResourceFactory.createNewResource("testAppserver");
		applicationServer.setId(5);
	}

	@Test
	public void test_executeShakedownTest() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		final ShakedownTestEntity resultShakedownTestEntity = new ShakedownTestEntity();


		ShakedownTestGenerationResult generationResult = ShakedownTestGenerationResultBuilder.buildGenerationResultSuccess(resultShakedownTestEntity, null);

		when(shakedownTestService.getShakedownTestById(test.getId())).thenReturn(test);
		when(locking.lockShakedownTestForTesting(test.getId())).thenReturn(true);
		when(entityManager.find(ShakedownTestEntity.class, test.getId())).thenReturn(test);
		when(shakedownTestGeneratorDomainService.generateConfigurationForShakedownTest(test)).thenReturn(generationResult);

		// when
		shakedownTestExecuterService.generateConfigurationAndExecuteShakedownTest(test.getId());

		// then
		verify(shakedownTestAsynchronousExecuter, times(1)).executeShakedownTest(generationResult);
	}

	@Test
	public void test_executeShakedownTest_faultyGenerationResult() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		ShakedownTestGenerationResult generationResult = ShakedownTestGenerationResultBuilder.mockGenerationResultWithErrors(test, null);

		when(shakedownTestService.getShakedownTestById(test.getId())).thenReturn(test);
		when(locking.lockShakedownTestForTesting(test.getId())).thenReturn(true);
		when(entityManager.find(ShakedownTestEntity.class, test.getId())).thenReturn(test);
		when(shakedownTestGeneratorDomainService.generateConfigurationForShakedownTest(test)).thenReturn(generationResult);

		// when
		shakedownTestExecuterService.generateConfigurationAndExecuteShakedownTest(test.getId());

		// then
		verify(shakedownTestAsynchronousExecuter, times(0)).executeShakedownTest(any(ShakedownTestGenerationResult.class));
	}

	@Test
	public void test_executeShakedownTest_nullGenerationResult() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		when(shakedownTestService.getShakedownTestById(test.getId())).thenReturn(test);
		when(locking.lockShakedownTestForTesting(test.getId())).thenReturn(true);
		when(entityManager.find(ShakedownTestEntity.class, test.getId())).thenReturn(test);
		when(shakedownTestGeneratorDomainService.generateConfigurationForShakedownTest(test)).thenReturn(null);

		// when
		shakedownTestExecuterService.generateConfigurationAndExecuteShakedownTest(test.getId());

		// then
		verify(shakedownTestAsynchronousExecuter, times(0)).executeShakedownTest(any(ShakedownTestGenerationResult.class));
	}

	@Test
	public void test_executeShakedownTest_noShakedowntTest() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		when(shakedownTestService.getShakedownTestById(test.getId())).thenReturn(null);

		// when
		shakedownTestExecuterService.generateConfigurationAndExecuteShakedownTest(test.getId());

		// then
		verify(shakedownTestAsynchronousExecuter, times(0)).executeShakedownTest(any(ShakedownTestGenerationResult.class));
	}

}


