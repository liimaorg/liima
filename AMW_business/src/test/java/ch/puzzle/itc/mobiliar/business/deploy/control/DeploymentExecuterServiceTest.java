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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentService;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorDomainServiceWithAppServerRelations;
import ch.puzzle.itc.mobiliar.business.generator.control.LockingService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;

public class DeploymentExecuterServiceTest {

	@InjectMocks
	DeploymentExecuterService deploymentExecuterService;

	@Mock
	GeneratorDomainServiceWithAppServerRelations generatorDomainServiceWithAppServerRelations;

	@Mock
	GeneratorDomainServiceWithAppServerRelations withRelations;

	@Mock
	DeploymentAsynchronousExecuter deploymentAsynchronousExecuter;

	@Mock
	DeploymentExecutionResultHandlerService deploymentExecutionResultHandler;

	@Mock
	DeploymentService deploymentService;

	@Mock
	LockingService locking;

	@Mock
	EntityManager entityManager;

	@Mock
	Logger log;

	DeploymentEntity deployment;

	ContextEntity contextEntity;

	ResourceEntity applicationServer;

	ResourceEntity deploymentResource;

	ResourceEntity targetPlatformEntity;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		contextEntity = new ContextEntity();
		contextEntity.setName("testContext");
		contextEntity.setId(Integer.valueOf(2));

		deploymentResource = ResourceFactory.createNewResource("testResource");
		deploymentResource.setId(Integer.valueOf(1));

		targetPlatformEntity = ResourceFactory.createNewResource();
		targetPlatformEntity.setId(Integer.valueOf(22));

		applicationServer =  ResourceFactory.createNewResource("testAppserver");
		applicationServer.setId(Integer.valueOf(5));

		deployment = new DeploymentEntity();
		deployment.setResourceGroup(deploymentResource.getResourceGroup());
		deployment.setId(Integer.valueOf(100));
		deployment.setContext(contextEntity);
		Date deploymentDate = new Date();
		deployment.setDeploymentDate(deploymentDate);
		deployment.setRuntime(targetPlatformEntity);

		List<ApplicationWithVersion> applicationsWithVersion = new ArrayList<DeploymentEntity.ApplicationWithVersion>();

		ApplicationWithVersion applicationWithVersion = new ApplicationWithVersion("testAppname", Integer.valueOf(1), "1.1.1");
		applicationsWithVersion.add(applicationWithVersion);

