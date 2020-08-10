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

package ch.puzzle.itc.mobiliar.business.generator.control;

import ch.puzzle.itc.mobiliar.builders.ShakedownTestEntityBuilder;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.globalfunction.control.GlobalFunctionService;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestExecutionResultHandlerService;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestGenerationResult;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException.MISSING;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class ShakedownTestGeneratorDomainServiceTest {

	@Mock
	Logger log;

	@Mock
	GeneratorDomainServiceWithAppServerRelations generatorDomainService;
	@Mock
	GlobalFunctionService globalFunctionService;

	@Mock
	private ShakedownTestExecutionResultHandlerService shakedownTestExecutionResultHandlerService;

	@Mock
	EntityManager entityManager;

	@InjectMocks
	ShakedownTestGeneratorDomainService service;
	
	@Mock
	ResourceDependencyResolverService resourceDependencyResolver;

	ContextEntity contextEntity;
	ResourceEntity applicationServer;
	DeploymentEntity dep;
	Date stateDate = new Date();


	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		contextEntity = new ContextEntity();
		contextEntity.setName("testContext");
		contextEntity.setId(Integer.valueOf(2));

		ResourceTypeEntity nodeType = new ResourceTypeEntity();
		nodeType.setId(1);
		nodeType.setName(DefaultResourceTypeDefinition.NODE.name());

	    ResourceTypeEntity asType = new ResourceTypeEntity();
	    asType.setId(2);
	    asType.setName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());


		ResourceEntity node = ResourceFactory.createNewResource("node1");
		node.setResourceType(nodeType);
		node.setId(1);

		ReleaseEntity release = new ReleaseEntity();
		release.setName("rel-1.0");
		release.setInstallationInProductionAt(new Date());

		applicationServer = ResourceFactory.createNewResource("testAppserver");
	    applicationServer.setResourceType(asType);
		applicationServer.setId(5);
		applicationServer.setRelease(release);
		applicationServer.getResourceGroup().setId(2);
		ConsumedResourceRelationEntity relation = applicationServer.addConsumedResourceRelation(node, null, null, ForeignableOwner.AMW);
		relation.setId(11);

		dep = mock(DeploymentEntity.class);
		when(dep.getDeploymentStateDate()).thenReturn(stateDate);
		
		when(globalFunctionService.getAllGlobalFunctionsAtDate(stateDate)).thenReturn(new ArrayList<GlobalFunctionEntity>());
	}

	@Test
	public void test_generateConfigurationForShakedownTest_missingContext() {
		// given
		ShakedownTestEntity test = ShakedownTestEntityBuilder.mockShakedownTestEntity(contextEntity, applicationServer, dep, 123, applicationServer.getResourceGroup());
		when(test.getId()).thenReturn(123);
		when(test.getResourceGroup()).thenReturn(applicationServer.getResourceGroup());
		when(entityManager.find(ResourceEntity.class, applicationServer.getId())).thenReturn(applicationServer);

		// when
		service.generateConfigurationForShakedownTest(test);

		// then
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleUnsuccessfulShakedownTest(any(GeneratorException.class), anyInt());
	}

	@Test
	public void test_generateConfigurationForShakedownTest_ioException() {
		// given
		ShakedownTestEntity test = ShakedownTestEntityBuilder.mockShakedownTestEntity(contextEntity, applicationServer, dep, null, applicationServer.getResourceGroup());

		when(entityManager.find(same(ResourceEntity.class), anyInt())).thenReturn(applicationServer);
		when(entityManager.find(same(ContextEntity.class), anyInt())).thenReturn(contextEntity);
		when(entityManager.find(same(ResourceGroupEntity.class), anyInt())).thenReturn(applicationServer.getResourceGroup());

		when(generatorDomainService.getAllEnvironments(contextEntity)).thenReturn(Collections.singletonList(contextEntity));
		when(resourceDependencyResolver.getResourceEntityForRelease(applicationServer.getResourceGroup(), applicationServer.getRelease())).thenReturn(applicationServer);
		
		IOException exception = new IOException("foo");

		try {
			when(generatorDomainService.generateApplicationServerConfigPerNode(any(GenerationContext.class))).thenThrow(exception);
		} catch (GeneratorException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		// when
		ShakedownTestGenerationResult result = service.generateConfigurationForShakedownTest(test);

		// then
		assertNull(result);
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleUnsuccessfulShakedownTest(exception, test.getId());
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleSuccessfulShakedownTest(test);
	}

	@Test
	public void test_generateConfigurationForShakedownTest_generatorException() throws GeneratorException, IOException {
		// given
		DeploymentEntity dep = mock(DeploymentEntity.class);
		Date stateDate = new Date();
		when(dep.getDeploymentStateDate()).thenReturn(stateDate);
		ShakedownTestEntity test = ShakedownTestEntityBuilder.mockShakedownTestEntity(contextEntity, applicationServer, dep, null, applicationServer.getResourceGroup());

		when(entityManager.find(ResourceEntity.class, applicationServer.getId())).thenReturn(applicationServer);
		when(entityManager.find(same(ContextEntity.class), anyInt())).thenReturn(contextEntity);
		when(entityManager.find(same(ResourceGroupEntity.class), anyInt())).thenReturn(applicationServer.getResourceGroup());

		when(generatorDomainService.getAllEnvironments(contextEntity)).thenReturn(Collections.singletonList(contextEntity));
		when(resourceDependencyResolver.getResourceEntityForRelease(applicationServer.getResourceGroup(), applicationServer.getRelease())).thenReturn(applicationServer);
		GeneratorException exception = new GeneratorException("foo", MISSING.STS_TEMPLATE);


		when(generatorDomainService.generateApplicationServerConfigPerNode(any(GenerationContext.class)))
						.thenThrow(exception);

		// when
		ShakedownTestGenerationResult result = service.generateConfigurationForShakedownTest(test);

		// then
		assertNull(result);
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleUnsuccessfulShakedownTest(exception, test.getId());
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleSuccessfulShakedownTest(test);
	}

	@Test
	public void test_generateConfigurationForShakedownTest_faultyGenerationResult() throws GeneratorException, IOException {
		// given
		DeploymentEntity dep = mock(DeploymentEntity.class);
		Date stateDate = new Date();
		when(dep.getDeploymentStateDate()).thenReturn(stateDate);
		ShakedownTestEntity test = ShakedownTestEntityBuilder.mockShakedownTestEntity(contextEntity, applicationServer, dep, null, applicationServer.getResourceGroup());

		when(entityManager.find(same(ResourceEntity.class), anyInt())).thenReturn(applicationServer);
		when(entityManager.find(same(ContextEntity.class), anyInt())).thenReturn(contextEntity);
		when(entityManager.find(same(ResourceGroupEntity.class), anyInt())).thenReturn(applicationServer.getResourceGroup());
		when(generatorDomainService.getAllEnvironments(contextEntity)).thenReturn(Collections.singletonList(contextEntity));
		when(resourceDependencyResolver.getResourceEntityForRelease(applicationServer.getResourceGroup(), applicationServer.getRelease())).thenReturn(applicationServer);
		
		NodeGenerationResult nodeResult = mock(NodeGenerationResult.class);
		when(nodeResult.hasErrors()).thenReturn(true);
		when(nodeResult.isNodeEnabled()).thenReturn(true);
		when(nodeResult.getErrorMessage()).thenReturn("foo");
		String content = "<sts><remoteHost>romai.rz.puzzle.ch</remoteHost><user>cweber</user><remoteSTPPath>/tmp/stm/stps</remoteSTPPath><testId>1900</testId><shakedowntests><shakedowntest>AMW_stp_dummy AMW_stp_dummy-0.0.1-SNAPSHOT.jar true</shakedowntest></shakedowntests></sts>";
		GeneratedTemplate template = new GeneratedTemplate("STS", "", content);
		when(nodeResult.getGeneratedTemplates()).thenReturn(Collections.singletonList(template));

		when(generatorDomainService.generateApplicationServerConfigPerNode(any(GenerationContext.class))).thenReturn(nodeResult);

		// when
		ShakedownTestGenerationResult result = service.generateConfigurationForShakedownTest(test);

		// then
		assertNotNull(result);
		assertTrue(result.hasErrors());
		verify(shakedownTestExecutionResultHandlerService, times(1)).handleUnsuccessfulShakedownTest(nodeResult.getErrorMessage(), test.getId());
		verify(shakedownTestExecutionResultHandlerService, times(0)).handleSuccessfulShakedownTest(test);
	}

}
