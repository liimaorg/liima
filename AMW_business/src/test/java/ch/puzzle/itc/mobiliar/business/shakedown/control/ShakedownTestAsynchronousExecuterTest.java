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

import ch.puzzle.itc.mobiliar.builders.ShakedownTestGenerationResultBuilder;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.STS;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet.OverallStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.logging.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ShakedownTestAsynchronousExecuterTest {


	@InjectMocks
	ShakedownTestAsynchronousExecuter shakedownTestAsynchronousExecuter;

	@Mock
	ShakedownTestExecutionResultHandlerService shakedownTestExecutionResultHandlerService;

	@Mock
	ShakedownTestRunner testRunner;

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
	public void test_executeShakedownTest_success() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		ShakedownTestGenerationResult result = ShakedownTestGenerationResultBuilder.buildGenerationResultSuccess(test, null);
		TestSet testSet = mock(TestSet.class);
		when(testSet.getOverallStatus()).thenReturn(OverallStatus.success);
		when(testSet.getTestMessage()).thenReturn("success");
		when(testRunner.executeShakedownTest(any(STS.class))).thenReturn(testSet);

		// when
		shakedownTestAsynchronousExecuter.executeShakedownTest(result);

		// then
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleSuccessfulShakedownTest(test);
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleUnsuccessfulShakedownTest(any(Exception.class), anyInt());
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleUnsuccessfulShakedownTest(anyString(), anyInt());
	}

	@Test
	public void test_executeShakedownTest_noSTS() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		ShakedownTestGenerationResult result = ShakedownTestGenerationResultBuilder.buildGenerationResultSuccessEmptySTS(test, null);

		// when
		shakedownTestAsynchronousExecuter.executeShakedownTest(result);

		// then
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleSuccessfulShakedownTest(test);
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleUnsuccessfulShakedownTest(any(Exception.class), anyInt());
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleUnsuccessfulShakedownTest(anyString(), anyInt());
	}

	@Test
	public void test_executeShakedownTest_resultFailure() {
		// given
		ShakedownTestEntity test = new ShakedownTestEntity();
		test.setId(1);
		test.setApplicationServer(applicationServer);
		test.setContext(contextEntity);

		ShakedownTestGenerationResult result = ShakedownTestGenerationResultBuilder.mockGenerationResultWithErrors(test, null);

		// when
		shakedownTestAsynchronousExecuter.executeShakedownTest(result);

		// then
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleSuccessfulShakedownTest(test);
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleUnsuccessfulShakedownTest(any(Exception.class), anyInt());
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleUnsuccessfulShakedownTest(anyString(), anyInt());
	}
}