		deployment.setApplicationsWithVersion(applicationsWithVersion);
	}

	@Test
	public void test_executeDeployment_No_DeploymentFound() {
		// given

		when(deploymentService.getDeploymentById(Integer.valueOf(1))).thenReturn(null);

		when(generatorDomainServiceWithAppServerRelations.generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class))).thenReturn(null);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(Integer.valueOf(1), GenerationModus.DEPLOY);

		// then
		verify(deploymentService, times(1)).getDeploymentById(Integer.valueOf(1));
		verify(generatorDomainServiceWithAppServerRelations, times(0)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(0))
		.executeDeployment(any(GenerationResult.class), any(DeploymentEntity.class), any(GenerationModus.class));
		verify(locking, times(0)).lockDeploymentForExecution(any(Integer.class), any(GenerationModus.class));
		verify(log, times(1)).log(any(Level.class), anyString());

	}

	@Test
	public void test_executeDeployment_lockingNok() {
		// given
		when(deploymentService.getDeploymentById(deployment.getId())).thenReturn(deployment);

		when(locking.lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY)).thenReturn(false);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(deployment.getId(), GenerationModus.DEPLOY);

		// then
		verify(deploymentService, times(1)).getDeploymentById(deployment.getId());
		verify(generatorDomainServiceWithAppServerRelations, times(0)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(0))
		.executeDeployment(any(GenerationResult.class), any(DeploymentEntity.class), any(GenerationModus.class));
		verify(locking, times(1)).lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY);
		verify(log, times(0)).log(any(Level.class), anyString());

	}

	@Test
	public void test_executeDeployment_No_callable_locking_ok() {
		// given
		when(deploymentService.getDeploymentById(deployment.getId())).thenReturn(deployment);

		when(generatorDomainServiceWithAppServerRelations.generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class))).thenReturn(null);

		when(locking.lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY)).thenReturn(true);
		when(entityManager.find(DeploymentEntity.class, deployment.getId())).thenReturn(deployment);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(deployment.getId(), GenerationModus.DEPLOY);
		

		// then
		verify(deploymentService, times(1)).getDeploymentById(deployment.getId());
		verify(generatorDomainServiceWithAppServerRelations, times(1)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(0))
		.executeDeployment(any(GenerationResult.class), any(DeploymentEntity.class), any(GenerationModus.class));
		verify(locking, times(1)).lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY);
		verify(log, times(0)).log(any(Level.class), anyString());

	}

	@Test
	public void test_executeDeployment_callable() {
		when(deploymentService.getDeploymentById(deployment.getId())).thenReturn(deployment);
		// given
		GenerationResult result = new GenerationResult();

		when(generatorDomainServiceWithAppServerRelations.generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class))).thenReturn(result);

		when(locking.lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY)).thenReturn(true);
		when(entityManager.find(DeploymentEntity.class, deployment.getId())).thenReturn(deployment);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(deployment.getId(), GenerationModus.DEPLOY);

		// then
		verify(deploymentService, times(1)).getDeploymentById(deployment.getId());
		verify(generatorDomainServiceWithAppServerRelations, times(1)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(1)).executeDeployment(result, deployment, GenerationModus.DEPLOY);
		verify(locking, times(1)).lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY);
		verify(log, times(0)).log(any(Level.class), anyString());
	}

	@Test
	public void test_executeDeployment_callable_Exception() {
		when(deploymentService.getDeploymentById(deployment.getId())).thenReturn(deployment);
		// given
		RuntimeException e = new RuntimeException();

		when(generatorDomainServiceWithAppServerRelations.generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class))).thenThrow(e);

		when(locking.lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY)).thenReturn(true);
		when(entityManager.find(DeploymentEntity.class, deployment.getId())).thenReturn(deployment);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(deployment.getId(), GenerationModus.DEPLOY);

		// then
		verify(deploymentService, times(1)).getDeploymentById(deployment.getId());
		verify(generatorDomainServiceWithAppServerRelations, times(1)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(0)).executeDeployment(any(GenerationResult.class), any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentExecutionResultHandler, times(1)).handleUnSuccessfulDeployment(GenerationModus.DEPLOY, deployment,null, e);
		verify(locking, times(1)).lockDeploymentForExecution(deployment.getId(), GenerationModus.DEPLOY);
	}

	@Test
	public void test_executeDeployment_callable_deploy_false() {
		when(deploymentService.getDeploymentById(deployment.getId())).thenReturn(deployment);
		// given
		GenerationResult result = new GenerationResult();


		when(generatorDomainServiceWithAppServerRelations.generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class))).thenReturn(result);

		when(locking.lockDeploymentForExecution(deployment.getId(), GenerationModus.SIMULATE)).thenReturn(true);
		when(entityManager.find(DeploymentEntity.class, deployment.getId())).thenReturn(deployment);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(deployment.getId(), GenerationModus.SIMULATE);

		// then
		verify(deploymentService, times(1)).getDeploymentById(deployment.getId());
		verify(generatorDomainServiceWithAppServerRelations, times(1)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(1)).executeDeployment(result, deployment, GenerationModus.SIMULATE);
		verify(locking, times(1)).lockDeploymentForExecution(deployment.getId(), GenerationModus.SIMULATE);
		verify(log, times(0)).log(any(Level.class), anyString());
	}

	@Test
	public void test_executeDeployment_callable_deploy_with_errors() {
		when(deploymentService.getDeploymentById(deployment.getId())).thenReturn(deployment);
		// given

		GenerationResult result = Mockito.mock(GenerationResult.class);
		when(result.hasErrors()).thenReturn(Boolean.TRUE);

		when(generatorDomainServiceWithAppServerRelations.generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class))).thenReturn(result);

		when(locking.lockDeploymentForExecution(deployment.getId(), GenerationModus.SIMULATE)).thenReturn(true);
		when(entityManager.find(DeploymentEntity.class, deployment.getId())).thenReturn(deployment);

		// when
		deploymentExecuterService.generateConfigurationAndExecuteDeployment(deployment.getId(), GenerationModus.SIMULATE);

		// then
		verify(deploymentService, times(1)).getDeploymentById(deployment.getId());
		verify(generatorDomainServiceWithAppServerRelations, times(1)).generateConfigurationForDeployment(any(DeploymentEntity.class), any(GenerationModus.class));
		verify(deploymentAsynchronousExecuter, times(0)).executeDeployment(result, deployment, GenerationModus.SIMULATE);
		verify(locking, times(1)).lockDeploymentForExecution(deployment.getId(), GenerationModus.SIMULATE);
		verify(log, times(0)).log(any(Level.class), anyString());
	}
}
