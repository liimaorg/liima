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

package ch.mobi.itc.mobiliar.rest.deployments;

import ch.mobi.itc.mobiliar.rest.dtos.AppWithVersionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentRequestDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.EnvironmentsScreenDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorDomainServiceWithAppServerRelations;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class DeploymentTest {

	@InjectMocks
	private DeploymentsRest deploymentRestService;
	@Mock
	private DeploymentBoundary deploymentBoundary;
	@Mock
	private GeneratorDomainServiceWithAppServerRelations generatorDomainServiceWithAppServerRelations;
	@Mock
	private EnvironmentsScreenDomainService environmentsService;
	@Mock
	private ApplicationServer applicationServer;
	@Mock
	private ReleaseMgmtService releaseService;
	@Mock
	private ResourceDependencyResolverService dependencyResolverService;
	@Mock
	private ResourceTypeProvider resourceTypeProvider;
	@Mock
	private ResourceGroupPersistenceService resourceGroupService;
	@Mock
	private ContextDomainService contextDomainService;


	private HashSet<DeploymentEntity> entities;
	private DeploymentEntity deploymentEntity;
	private DeploymentDTO deploymentDto;
	private DeploymentRequestDTO deploymentRequestDto;
	private ReleaseEntity release;
	
	@Before
	public void configure() throws IOException, GeneralDBException, ResourceNotFoundException {
		MockitoAnnotations.initMocks(this);

		// Test data
		entities = new HashSet<DeploymentEntity>();
		deploymentEntity = new DeploymentEntity();
		LinkedList<DeploymentEntity.ApplicationWithVersion> appsWithVersion = new LinkedList<DeploymentEntity.ApplicationWithVersion>();
		appsWithVersion.add(new ApplicationWithVersion("test", 123, "1.2.3"));
		deploymentEntity.setApplicationsWithVersion(appsWithVersion);
		deploymentEntity.setDeploymentParameters(new LinkedList<DeploymentParameter>());
		deploymentEntity.setId(123);
		deploymentEntity.setDeploymentDate(new Date());
		deploymentEntity.setStateToDeploy(new Date());
		deploymentEntity.setSimulating(false);
		deploymentEntity.setShakedownTests(null);
		deploymentEntity.setTrackingId(321);
		ResourceEntity targetPlatform = ResourceFactory.createNewResource("test");
		deploymentEntity.setRuntime(targetPlatform);
		deploymentEntity.setDeploymentCancelUser("u123456");
		deploymentEntity.setDeploymentRequestUser("u123457");
		deploymentEntity.setDeploymentConfirmationUser("u123458");
		deploymentEntity.setNodeJobs(new HashSet<NodeJobEntity>());
		release = new ReleaseEntity();
		release.setId(2);
		release.setName("RL-13.04");
		deploymentEntity.setRelease(release);
		entities.add(deploymentEntity);

		ResourceEntity resource =  ResourceFactory.createNewResource("test");
		resource.setId(1);
		deploymentEntity.setResourceGroup(resource.getResourceGroup());

		ContextEntity context = new ContextEntity();
		context.setName("test");
		deploymentEntity.setContext(context);

		deploymentDto = new DeploymentDTO(deploymentEntity);

		deploymentRequestDto = new DeploymentRequestDTO();
		deploymentRequestDto.setAppServerName(deploymentEntity.getResourceGroup().getName());
		LinkedList<AppWithVersionDTO> apps = new LinkedList<AppWithVersionDTO>();
		apps.add(new AppWithVersionDTO(appsWithVersion.getFirst().getApplicationName(), appsWithVersion.getFirst().getVersion()));
		deploymentRequestDto.setAppsWithVersion(apps);
		deploymentRequestDto.setDeploymentDate(deploymentEntity.getDeploymentDate());
		deploymentRequestDto.setEnvironmentName(deploymentEntity.getContext().getName());
		deploymentRequestDto.setExecuteShakedownTest(deploymentEntity.isCreateTestAfterDeployment());
		deploymentRequestDto.setNeighbourhoodTest(
				deploymentEntity.isCreateTestForNeighborhoodAfterDeployment());
		deploymentRequestDto.setRequestOnly(false);
		deploymentRequestDto.setSendEmail(deploymentEntity.isSendEmail());
		deploymentRequestDto.setSimulate(deploymentEntity.isSimulating());

		//mock app
		LinkedList<Application> applications = new LinkedList<Application>();
		Application application = mock(Application.class);
		when(application.getName()).thenReturn(appsWithVersion.getFirst().getApplicationName());
		applications.add(application);
		

		//mock as
		when(applicationServer.getName()).thenReturn(deploymentEntity.getResourceGroup().getName());
		when(applicationServer.getAMWApplications()).thenReturn(applications);
//		when(appServerService.getApplicationServerById(anyInt())).thenReturn(applicationServer);
		
		// resource type
		ResourceTypeEntity defaultAS = mock(ResourceTypeEntity.class);
		when(defaultAS.getId()).thenReturn(1);
		when(resourceTypeProvider.getOrCreateDefaultResourceType(
				DefaultResourceTypeDefinition.APPLICATIONSERVER)).thenReturn(defaultAS);
		
		// resource group
		when(resourceGroupService.loadUniqueGroupByNameAndType(anyString(), anyInt())).thenReturn(resource.getResourceGroup());
		
		when(dependencyResolverService.getResourceEntityForRelease(any(ResourceGroupEntity.class),
				any(ReleaseEntity.class))).thenReturn(resource);
		when(dependencyResolverService.findMostRelevantRelease((TreeSet<ReleaseEntity>) any(), (Date) any())).thenReturn(release);
	     when(dependencyResolverService
				.getConsumedRelatedResourcesByResourceType(Mockito.any(ResourceEntity.class),
						Mockito.any(DefaultResourceTypeDefinition.class),
								Mockito.any(ReleaseEntity.class))).thenAnswer(new Answer<Set<ResourceEntity>>() {
		    @Override public Set<ResourceEntity> answer(InvocationOnMock invocation) throws Throwable {
			   ResourceEntity appServer = (ResourceEntity)invocation.getArguments()[0];
			   HashSet<ResourceEntity> set = new HashSet<ResourceEntity>();
			   for(Application a : applicationServer.getAMWApplications()){
				  final String name = a.getName();
				  ResourceEntity resMock = Mockito.mock(ResourceEntity.class);
				  when(resMock.getName()).thenReturn(name);
				  set.add(resMock);
			   }
			   return set;
		    }
		});
		
		ContextEntity global = mock(ContextEntity.class);
		when(contextDomainService.getGlobalResourceContextEntity()).thenReturn(global);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getDeploymentsBasic() {
		when(deploymentBoundary.getFilteredDeployments(true, null, null, new LinkedList<CustomFilter>(), null, null, null)).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(new HashSet<DeploymentEntity>(), 0));

		Response response = deploymentRestService.getDeployments(null, null, null, null, null, null, null, null, null, null, null, null, false);
		ArrayList<DeploymentDTO> deploymentDtos = (ArrayList<DeploymentDTO>) response.getEntity();
		LinkedList<Integer> metaList = new LinkedList<Integer>();
		metaList.add(0);

		assertEquals(200, response.getStatus());
		assertEquals(0, deploymentDtos.size());
		assertEquals(metaList, response.getMetadata().get("X-Total-Count"));
	}

	@Test
	public void verifyThatDeploymentServiceIsCalledWithNonEmptyFilterIfDeploymentParametersAreSet() {
        when(deploymentBoundary.getFilteredDeployments(eq(true), anyInt(), anyInt(), anyList(), anyString(), any(CommonFilterService.SortingDirectionType.class), anyList())).thenReturn(
                new Tuple<Set<DeploymentEntity>, Integer>(new HashSet<DeploymentEntity>(), 0));
		deploymentRestService.getDeployments(null, null, null, null, null, null, null, null, null, null, Collections.singletonList("TEST"), null, false);

        verify(deploymentBoundary, never()).getFilteredDeployments(true, null, null, Collections.<CustomFilter>emptyList(), null, null, null);
		verify(deploymentBoundary, times(1)).getFilteredDeployments(eq(true), anyInt(), anyInt(), anyList(), anyString(), any(CommonFilterService.SortingDirectionType.class), anyList());
	}

	@Test
	public void verifyThatDeploymentServiceIsCalledWithNonEmptyFilterIfDeploymentParameterValuesAreSet() {
		when(deploymentBoundary.getFilteredDeployments(eq(true), anyInt(), anyInt(), anyList(), anyString(), any(CommonFilterService.SortingDirectionType.class), anyList())).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(new HashSet<DeploymentEntity>(), 0));
		deploymentRestService.getDeployments(null, null, null, null, null, null, null, null, null, null, null, Collections.singletonList("TESTVALUE"), false);

		verify(deploymentBoundary, never()).getFilteredDeployments(true, null, null, Collections.<CustomFilter>emptyList(), null, null, null);
		verify(deploymentBoundary, times(1)).getFilteredDeployments(eq(true), anyInt(), anyInt(), anyList(), anyString(), any(CommonFilterService.SortingDirectionType.class), anyList());
	}

	@Test
	public void verifyThatDeploymentServiceIsCalledWithNonEmptyFilterIfDeploymentParametersAndDeploymentParameterValuesAreSet() {
		when(deploymentBoundary.getFilteredDeployments(eq(true), anyInt(), anyInt(), anyList(), anyString(), any(CommonFilterService.SortingDirectionType.class), anyList())).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(new HashSet<DeploymentEntity>(), 0));
		deploymentRestService.getDeployments(null, null, null, null, null, null, null, null, null, null, Collections.singletonList("TEST"), Collections.singletonList("TESTVALUE"), false);

		verify(deploymentBoundary, never()).getFilteredDeployments(true, null, null, Collections.<CustomFilter>emptyList(), null, null, null);
		verify(deploymentBoundary, times(1)).getFilteredDeployments(eq(true), anyInt(), anyInt(), anyList(), anyString(), any(CommonFilterService.SortingDirectionType.class), anyList());
	}

	@Test
	public void getDeployment() {
		when(deploymentBoundary.getDeploymentById(deploymentEntity.getId())).thenReturn(deploymentEntity);

		Response response = deploymentRestService.getDeployment(deploymentEntity.getId());
		assertEquals(200, response.getStatus());
		
		DeploymentDTO deploymentDto = (DeploymentDTO) response.getEntity();
		assertEquals(deploymentEntity.getId(), deploymentDto.getId());

	}

	@Test
	public void getDeploymentNoResult() {
		when(deploymentBoundary.getDeploymentById(234)).thenThrow(new NoResultException());

		Response response = deploymentRestService.getDeployment(234);
		assertEquals(404, response.getStatus());
		
	    ExceptionDto exception = (ExceptionDto) response.getEntity();
	    assertTrue(exception.getMessage().length() > 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void addDeployment() throws GeneralDBException {
		ReleaseEntity release = mockRelease();
		when(
				deploymentBoundary.createDeploymentReturnTrackingId(anyInt(), anyInt(), any(Date.class), any(Date.class), any(LinkedList.class),
						any(LinkedList.class), any(ArrayList.class),
						anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(deploymentEntity.getTrackingId());
		
		when(environmentsService.getContextByName(deploymentEntity.getContext().getName())).thenReturn(deploymentEntity.getContext());
		when(
				deploymentBoundary.getFilteredDeployments(anyBoolean(), anyInt(), anyInt(), any(LinkedList.class), anyString(),
						any(CommonFilterService.SortingDirectionType.class), any(LinkedList.class))).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(entities, entities.size()));
		when(releaseService.findByName(anyString())).thenReturn(release);

		when(generatorDomainServiceWithAppServerRelations.hasActiveNodeToDeployOnAtDate(any(ResourceEntity.class), any(ContextEntity.class), any(Date.class))).thenReturn(Boolean.TRUE);
		
		Response response = deploymentRestService.addDeployment(deploymentRequestDto);

		assertEquals(201, response.getStatus());
		DeploymentDTO responseDto = (DeploymentDTO) response.getEntity();
		assertEquals(responseDto.getId(), deploymentDto.getId());

		LinkedList<String> metaList = new LinkedList<String>();
		metaList.add("/deployments/" + deploymentEntity.getId());
		assertEquals(metaList, response.getMetadata().get("Location"));
	}
	
	@Test
	public void addDeployment_no_active_node() throws GeneralDBException {
		ReleaseEntity release = mockRelease();
		when(
				deploymentBoundary.createDeploymentReturnTrackingId(anyInt(), anyInt(), any(Date.class), any(Date.class), any(LinkedList.class),
						any(LinkedList.class), any(ArrayList.class),
						anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(deploymentEntity.getTrackingId());
		
		when(environmentsService.getContextByName(deploymentEntity.getContext().getName())).thenReturn(deploymentEntity.getContext());
		when(
				deploymentBoundary.getFilteredDeployments(anyBoolean(), anyInt(), anyInt(), any(LinkedList.class), anyString(),
						any(CommonFilterService.SortingDirectionType.class), any(LinkedList.class))).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(entities, entities.size()));
		when(releaseService.findByName(anyString())).thenReturn(release);

		when(generatorDomainServiceWithAppServerRelations.hasActiveNodeToDeployOnAtDate(any(ResourceEntity.class), any(ContextEntity.class), any(Date.class))).thenReturn(Boolean.FALSE);
		
		Response response = deploymentRestService.addDeployment(deploymentRequestDto);

		assertEquals(400, response.getStatus());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addDeploymentWithOutRelease() throws GeneralDBException {
		ReleaseEntity release = mockRelease();
		when(
				deploymentBoundary.createDeploymentReturnTrackingId(anyInt(), anyInt(), any(Date.class), any(Date.class), any(LinkedList.class),
						any(LinkedList.class), any(ArrayList.class), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(
				deploymentEntity.getTrackingId());
		when(environmentsService.getContextByName(deploymentEntity.getContext().getName())).thenReturn(deploymentEntity.getContext());
		when(
				deploymentBoundary.getFilteredDeployments(anyBoolean(), anyInt(), anyInt(), any(LinkedList.class), anyString(),
						any(CommonFilterService.SortingDirectionType.class), any(LinkedList.class))).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(entities, entities.size()));
		when(releaseService.findByName(null)).thenReturn(null);
		when(releaseService.loadAllReleases(false)).thenReturn(Collections.singletonList(release));
		when(generatorDomainServiceWithAppServerRelations.hasActiveNodeToDeployOnAtDate(any(ResourceEntity.class), any(ContextEntity.class), any(Date.class))).thenReturn(Boolean.TRUE);
		

		Response response = deploymentRestService.addDeployment(deploymentRequestDto);

		assertEquals(201, response.getStatus());
		DeploymentDTO responseDto = (DeploymentDTO) response.getEntity();
		assertEquals(responseDto.getId(), deploymentDto.getId());

		LinkedList<String> metaList = new LinkedList<String>();
		metaList.add("/deployments/" + deploymentEntity.getId());
		assertEquals(metaList, response.getMetadata().get("Location"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addDeploymentWrongAppName() throws GeneralDBException {
		DeploymentRequestDTO wrongdeploymentRequestDto = new DeploymentRequestDTO(deploymentRequestDto);
		ReleaseEntity release = mockRelease();
		wrongdeploymentRequestDto.getAppsWithVersion().get(0).setApplicationName("wrong");
		
		when(
				deploymentBoundary.createDeploymentReturnTrackingId(anyInt(), anyInt(), any(Date.class), any(Date.class), any(LinkedList.class),
						any(LinkedList.class), any(ArrayList.class),
						anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(deploymentEntity.getTrackingId());
		when(environmentsService.getContextByName(deploymentEntity.getContext().getName())).thenReturn(deploymentEntity.getContext());
		when(
				deploymentBoundary.getFilteredDeployments(anyBoolean(), anyInt(), anyInt(), any(LinkedList.class), anyString(),
						any(CommonFilterService.SortingDirectionType.class), any(LinkedList.class))).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(entities, entities.size()));
		when(releaseService.findByName(anyString())).thenReturn(release);

		Response response = deploymentRestService.addDeployment(wrongdeploymentRequestDto);
		assertEquals(400, response.getStatus());
		
	    ExceptionDto exception = (ExceptionDto) response.getEntity();
	    assertTrue(exception.getDetail().length() > 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addDeploymentNoAppInAs() throws GeneralDBException {
		ReleaseEntity release = mockRelease();
		when(
				deploymentBoundary.createDeploymentReturnTrackingId(anyInt(), anyInt(), any(Date.class), any(Date.class), any(LinkedList.class),
						any(LinkedList.class), any(ArrayList.class),
						anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(deploymentEntity.getTrackingId());
		when(environmentsService.getContextByName(deploymentEntity.getContext().getName())).thenReturn(deploymentEntity.getContext());
		when(
				deploymentBoundary.getFilteredDeployments(anyBoolean(), anyInt(), anyInt(), any(LinkedList.class), anyString(),
						any(CommonFilterService.SortingDirectionType.class), any(LinkedList.class))).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(entities, entities.size()));
		//the as has no apps
		when(applicationServer.getAMWApplications()).thenReturn(new LinkedList<Application>());
		when(releaseService.findByName(anyString())).thenReturn(release);
		
		Response response = deploymentRestService.addDeployment(deploymentRequestDto);
		assertEquals(400, response.getStatus());
		
	    ExceptionDto exception = (ExceptionDto) response.getEntity();
	    assertTrue(exception.getDetail().length() > 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addDeploymentNotAllAppsInRequest() throws GeneralDBException {
		
		//mock app
		List<Application> applications = applicationServer.getAMWApplications();
		Application application = mock(Application.class);
		when(application.getName()).thenReturn("noInRequest");
		applications.add(application);

		when(
				deploymentBoundary.createDeploymentReturnTrackingId(anyInt(), anyInt(), any(Date.class), any(Date.class), any(LinkedList.class),
						any(LinkedList.class), any(ArrayList.class),
						anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(deploymentEntity.getTrackingId());
		when(environmentsService.getContextByName(deploymentEntity.getContext().getName())).thenReturn(deploymentEntity.getContext());
		when(
				deploymentBoundary.getFilteredDeployments(anyBoolean(), anyInt(), anyInt(), any(LinkedList.class), anyString(),
						any(CommonFilterService.SortingDirectionType.class), any(LinkedList.class))).thenReturn(
				new Tuple<Set<DeploymentEntity>, Integer>(entities, entities.size()));
		when(releaseService.findByName(anyString())).thenReturn(release);

		Response response = deploymentRestService.addDeployment(deploymentRequestDto);
		assertEquals(400, response.getStatus());
		
	    ExceptionDto exception = (ExceptionDto) response.getEntity();
	    assertTrue(exception.getDetail().length() > 0);
	}
	
	private ReleaseEntity mockRelease(){
		ReleaseEntity mock = mock(ReleaseEntity.class);
		Calendar cal = new GregorianCalendar();
		cal.set(2014, 05, 01);
		when(mock.getId()).thenReturn(2);
		when(mock.getName()).thenReturn("RL-13.04");
		when(mock.getInstallationInProductionAt()).thenReturn(cal.getTime());
		return mock;
	}

}
