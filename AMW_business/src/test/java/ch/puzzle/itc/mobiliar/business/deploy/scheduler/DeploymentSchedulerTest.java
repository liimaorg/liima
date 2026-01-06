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

package ch.puzzle.itc.mobiliar.business.deploy.scheduler;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.control.DeploymentExecuterService;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFailureReason;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeploymentSchedulerTest {

	@InjectMocks
	DeploymentScheduler deploymentScheduler;
	@Mock
	Logger log;
	@Mock
	DeploymentBoundary deploymentBoundary;
	@Mock
	DeploymentExecuterService deploymentExecuterService;

	ContextEntity contextEntity;

	@BeforeEach
	public void setUp(){
		contextEntity =  new ContextEntity();
		contextEntity.setName("testContext");
	}

	@Test
	public void test_checkForSimulation_noDeployment() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();

		when(deploymentBoundary.getDeploymentsToSimulate()).thenReturn(deployments);

		// when
		deploymentScheduler.executeSimulation();

		// then
		verify(deploymentExecuterService, never()).generateConfigurationAndExecuteDeployment(any(Integer.class), any(GenerationModus.class));
		verify(log, times(2)).log(argThat(matchesLevel(Level.FINE)), anyString());
	}

	@Test
	public void test_checkForSimulation_OneDeployment_noContext() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		DeploymentEntity deployment = new DeploymentEntity();
		deployments.add(deployment);

		when(deploymentBoundary.getDeploymentsToSimulate()).thenReturn(deployments);

		// when
		deploymentScheduler.executeSimulation();

		// then
		verify(deploymentExecuterService, never()).generateConfigurationAndExecuteDeployment(any(Integer.class), any(GenerationModus.class));
		verify(log, times(1)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	@Test
	public void test_checkForSimulation_OneDeployment_Context() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		ResourceEntity resource =  ResourceFactory.createNewResource("testResource");

		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setResourceGroup(resource.getResourceGroup());
		deployment.setContext(contextEntity);

		deployments.add(deployment);

		when(deploymentBoundary.getDeploymentsToSimulate()).thenReturn(deployments);

		// when
		deploymentScheduler.executeSimulation();

		// then
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.SIMULATE);
		verify(log, times(2)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	@Test
	public void test_checkForSimulation_2Deployments_Context() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		ResourceEntity resource =  ResourceFactory.createNewResource("testResource");

		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setResourceGroup(resource.getResourceGroup());
		deployment.setContext(contextEntity);

		DeploymentEntity deployment2 = new DeploymentEntity();
		deployment2.setId(Integer.valueOf(2));
		deployment2.setResourceGroup(resource.getResourceGroup());
		deployment2.setContext(contextEntity);

		deployments.add(deployment);
		deployments.add(deployment2);

		when(deploymentBoundary.getDeploymentsToSimulate()).thenReturn(deployments);

		// when
		deploymentScheduler.executeSimulation();

		// then
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.SIMULATE);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(2), GenerationModus.SIMULATE);
		verify(log, times(3)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}


	@Test
	public void test_checkForDeployments_noDeployment() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();

		when(deploymentBoundary.getDeploymentsToExecute()).thenReturn(deployments);

		// when
		deploymentScheduler.executeDeployments();

		// then
		verify(deploymentExecuterService, never()).generateConfigurationAndExecuteDeployment(any(Integer.class), any(GenerationModus.class));
		verify(log, times(2)).log(argThat(matchesLevel(Level.FINE)), anyString());
	}

	@Test
	public void test_checkForDeployments_OneDeployment_noContext() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		DeploymentEntity deployment = new DeploymentEntity();
		deployments.add(deployment);

		when(deploymentBoundary.getDeploymentsToExecute()).thenReturn(deployments);

		// when
		deploymentScheduler.executeDeployments();

		// then
		verify(deploymentExecuterService, never()).generateConfigurationAndExecuteDeployment(any(Integer.class), any(GenerationModus.class));
		verify(log, times(1)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	@Test
	public void test_checkForDeployments_OneDeployment_Context() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		ResourceEntity resource =  ResourceFactory.createNewResource("testResource");

		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setResourceGroup(resource.getResourceGroup());
		deployment.setContext(contextEntity);

		deployments.add(deployment);

		when(deploymentBoundary.getDeploymentsToExecute()).thenReturn(deployments);

		// when
		deploymentScheduler.executeDeployments();

		// then
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.DEPLOY);
		verify(log, times(2)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	@Test
	public void test_checkForDeployments_2Deployments_Context() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		ResourceEntity resource =  ResourceFactory.createNewResource("testResource");

		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setResourceGroup(resource.getResourceGroup());
		deployment.setContext(contextEntity);

		DeploymentEntity deployment2 = new DeploymentEntity();
		deployment2.setId(Integer.valueOf(2));
		deployment2.setResourceGroup(resource.getResourceGroup());
		deployment2.setContext(contextEntity);

		deployments.add(deployment);
		deployments.add(deployment2);

		when(deploymentBoundary.getDeploymentsToExecute()).thenReturn(deployments);

		// when
		deploymentScheduler.executeDeployments();

		// then
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.DEPLOY);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(2), GenerationModus.DEPLOY);
		verify(log, times(3)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	@Test
	public void test_checkForEndlessDeployments_noDeployments(){
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		when(deploymentBoundary.getDeploymentsInProgressTimeoutReached()).thenReturn(deployments);

		// when
		deploymentScheduler.checkForEndlessDeployments();

		// then
		verify(deploymentBoundary, times(0)).updateDeploymentInfoAndSendNotification(any(GenerationModus.class), any(Integer.class), anyString(), any(Integer.class), any(GenerationResult.class), any(DeploymentFailureReason.class));
		verify(log, times(2)).log(argThat(matchesLevel(Level.FINE)), anyString());
	}

	@Test
	public void test_checkForEndlessDeployments_Deployments(){
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployments.add(deployment);

		int timeout = 3600;

		System.setProperty(ConfigKey.DEPLOYMENT_IN_PROGRESS_TIMEOUT.getValue(), Integer.toString(timeout));
		
		when(deploymentBoundary.getDeploymentsInProgressTimeoutReached()).thenReturn(deployments);

		// when
		deploymentScheduler.checkForEndlessDeployments();

		// then
		verify(deploymentBoundary, times(1)).updateDeploymentInfoAndSendNotification(eq(GenerationModus.DEPLOY), eq(Integer.valueOf(1)),
				matches("Deployment was marked as failed because it reached the deplyoment timeout \\(" + timeout + " s\\).*"),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.<DeploymentFailureReason>eq(DeploymentFailureReason.TIMEOUT));
		verify(log, times(2)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	@Test
	public void test_checkForDeployments_2Deployments() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		ResourceEntity resource =  ResourceFactory.createNewResource("testResource");

		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setResourceGroup(resource.getResourceGroup());
		deployment.setContext(contextEntity);

		DeploymentEntity deployment2 = new DeploymentEntity();
		deployment2.setId(Integer.valueOf(2));
		deployment2.setResourceGroup(resource.getResourceGroup());
		deployment2.setContext(contextEntity);

		deployments.add(deployment);
		deployments.add(deployment2);

		when(deploymentBoundary.getDeploymentsToSimulate()).thenReturn(deployments);
		when(deploymentBoundary.getDeploymentsToExecute()).thenReturn(deployments);
		when(deploymentBoundary.getPreDeploymentsToExecute()).thenReturn(deployments);

		// when
		deploymentScheduler.triggerDeyploymentsAndTests();

		// then
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.DEPLOY);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(2), GenerationModus.DEPLOY);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.SIMULATE);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(2), GenerationModus.SIMULATE);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.PREDEPLOY);
		verify(deploymentExecuterService, times(1)).generateConfigurationAndExecuteDeployment(Integer.valueOf(2), GenerationModus.PREDEPLOY);
		verify(log, times(9)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}
	
	@Test
	public void test_checkForDeploymentsAndTests_2Deployments_SchedulerConfigDisabled() {
		// given
		
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.DEPLOYMENT_SCHEDULER_DISABLED.getValue(), "true");
		System.setProperties(props);
		// when
		deploymentScheduler.triggerDeyploymentsAndTests();

		// then
		verify(deploymentExecuterService, never()).generateConfigurationAndExecuteDeployment(any(Integer.class), any(GenerationModus.class));
		verify(log, never()).log(any(Level.class), anyString());
		//remove Systemconfig
		System.getProperties().remove(ConfigKey.DEPLOYMENT_SCHEDULER_DISABLED.getValue());
	}

	@Test
	public void test_checkForPendingDeploymentsAndTest_2Deployments() {
		// given
		List<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		ResourceEntity resource =  ResourceFactory.createNewResource("testResource");

		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setResourceGroup(resource.getResourceGroup());
		deployment.setContext(contextEntity);

		DeploymentEntity deployment2 = new DeploymentEntity();
		deployment2.setId(Integer.valueOf(2));
		deployment2.setResourceGroup(resource.getResourceGroup());
		deployment2.setContext(contextEntity);

		deployments.add(deployment);
		deployments.add(deployment2);

		int timeout = 3600;

		when(deploymentBoundary.getDeploymentsInProgressTimeoutReached()).thenReturn(deployments);
		System.setProperty(ConfigKey.DEPLOYMENT_IN_PROGRESS_TIMEOUT.getValue(), Integer.toString(timeout));

		// when
		deploymentScheduler.checkForPendingDeploymentsAndTest();

		// then
		verify(deploymentBoundary, times(1)).updateDeploymentInfoAndSendNotification(eq(GenerationModus.DEPLOY), eq(Integer.valueOf(1)),
				matches("Deployment was marked as failed because it reached the deplyoment timeout \\(" + timeout + " s\\).*"),
				eq(deployment.getResource() != null ? deployment.getResource().getId() : null), ArgumentMatchers.isNull(),
				ArgumentMatchers.<DeploymentFailureReason>eq(DeploymentFailureReason.TIMEOUT));
		verify(deploymentBoundary, times(1)).updateDeploymentInfoAndSendNotification(eq(GenerationModus.DEPLOY), eq(Integer.valueOf(2)),
				matches("Deployment was marked as failed because it reached the deplyoment timeout \\(" + timeout + " s\\).*"),
				eq(deployment.getResource() != null ? deployment.getResource().getId() : null), ArgumentMatchers.isNull(),
				ArgumentMatchers.<DeploymentFailureReason>eq(DeploymentFailureReason.TIMEOUT));
		verify(deploymentBoundary, times(0)).updateDeploymentInfoAndSendNotification(eq(GenerationModus.PREDEPLOY), anyInt(),
				anyString(), anyInt(), ArgumentMatchers.<GenerationResult>any(), ArgumentMatchers.<DeploymentFailureReason>any());
		
		verify(log, times(3)).log(argThat(matchesLevel(Level.INFO)), anyString());
	}

	private static BaseMatcher<Level> matchesLevel(final Level expectedLevel) {

		return new BaseMatcher<Level>() {

			protected Object theExpected = expectedLevel;

			@Override
			public boolean matches(Object actual) {
				return expectedLevel == (Level) actual;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(theExpected.toString());
			}

		};
	}

}
