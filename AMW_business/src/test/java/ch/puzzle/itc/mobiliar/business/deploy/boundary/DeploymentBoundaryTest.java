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

package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import ch.puzzle.itc.mobiliar.builders.DeploymentEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentOrder;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService;
import ch.puzzle.itc.mobiliar.business.domain.commons.Sort;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFailureReason;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.utils.database.DatabaseUtil;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.domain.commons.Sort.SortingDirectionType.DESC;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentBoundaryTest
{
	
	@InjectMocks
	@Spy
	private DeploymentBoundary deploymentBoundary;

	@Mock
	private PermissionService permissionService;

	@Mock
	private CommonFilterService commonFilterService;

	@Mock
	private DatabaseUtil databaseUtil;

	@Mock
	private Logger log;

	@Mock
	private EntityManager em;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void deploymentCleanupEmptyFolder() throws Exception {
		Properties properties = System.getProperties();
		File generatorFolder = tempFolder.newFolder();
		
		// given
		Integer cleanupAge = 10;
		properties.setProperty(ConfigKey.GENERATOR_PATH.getValue(), generatorFolder.getCanonicalPath().toString());
		properties.setProperty(ConfigKey.GENERATOR_PATH_SIMULATION.getValue(), generatorFolder.getCanonicalPath().toString());
		properties.setProperty(ConfigKey.GENERATOR_PATH_TEST.getValue(), generatorFolder.getCanonicalPath().toString());
		properties.setProperty(ConfigKey.DEPLOYMENT_CLEANUP_AGE.getValue(), cleanupAge.toString());
		System.setProperties(properties);

		// when
        deploymentBoundary.cleanupDeploymentFiles();
        
        // then
		assertTrue(generatorFolder.exists());
	}
	
	@Test
	public void deploymentCleanup() throws Exception {
		Date now = new Date();
		Properties properties = System.getProperties();
		File generatorFolder = tempFolder.newFolder();
		
		// given
		Integer cleanupAge = 10;
		properties.setProperty(ConfigKey.GENERATOR_PATH.getValue(), generatorFolder.getCanonicalPath().toString());
		properties.setProperty(ConfigKey.GENERATOR_PATH_SIMULATION.getValue(), generatorFolder.getCanonicalPath().toString());
		properties.setProperty(ConfigKey.GENERATOR_PATH_TEST.getValue(), generatorFolder.getCanonicalPath().toString());
		properties.setProperty(ConfigKey.DEPLOYMENT_CLEANUP_AGE.getValue(), cleanupAge.toString());
		System.setProperties(properties);
   
		// create folders
		File deployment1 = new File(generatorFolder, "appOne" + File.separator + "deployment1");
		File deployment2 = new File(generatorFolder, "appOne" + File.separator + "deployment2");
		deployment1.mkdirs();
		deployment2.mkdirs();

		// create files
		File testFile1 = new File(deployment1, "test1.txt");
		File testFile2 = new File(deployment2, "test2.txt");
		testFile1.createNewFile();
		testFile2.createNewFile();
		
		FileTime time = FileTime.fromMillis(now.getTime() - cleanupAge*60*1000 - 1000);
		
		// age testFile1 so it should get deleted
        BasicFileAttributeView attributes = Files.getFileAttributeView(testFile1.toPath(), BasicFileAttributeView.class);
        attributes.setTimes(time, time, time);
        
		// when
        deploymentBoundary.cleanupDeploymentFiles();
        
		// then
		assertTrue(deployment1.exists());
		assertFalse(testFile1.exists());
		assertTrue(deployment2.exists());
		assertTrue(testFile2.exists());
	}

    @Test
    public void testConfirmDeployment() throws ParseException {
        // given
        Integer deploymentId = 1;
        boolean sendEmailWhenDeployed = true;
        boolean simulateBeforeDeployment = true;
        boolean shakedownTestsWhenDeployed = true;
        boolean neighbourhoodTest = true;;

        Integer trackingId = 2;
        Date releaseDate = new Date();
        ReleaseEntity release = new ReleaseEntityBuilder().buildReleaseEntity("Main Release", releaseDate, false);
        ResourceGroupEntity group = new ResourceGroupEntity();
        ResourceEntity appServer = new ResourceEntity();
        boolean buildSuccess = true;
        ContextEntity context = new ContextEntity();

        DeploymentEntity deployment = new DeploymentEntityBuilder().buildDeploymentEntity(trackingId, release, group, appServer, buildSuccess, context, false);
        deployment.setDeploymentDate(new Date());
        deployment.setDeploymentState(DeploymentState.requested);
        deployment.setSendEmailConfirmation(false);
        deployment.setCreateTestAfterDeployment(false);
        deployment.setCreateTestForNeighborhoodAfterDeployment(false);
        deployment.setSimulating(false);

        Mockito.doReturn(DeploymentBoundary.DeploymentOperationValidation.SUCCESS).when(deploymentBoundary).isConfirmPossible(deployment);
        Mockito.doReturn(deployment).when(deploymentBoundary).getDeploymentById(deploymentId);
        Mockito.doReturn(deployment).when(deploymentBoundary).saveDeployment(deployment);
        Mockito.doReturn("Tom").when(permissionService).getCurrentUserName();

		Date deploymentDate = Date.from(LocalDate.now().plusDays(2).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        // when
        DeploymentEntity deploymentEntity = deploymentBoundary.confirmDeployment(deploymentId, sendEmailWhenDeployed, shakedownTestsWhenDeployed, neighbourhoodTest, simulateBeforeDeployment, deploymentDate);

        // then
        assertThat(deploymentEntity.getDeploymentState(), is(DeploymentState.scheduled));
        assertThat(deploymentEntity.getDeploymentConfirmationUser(), is("Tom"));
        assertThat(deploymentEntity.getDeploymentConfirmed(), is(true));

        assertThat(deploymentEntity.isSendEmailConfirmation(), is(sendEmailWhenDeployed));
        assertThat(deploymentEntity.isSimulating(), is(simulateBeforeDeployment));
        assertThat(deploymentEntity.isCreateTestAfterDeployment(), is(shakedownTestsWhenDeployed));
        assertThat(deploymentEntity.isCreateTestForNeighborhoodAfterDeployment(), is(neighbourhoodTest));
        assertThat(deploymentEntity.getDeploymentDate(), is(deploymentDate));
    }


	@Test
	public void shouldUpdateDeploymentInfoWithRightReasonIfModeIsDeployAndReasonIsNull() {

		// given
		GenerationModus mode = GenerationModus.DEPLOY;
		Integer deploymentId = 1;
		String errorMessage = "Error";
		Integer resourceId = null;
		GenerationResult result = new GenerationResult();
		DeploymentFailureReason reason = null;
		when(em.find(DeploymentEntity.class, deploymentId, LockModeType.PESSIMISTIC_FORCE_INCREMENT)).thenReturn(new DeploymentEntity());

		// when
		DeploymentEntity deploymentEntity = deploymentBoundary.updateDeploymentInfo(mode, deploymentId, errorMessage, resourceId, result, reason);

		// then
		assertThat(deploymentEntity.getReason(), is(DeploymentFailureReason.DEPLOYMENT_GENERATION));

	}

	@Test
	public void shouldUpdateDeploymentInfoWithRightReasonIfModeIsPreDeployAndReasonIsNull() {

		// given
		GenerationModus mode = GenerationModus.PREDEPLOY;
		Integer deploymentId = 1;
		String errorMessage = "Error";
		Integer resourceId = null;
		GenerationResult result = new GenerationResult();
		DeploymentFailureReason reason = null;
		when(em.find(DeploymentEntity.class, deploymentId, LockModeType.PESSIMISTIC_FORCE_INCREMENT)).thenReturn(new DeploymentEntity());

		// when
		DeploymentEntity deploymentEntity = deploymentBoundary.updateDeploymentInfo(mode, deploymentId, errorMessage, resourceId, result, reason);

		// then
		assertThat(deploymentEntity.getReason(), is(DeploymentFailureReason.PRE_DEPLOYMENT_GENERATION));

	}

	@Test
	public void shouldUpdateDeploymentInfoWithProvidedReasonIfReasonIsSet() {

		// given
		GenerationModus mode = GenerationModus.DEPLOY;
		Integer deploymentId = 1;
		String errorMessage = "Error";
		Integer resourceId = null;
		GenerationResult result = new GenerationResult();
		DeploymentFailureReason reason = DeploymentFailureReason.NODE_MISSING;
		when(em.find(DeploymentEntity.class, deploymentId, LockModeType.PESSIMISTIC_FORCE_INCREMENT)).thenReturn(new DeploymentEntity());

		// when
		DeploymentEntity deploymentEntity = deploymentBoundary.updateDeploymentInfo(mode, deploymentId, errorMessage, resourceId, result, reason);

		// then
		assertThat(deploymentEntity.getReason(), is(DeploymentFailureReason.NODE_MISSING));

	}

	@Test
	public void shouldSetSortColumnIfColumnKnown() {
		// given
		Query query = mock(Query.class);
		when(commonFilterService.addFilterAndCreateQuery(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(query);
		when(commonFilterService.setParameterToQuery(any(), any(), any(), any())).thenReturn(query);

		// when
		deploymentBoundary.getFilteredDeployments(0, null, emptyList(), "d.trackingId", DESC, emptyList() );

		// then
		ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
		verify(commonFilterService).addFilterAndCreateQuery(any(), any(), sortCaptor.capture(), anyBoolean(), anyBoolean());
		assertThat(sortCaptor.getValue().toString(), containsString("d.trackingId"));
	}

	@Test
	public void shouldIgnoreSortColumnIfNull() {
		// given
		Query query = mock(Query.class);
		when(commonFilterService.addFilterAndCreateQuery(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(query);
		when(commonFilterService.setParameterToQuery(any(), any(), any(), any())).thenReturn(query);

		// when
		deploymentBoundary.getFilteredDeployments(0, null, emptyList(), null, DESC, emptyList() );

		// then
		ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
		verify(commonFilterService).addFilterAndCreateQuery(any(), any(), sortCaptor.capture(), anyBoolean(), anyBoolean());
		Iterator<Sort.Order> iterator = sortCaptor.getValue().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(DeploymentOrder.of("d.id", DESC, false)));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void shouldThrowIfSortColumnUnknown() {
		try {
			// when
			deploymentBoundary.getFilteredDeployments(0, null, emptyList(), "unknown.column", DESC, emptyList());
			fail("IllegalArgumentException should have been thrown");
		} catch (IllegalArgumentException ex) {
			// then
			assertThat(ex.getMessage(), containsString("colToSort not found"));
		}
	}

}
