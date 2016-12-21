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

package ch.puzzle.itc.mobiliar.business.deploy.control;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestService;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;

public class DeploymentExecutionResultHandlerServiceTest {

	@InjectMocks
	DeploymentExecutionResultHandlerService deploymentExecutionResultHandlerService;

	@Mock
	Logger log;

	@Mock
	DeploymentService deploymentService;

	@Mock
	ShakedownTestService shakedownTestService;

	DeploymentEntity deployment;
	GenerationResult result;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(100));

		result = new GenerationResult();
		result.setDeployment(deployment);

	}

	@Test
	public void handleSuccessfulDeployment_noSTP() {
		// given

		// when
		deploymentExecutionResultHandlerService.handleSuccessfulDeployment(GenerationModus.DEPLOY, result);

		// then
		verify(deploymentService, times(1)).updateDeploymentInfoAndSendNotification(GenerationModus.DEPLOY, deployment.getId(), null,
				deployment.getResource() != null ? deployment.getResource().getId() : null, result);
	}

	@Test
	public void handleSuccessfulDeployment_noSTP_simulation() {
		// given

		// when
		deploymentExecutionResultHandlerService.handleSuccessfulDeployment(GenerationModus.SIMULATE, result);

		// then
		verify(deploymentService, times(1)).updateDeploymentInfoAndSendNotification(GenerationModus.SIMULATE, deployment.getId(), null,
				deployment.getResource() != null ? deployment.getResource().getId() : null, result);
	}

	@Test
	public void handleSuccessfulDeployment_STP() {
		// given
		deployment.setCreateTestAfterDeployment(true);

		// when
		deploymentExecutionResultHandlerService.handleSuccessfulDeployment(GenerationModus.DEPLOY, result);

		// then
		verify(deploymentService, times(1)).updateDeploymentInfoAndSendNotification(GenerationModus.DEPLOY, deployment.getId(), null,
				deployment.getResource() != null ? deployment.getResource().getId() : null, result);
		verify(deploymentService, times(1)).createShakedownTestForTrackinIdOfDeployment(deployment.getTrackingId());
	}

	@Test
	public void handleUnSuccessfulDeployment_deploy_true() {
		// given
		Exception e = new Exception();
		// when
		deploymentExecutionResultHandlerService.handleUnSuccessfulDeployment(GenerationModus.DEPLOY, deployment, result, e);

		// then
		verify(deploymentService, times(1)).updateDeploymentInfoAndSendNotification(GenerationModus.DEPLOY, deployment.getId(),
				"Deployment(100) failed \nnull",
				deployment.getResource() != null ? deployment.getResource().getId() : null, result);
		verify(log, times(1)).log(Level.SEVERE, "Deployment(100) failed \nnull", e);
	}

	@Test
	public void handleUnSuccessfulDeployment_deploy_false() {
		// given
		Exception e = new Exception();
		// when
		deploymentExecutionResultHandlerService.handleUnSuccessfulDeployment(GenerationModus.SIMULATE, deployment, result, e);

		// then
		verify(deploymentService, times(1)).updateDeploymentInfoAndSendNotification(GenerationModus.SIMULATE, deployment.getId(),
				"Build(100) failed \nnull",
				deployment.getResource() != null ? deployment.getResource().getId() : null, result);
		verify(log, times(1)).log(Level.SEVERE, "Build(100) failed \nnull", e);
	}

}
