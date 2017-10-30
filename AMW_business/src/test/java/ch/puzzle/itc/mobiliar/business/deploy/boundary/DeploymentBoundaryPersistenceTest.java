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

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceGroupEntityBuilder;
import ch.puzzle.itc.mobiliar.business.deploy.control.DeploymentNotificationService;
import ch.puzzle.itc.mobiliar.business.deploy.entity.*;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.DeploymentStateException;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(PersistenceTestRunner.class)
public class DeploymentBoundaryPersistenceTest
{

	@InjectMocks
    DeploymentBoundary deploymentBoundary;

	@Mock
	ResourceEditService resourceEditService;
	@Mock
	CommonFilterService commonFilterService;

	@Mock
	ContextDomainService contextDomainService;

	@Mock
	PermissionService permissionService;
	
	@Mock
	DeploymentNotificationService deploymentNotificationService;

	@Mock
	Logger log;

	@PersistenceContext
	private EntityManager entityManager;

	private DeploymentEntity d;

	@Before
	public void setup() throws Exception {

		MockitoAnnotations.initMocks(this);
		deploymentBoundary.setEntityManager(entityManager);

		commonFilterService = new CommonFilterService();
		deploymentBoundary.commonFilterService = commonFilterService;
		commonFilterService.setEm(entityManager);
		commonFilterService.setLog(log);
		// given
		d = new DeploymentEntity();

		// disable security
		when(permissionService.hasPermissionForDeployment(any(DeploymentEntity.class))).thenReturn(true);
		when(permissionService.hasPermissionForCancelDeployment(any(DeploymentEntity.class))).thenReturn(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_changeDeploymentTime_exception_noId() throws DeploymentStateException {
		// when
		deploymentBoundary.changeDeploymentDate(null, new Date());
	}

	@Test
	public void test_changeDeploymentTime_success() throws DeploymentStateException {
		// given
		d.setDeploymentState(DeploymentState.requested);
		persistDeploymentEntityForTest(d);

		DeploymentEntity detachedEntity = getDetachedEntityFromDb(d);

		Date deploymentDate = new Date(new Date().getTime()+100000l);
		detachedEntity.setDeploymentDate(deploymentDate);

		// when
		DeploymentEntity result = deploymentBoundary
				.changeDeploymentDate(detachedEntity.getId(), detachedEntity.getDeploymentDate());

		// then
		assertNotNull(
				"Deployment should not be started or been executed so far",
				result);
		assertEquals(
				"Deploymentdate should be set to "
						+ deploymentDate.toString(),
				getDetachedEntityFromDb(d).getDeploymentDate(),
				deploymentDate);

	}

	@Test(expected = DeploymentStateException.class)
	public void test_changeDeploymentTime_failed_alreadyExecuted() throws DeploymentStateException  {
		// given
		d.setDeploymentState(DeploymentState.canceled);
		persistDeploymentEntityForTest(d);

		DeploymentEntity detachedEntity = getDetachedEntityFromDb(d);

		Date deploymentDate = new Date();
		detachedEntity.setDeploymentDate(deploymentDate);

		deploymentBoundary.changeDeploymentDate(detachedEntity.getId(), detachedEntity.getDeploymentDate());
	}

	@Test(expected = DeploymentStateException.class)
	public void test_changeDeploymentTime_failed_alreadyStarted() throws DeploymentStateException  {
		// given
		d.setDeploymentState(DeploymentState.progress);
		persistDeploymentEntityForTest(d);

		DeploymentEntity detachedEntity = getDetachedEntityFromDb(d);

		Date deploymentDate = new Date();
		detachedEntity.setDeploymentDate(deploymentDate);

		deploymentBoundary.changeDeploymentDate(detachedEntity.getId(), detachedEntity.getDeploymentDate());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_cancelDeployment_failed_nullvalue() throws DeploymentStateException {
		assertNull("", deploymentBoundary.cancelDeployment(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_cancelDeployment_exception_noId() throws DeploymentStateException  {
		// given
		d.setId(null);

		// when
		deploymentBoundary.cancelDeployment(d.getId());

	}

	@Test(expected = DeploymentStateException.class)
	public void test_cancelDeployment_failed_alreadyExecuted() throws DeploymentStateException  {
		// given
		d.setDeploymentState(DeploymentState.failed);
		persistDeploymentEntityForTest(d);

		DeploymentEntity detachedEntity = getDetachedEntityFromDb(d);

		// when
		deploymentBoundary.cancelDeployment(detachedEntity.getId());

	}

	@Test(expected = DeploymentStateException.class)
	public void test_cancelDeployment_failed_alreadyStarted() throws DeploymentStateException  {
		// given
		d.setDeploymentState(DeploymentState.progress);
		persistDeploymentEntityForTest(d);

		persistDeploymentEntityForTest(d);

		DeploymentEntity detachedEntity = getDetachedEntityFromDb(d);		
		
		deploymentBoundary.cancelDeployment(detachedEntity.getId());

	}

	@Test
	public void test_cancelDeployment_success() throws DeploymentStateException  {
		// given
		d.setDeploymentState(DeploymentState.scheduled);
		d.appendStateMessage(null);
		d.setDeploymentCancelUser(null);
		d.setDeploymentCancelDate(null);
		persistDeploymentEntityForTest(d);

		DeploymentEntity detachedEntity = getDetachedEntityFromDb(d);
		String cancelUserName = "jupi";
		

		// when
		when(permissionService.getCurrentUserName()).thenReturn(cancelUserName);
		DeploymentEntity cancelDeployment = deploymentBoundary.cancelDeployment(detachedEntity.getId());
		
		// then
		assertTrue("Deployment executed must be set to true after cancelling", cancelDeployment.isExecuted());
		assertEquals("State must be set to canceled",DeploymentState.canceled, cancelDeployment.getDeploymentState());
		assertNotNull("A statemessage must be set", cancelDeployment.getStateMessage());
		assertEquals("A cancel user must be set to "+cancelUserName,cancelUserName, cancelDeployment.getDeploymentCancelUser());
		assertNotNull("A cancel date be set", cancelDeployment.getDeploymentCancelDate());

	}
	
	@Test
	public void test_getDeploymentsInProgressTimeoutReached_started_inprogress_but_date_not_reached(){
		// given
		d.appendStateMessage(null);
		d.setDeploymentCancelUser(null);
		d.setDeploymentCancelDate(null);
		d.setDeploymentDate(new Date());
		d.setDeploymentState(DeploymentState.progress);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsInProgressTimeoutReached();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsInProgressTimeoutReached_started_inprogress_and_date(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, - ConfigurationService.getPropertyAsInt(ConfigKey.DEPLOYMENT_IN_PROGRESS_TIMEOUT) - 100); 
		
		d.appendStateMessage(null);
		d.setDeploymentCancelUser(null);
		d.setDeploymentCancelDate(null);
		d.setDeploymentDate(deploymentDate.getTime());
		d.setDeploymentState(DeploymentState.progress);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsInProgressTimeoutReached();
		
		// then
		assertNotNull(deployments);
		assertEquals(1, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsInProgressTimeoutReached_started_not_inprogress_and_date(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, - 4000); 
		
		d.setDeploymentDate(deploymentDate.getTime());
		d.appendStateMessage(null);
		d.setDeploymentCancelUser(null);
		d.setDeploymentCancelDate(null);
		d.setDeploymentState(DeploymentState.failed);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsInProgressTimeoutReached();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToExecute_noDeployments(){
		// given
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToExecute_oneDeployment(){
		// given
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, + 4000);
		
		d.setDeploymentDate(deploymentDate.getTime());
		d.setDeploymentState(DeploymentState.READY_FOR_DEPLOYMENT);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		assertEquals(1, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToExecute_oneDeployment_wrong_state(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, - 4000);
		
		d.setDeploymentDate(deploymentDate.getTime());
		d.setDeploymentState(DeploymentState.scheduled);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	
	@Test
	public void test_getDeploymentsToExecute_oneDeployment_allok(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, - 4000);
		
		d.setDeploymentState(DeploymentState.scheduled);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getPreDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		assertEquals(1, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToExecute_oneDeployment_wrongTime(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, + 4000);
		
		d.setDeploymentState(DeploymentState.scheduled);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getPreDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToExecute_oneDeployment_Started(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, - 4000);
		
		d.setDeploymentDate(deploymentDate.getTime());
		d.setDeploymentState(DeploymentState.PRE_DEPLOYMENT);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getPreDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToExecute_oneDeployment_Limit(){
		// given
		persistDeploymentEntityForTest(createDeploymentEntityToExecute());
		persistDeploymentEntityForTest(createDeploymentEntityToExecute());
		persistDeploymentEntityForTest(createDeploymentEntityToExecute());
		persistDeploymentEntityForTest(createDeploymentEntityToExecute());
		persistDeploymentEntityForTest(createDeploymentEntityToExecute());
		persistDeploymentEntityForTest(createDeploymentEntityToExecute());

		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getPreDeploymentsToExecute();
		
		// then
		assertNotNull(deployments);
		// limit is 5 so only 5 are returned
		assertEquals(5, deployments.size());
	}
	
	private DeploymentEntity createDeploymentEntityToExecute(){
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.SECOND, - 4000);
		
		DeploymentEntity deploymentEntity = new DeploymentEntity();
		deploymentEntity.setDeploymentState(DeploymentState.scheduled);
		deploymentEntity.setDeploymentDate(deploymentDate.getTime());
		
		return deploymentEntity;
	}
	
	
	@Test
	public void test_getDeploymentsToSimulate_noDeployments(){
		// given
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_allok_sameDay(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		
		d.confirm("testuser");
		d.setSimulating(true);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(1, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_allok_in_Future(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.DAY_OF_MONTH, 1);
		
		d.confirm("testuser");
		d.setSimulating(true);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(1, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_notok_in_Past(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		deploymentDate.add(Calendar.DAY_OF_MONTH, -2);
		
		d.confirm("testuser");
		d.setSimulating(true);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_Simulating(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		
		d.confirm("testuser");
		d.setSimulating(true);
		d.setDeploymentState(DeploymentState.simulating);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_NotConfirmed(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		
		d.setSimulating(true);
		d.setDeploymentState(DeploymentState.scheduled);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(1, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_Executed(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		
		d.confirm("testuser");
		d.setSimulating(true);
		d.setDeploymentState(DeploymentState.rejected);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_Started(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date());
		deploymentDate.add(Calendar.SECOND, 200);
		
		d.confirm("testuser");
		d.setSimulating(true);
		d.setDeploymentDate(deploymentDate.getTime());
		d.setDeploymentState(DeploymentState.progress);
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_NotSimulating(){
		// given
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		
		d.confirm("testuser");
		d.setSimulating(false);
		d.setDeploymentDate(deploymentDate.getTime());
		persistDeploymentEntityForTest(d);
		
		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void test_getDeploymentsToSimulate_oneDeployment_Limit(){
		// given
		persistDeploymentEntityForTest(createDeploymentEntityToSimulate());
		persistDeploymentEntityForTest(createDeploymentEntityToSimulate());
		persistDeploymentEntityForTest(createDeploymentEntityToSimulate());
		persistDeploymentEntityForTest(createDeploymentEntityToSimulate());
		persistDeploymentEntityForTest(createDeploymentEntityToSimulate());
		persistDeploymentEntityForTest(createDeploymentEntityToSimulate());

		// when
		List<DeploymentEntity> deployments = deploymentBoundary.getDeploymentsToSimulate();
		
		// then
		assertNotNull(deployments);
		// limit is 5 so only 5 are returned
		assertEquals(5, deployments.size());
	}
	
	private DeploymentEntity createDeploymentEntityToSimulate(){
		
		Calendar deploymentDate = Calendar.getInstance(); 
		deploymentDate.setTime(new Date()); 
		
		DeploymentEntity deploymentEntity = new DeploymentEntity();
		deploymentEntity.confirm("testuser");
		deploymentEntity.setSimulating(true);
		deploymentEntity.setDeploymentDate(deploymentDate.getTime());
		
		return deploymentEntity;
	}
	
	@Test
	public void test_sendOneNotificationForTrackingIdOfDeployment_non_Executed(){
		// given
		
		DeploymentEntity d = new DeploymentEntity();
		d.setTrackingId(Integer.valueOf(12));
		
		persistDeploymentEntityForTest(d);
		
		d = new DeploymentEntity();
		d.setTrackingId(Integer.valueOf(12));
		
		persistDeploymentEntityForTest(d);
		
		// when
		deploymentBoundary.sendOneNotificationForTrackingIdOfDeployment(Integer.valueOf(12));
		
		// then
		verify(deploymentNotificationService, times(0)).createAndSendMailForDeplyoments(anyList());
	}
	
	@Test
	public void test_sendOneNotificationForTrackingIdOfDeployment_one_Executed(){
		// given
		
		DeploymentEntity d = new DeploymentEntity();
		d.setTrackingId(Integer.valueOf(12));
		
		persistDeploymentEntityForTest(d);
		
		d = new DeploymentEntity();
		d.setTrackingId(Integer.valueOf(12));
		d.setDeploymentState(DeploymentState.success);
		
		persistDeploymentEntityForTest(d);
		
		// when
		deploymentBoundary.sendOneNotificationForTrackingIdOfDeployment(Integer.valueOf(12));
		
		// then
		verify(deploymentNotificationService, times(0)).createAndSendMailForDeplyoments(anyList());
	}
	
	@Test
	public void test_sendOneNotificationForTrackingIdOfDeployment_both_Executed(){
		// given
		
		DeploymentEntity d = new DeploymentEntity();
		d.setTrackingId(Integer.valueOf(12));
		d.setDeploymentState(DeploymentState.success);
		
		persistDeploymentEntityForTest(d);
		
		d = new DeploymentEntity();
		d.setTrackingId(Integer.valueOf(12));
		d.setDeploymentState(DeploymentState.success);
		
		persistDeploymentEntityForTest(d);
		
		// when
		deploymentBoundary.sendOneNotificationForTrackingIdOfDeployment(Integer.valueOf(12));
		
		// then
		verify(deploymentNotificationService, times(1)).createAndSendMailForDeplyoments(anyList());
	}
	
	@Test
	public void test_updateDeploymentInfo() {
		// given
		ResourceEntity resource = ResourceFactory.createNewResource();
		resource.setName("fooAS");
		entityManager.persist(resource);
		DeploymentEntity d = new DeploymentEntity();
		d.setResourceGroup(resource.getResourceGroup());
		persistDeploymentEntityForTest(d);

		// when
		deploymentBoundary.updateDeploymentInfo(GenerationModus.DEPLOY, d.getId(), "foo error", resource.getId(), null, DeploymentFailureReason.DEPLOYMENT_SCRIPT);

		// then
		DeploymentEntity afterUpdate = entityManager.find(DeploymentEntity.class, d.getId());
		assertNotNull(afterUpdate.getResource());
		assertEquals(resource.getId(), afterUpdate.getResource().getId());
		assertNotNull(d.getStateMessage());
		assertEquals("foo error", afterUpdate.getStateMessage());
		assertThat(afterUpdate.getReason(), is(DeploymentFailureReason.DEPLOYMENT_SCRIPT));
	}

	
	private void persistDeploymentEntityForTest(DeploymentEntity d) {
		entityManager.persist(d);
		entityManager.flush();
	}

	private DeploymentEntity getDetachedEntityFromDb(DeploymentEntity d) {
		DeploymentEntity entity = entityManager.find(DeploymentEntity.class,
				d.getId());
		entityManager.detach(entity);
		return entity;
	}
	
	@Test
	public void test_getFilteredDeployments_sortByRelease() {
		// given
		Calendar cal = new GregorianCalendar();
		ReleaseEntity releaseA = new ReleaseEntity();
		releaseA.setName("releaseA");
		cal.set(2014, Calendar.JANUARY, 1);
		releaseA.setInstallationInProductionAt(cal.getTime());
		ReleaseEntity releaseC = new ReleaseEntity();
		releaseA.setName("releaseC");
		cal.set(2014, Calendar.JULY, 1);
		releaseC.setInstallationInProductionAt(cal.getTime());
		ReleaseEntity releaseB = new ReleaseEntity();
		releaseA.setName("releaseB");
		cal.set(2014, Calendar.OCTOBER, 1);
		releaseB.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(releaseA);
		entityManager.persist(releaseC);
		entityManager.persist(releaseB);
		
		cal.set(2014, Calendar.AUGUST, 1);
		d.setDeploymentDate(cal.getTime());
		d.setRelease(releaseA);
		d.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(d);

		DeploymentEntity d2 = new DeploymentEntity();
		d2.setDeploymentDate(cal.getTime());
		d2.setRelease(releaseC);
		d2.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(d2);
		
		DeploymentEntity d3 = new DeploymentEntity();
		d3.setDeploymentDate(null);
		d3.setRelease(releaseB);
		d3.setDeploymentState(DeploymentState.requested);
		persistDeploymentEntityForTest(d3);

		// when sorting by release ascending (releaseA - releaseC - releaseB)
		String colToSort =	DeploymentFilterTypes.RELEASE.getFilterTabColumnName();
		Tuple<Set<DeploymentEntity>, Integer> result1 = deploymentBoundary.getFilteredDeployments(true, 0, 10, null, colToSort, CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result1);
		assertEquals(3, result1.getA().size());
		Iterator<DeploymentEntity> it = result1.getA().iterator();
		assertEquals(d, it.next());
		assertEquals(d2, it.next());
		assertEquals(d3, it.next());
		
		// when sorting by release descending (releaseB - releaseC - releaseC)
		Tuple<Set<DeploymentEntity>, Integer> result2 = deploymentBoundary.getFilteredDeployments(true, 0,
				10, null, colToSort, CommonFilterService.SortingDirectionType.DESC, null);

		// then
		assertNotNull(result2);
		assertEquals(3, result2.getA().size());
		it = result2.getA().iterator();
		assertEquals(d3, it.next());
		assertEquals(d2, it.next());
		assertEquals(d, it.next());
	}
	
	@Test
	public void test_getFilteredDeployments_filterByRelease() {
		// given
		Calendar cal = new GregorianCalendar();
		ReleaseEntity releaseA = new ReleaseEntity();
		releaseA.setName("releaseA");
		cal.set(2014, Calendar.JANUARY, 1);
		releaseA.setInstallationInProductionAt(cal.getTime());
		ReleaseEntity releaseC = new ReleaseEntity();
		releaseA.setName("releaseC");
		cal.set(2014, Calendar.JULY, 1);
		releaseC.setInstallationInProductionAt(cal.getTime());
		ReleaseEntity releaseB = new ReleaseEntity();
		releaseA.setName("releaseB");
		cal.set(2014, Calendar.OCTOBER, 1);
		releaseB.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(releaseA);
		entityManager.persist(releaseC);
		entityManager.persist(releaseB);
		
		cal.set(2014, Calendar.AUGUST, 1);
		d.setDeploymentDate(cal.getTime());
		d.setDeploymentState(DeploymentState.success);
		d.setRelease(releaseA);
		persistDeploymentEntityForTest(d);

		DeploymentEntity d2 = new DeploymentEntity();
		d2.setDeploymentDate(cal.getTime());
		d2.setDeploymentState(DeploymentState.success);
		d2.setRelease(releaseC);
		persistDeploymentEntityForTest(d2);
		
		DeploymentEntity d3 = new DeploymentEntity();
		d3.setDeploymentDate(new Date());
		d3.setDeploymentState(DeploymentState.requested);
		d3.setRelease(releaseB);
		persistDeploymentEntityForTest(d3);

		// when filtering >= releaseA
		Tuple<Set<DeploymentEntity>, Integer> result;

		CustomFilter filter = CustomFilter.builder(DeploymentFilterTypes.RELEASE)
				.comparatorSelection(ComparatorFilterOption.greaterequals).build();
		filter.setValue(CustomFilter.convertDateToString(releaseA.getInstallationInProductionAt()));
		result = deploymentBoundary.getFilteredDeployments(true, 0, 10, Collections.singletonList(filter),
				null, CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result);
		assertEquals(3, result.getA().size());
		assertTrue(result.getA().contains(d));
		assertTrue(result.getA().contains(d2));
		assertTrue(result.getA().contains(d3));
		
		// when filtering <= releaseC
		filter.setValue(CustomFilter.convertDateToString(releaseC.getInstallationInProductionAt()));
		filter.setComparatorSelection(ComparatorFilterOption.smallerequals);
		result = deploymentBoundary.getFilteredDeployments(true, 0,
				10, Collections.singletonList(filter), null,
				CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result);
		assertEquals(2, result.getA().size());
		assertTrue(result.getA().contains(d));
		assertTrue(result.getA().contains(d2));
		assertFalse(result.getA().contains(d3));

		// when filtering == releaseB
		filter.setValue(CustomFilter.convertDateToString(releaseB.getInstallationInProductionAt()));
		filter.setComparatorSelection(ComparatorFilterOption.equals);
		result = deploymentBoundary.getFilteredDeployments(true, 0,
				10, Collections.singletonList(filter), null,
				CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result);
		assertEquals(1, result.getA().size());
		assertFalse(result.getA().contains(d));
		assertFalse(result.getA().contains(d2));
		assertTrue(result.getA().contains(d3));
	}

	
	@Test
	public void test_getPerviousDeployment() throws AMWException {
		// given
		ResourceEntity resource = ResourceFactory.createNewResource();
		resource.setName("fooAS");
		entityManager.persist(resource);
		
		ContextEntity context = new ContextEntity();
		context.setName("test");
		entityManager.persist(context);
		
		DeploymentEntity d = new DeploymentEntity();
		d.setResourceGroup(resource.getResourceGroup());
		d.setContext(context);
		d.setDeploymentDate(new Date());
		d.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(d);

		// when
		DeploymentEntity previous = deploymentBoundary.getPreviousDeployment(d);

		// then
		assertEquals(d.getResourceGroup().getName(), previous.getResourceGroup().getName());
		assertEquals(d.getContext().getName(), previous.getContext().getName());
	}

	@Test
	public void getEssentialListOfLastDeploymentsForAppServerAndContext_shouldReturnLatest() throws AMWException {
		// given
		ResourceEntity resource = ResourceFactory.createNewResource();
		resource.setName("fooAS");
		entityManager.persist(resource);

		ContextEntity context = new ContextEntity();
		context.setName("test");
		entityManager.persist(context);

		DeploymentEntity successful = new DeploymentEntity();
		successful.setResourceGroup(resource.getResourceGroup());
		successful.setContext(context);
		successful.setDeploymentDate(new Date());
		successful.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(successful);

		DeploymentEntity failed = new DeploymentEntity();
		failed.setResourceGroup(resource.getResourceGroup());
		failed.setContext(context);
		failed.setDeploymentDate(new Date());
		failed.setDeploymentState(DeploymentState.failed);
		persistDeploymentEntityForTest(failed);

		// when
		List<Object[]> latest = deploymentBoundary.getEssentialListOfLastDeploymentsForAppServerAndContext(false);

		// then
		assertEquals(failed.getContext().getId(), latest.get(0)[0]);
		assertEquals(failed.getResourceGroup(), latest.get(0)[1]);
	}

	@Test
	public void getEssentialListOfLastDeploymentsForAppServerAndContext_shouldReturnLatestOnlyIfSuccessful() throws AMWException {
		// given
		ResourceEntity resource = ResourceFactory.createNewResource();
		resource.setName("fooAS");
		entityManager.persist(resource);

		ContextEntity context = new ContextEntity();
		context.setName("test");
		entityManager.persist(context);

		DeploymentEntity successful = new DeploymentEntity();
		successful.setResourceGroup(resource.getResourceGroup());
		successful.setContext(context);
		successful.setDeploymentDate(new Date());
		successful.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(successful);

		DeploymentEntity failed = new DeploymentEntity();
		failed.setResourceGroup(resource.getResourceGroup());
		failed.setContext(context);
		failed.setDeploymentDate(new Date());
		failed.setDeploymentState(DeploymentState.failed);
		persistDeploymentEntityForTest(failed);

		// when
		List<Object[]> latest = deploymentBoundary.getEssentialListOfLastDeploymentsForAppServerAndContext(true);

		// then
		assertThat(latest.size(), is(0));
	}

	@Test
	public void getEssentialListOfLastDeploymentsForAppServerAndContext_shouldReturnLatestOfEveryResourceGroupAndContext() throws AMWException {
		// given
		ResourceEntity resourceA = ResourceFactory.createNewResource();
		resourceA.setName("A");
		entityManager.persist(resourceA);

		ResourceEntity resourceB = ResourceFactory.createNewResource();
		resourceB.setName("B");
		entityManager.persist(resourceB);

		ContextEntity contextC = new ContextEntity();
		contextC.setName("C");
		entityManager.persist(contextC);

		ContextEntity contextT = new ContextEntity();
		contextT.setName("T");
		entityManager.persist(contextT);

		DeploymentEntity deploymentAColder = new DeploymentEntity();
		deploymentAColder.setResourceGroup(resourceA.getResourceGroup());
		deploymentAColder.setContext(contextC);
		deploymentAColder.setDeploymentDate(new Date());
		deploymentAColder.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(deploymentAColder);

		DeploymentEntity deploymentAC = new DeploymentEntity();
		deploymentAC.setResourceGroup(resourceA.getResourceGroup());
		deploymentAC.setContext(contextC);
		deploymentAC.setDeploymentDate(new Date());
		deploymentAC.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(deploymentAC);

		DeploymentEntity deploymentBC = new DeploymentEntity();
		deploymentBC.setResourceGroup(resourceB.getResourceGroup());
		deploymentBC.setContext(contextC);
		deploymentBC.setDeploymentDate(new Date());
		deploymentBC.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(deploymentBC);

		DeploymentEntity deploymentBT = new DeploymentEntity();
		deploymentBT.setResourceGroup(resourceB.getResourceGroup());
		deploymentBT.setContext(contextT);
		deploymentBT.setDeploymentDate(new Date());
		deploymentBT.setDeploymentState(DeploymentState.success);
		persistDeploymentEntityForTest(deploymentBT);

		// when
		List<Object[]> latest = deploymentBoundary.getEssentialListOfLastDeploymentsForAppServerAndContext(true);

		// then
		assertThat(latest.size(), is(3));
	}

	@Test
	public void shouldReturnTheRightFilteredDeploymentsInTheRightOrderIfLatestDeploymentJobFilterAndStateSuccessAreSet() {
		// given
		Calendar cal = new GregorianCalendar();
		ReleaseEntity release1 = new ReleaseEntity();
		release1.setName("release1");
		cal.set(2014, Calendar.JANUARY, 1);
		release1.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(release1);

		ReleaseEntity release2 = new ReleaseEntity();
		release2.setName("release2");
		cal.set(2014, Calendar.JANUARY, 2);
		release2.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(release2);

		ResourceEntity resource = ResourceEntityBuilder.createResourceEntity("Test", null);
		entityManager.persist(resource);
		Set<ResourceEntity> resources = new HashSet<>();
		resources.add(resource);
		ResourceGroupEntity group = new ResourceGroupEntityBuilder().buildResourceGroupEntity("TestGroup", resources, false);
		entityManager.persist(group);

		ContextEntity contextA = new ContextEntity();
		contextA.setName("A");
		entityManager.persist(contextA);

		ContextEntity contextB = new ContextEntity();
		contextB.setName("B");
		entityManager.persist(contextB);

		cal.set(2014, Calendar.AUGUST, 1);
		DeploymentEntity d1 = new DeploymentEntity();
		d1.setDeploymentDate(cal.getTime());
		d1.setRelease(release1);
		d1.setDeploymentState(DeploymentState.success);
		d1.setResourceGroup(group);
		d1.setContext(contextA);
		persistDeploymentEntityForTest(d1);

		cal.set(2014, Calendar.AUGUST, 11);
		DeploymentEntity d2 = new DeploymentEntity();
		d2.setDeploymentDate(cal.getTime());
		d2.setRelease(release2);
		d2.setDeploymentState(DeploymentState.success);
		d2.setResourceGroup(group);
		d2.setContext(contextB);
		persistDeploymentEntityForTest(d2);

		cal.set(2014, Calendar.AUGUST, 21);
		DeploymentEntity d3 = new DeploymentEntity();
		d3.setDeploymentDate(cal.getTime());
		d3.setRelease(release2);
		d3.setDeploymentState(DeploymentState.failed);
		d3.setResourceGroup(group);
		d3.setContext(contextA);
		persistDeploymentEntityForTest(d3);

		List<CustomFilter> filters = new LinkedList<>();
		CustomFilter lastDeploymentJobFilter = CustomFilter.builder(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV).build();
		filters.add(lastDeploymentJobFilter);
		CustomFilter stateFilter = CustomFilter.builder(DeploymentFilterTypes.DEPLOYMENT_STATE).build();
		stateFilter.setValue(DeploymentState.success.getDisplayName());
		stateFilter.setEnumType(DeploymentState.class);
		filters.add(stateFilter);
		CustomFilter appServerFilter = CustomFilter.builder(DeploymentFilterTypes.APPSERVER_NAME).build();
		appServerFilter.setValue(group.getName());
		filters.add(appServerFilter);

		// when sorting by release ascending
		String colToSort = DeploymentFilterTypes.RELEASE.getFilterTabColumnName();
		Tuple<Set<DeploymentEntity>, Integer> result1 = deploymentBoundary.getFilteredDeployments(true, 0, 10, filters, colToSort, CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result1);
		assertThat(result1.getA().size(), is(2));
		assertThat(result1.getB(), is(2));
		Iterator<DeploymentEntity> it = result1.getA().iterator();
		assertThat(it.next().getRelease(), is(release1));
		assertThat(it.next().getRelease(), is(release2));

		// when sorting by release descending
		Tuple<Set<DeploymentEntity>, Integer> result2 = deploymentBoundary.getFilteredDeployments(true, 0, 10, filters, colToSort, CommonFilterService.SortingDirectionType.DESC, null);

		// then
		assertNotNull(result2);
		assertThat(result2.getA().size(), is(2));
		assertThat(result2.getB(), is(2));
		it = result2.getA().iterator();
		assertThat(it.next().getRelease(), is(release2));
		assertThat(it.next().getRelease(), is(release1));
	}

	@Test
	public void shouldReturnTheRightFilteredDeploymentsIfLatestDeploymentJobFilterAndStateFailedAreSet() {
		// given
		Calendar cal = new GregorianCalendar();
		ReleaseEntity release = new ReleaseEntity();
		release.setName("release");
		cal.set(2014, Calendar.JANUARY, 1);
		release.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(release);

		ResourceEntity resource = ResourceEntityBuilder.createResourceEntity("Test", null);
		entityManager.persist(resource);
		Set<ResourceEntity> resources = new HashSet<>();
		resources.add(resource);
		ResourceGroupEntity group = new ResourceGroupEntityBuilder().buildResourceGroupEntity("TestGroup", resources, false);
		entityManager.persist(group);

		ContextEntity contextA = new ContextEntity();
		contextA.setName("A");
		entityManager.persist(contextA);

		ContextEntity contextB = new ContextEntity();
		contextB.setName("B");
		entityManager.persist(contextB);

		cal.set(2014, Calendar.AUGUST, 1);
		DeploymentEntity d1 = new DeploymentEntity();
		d1.setDeploymentDate(cal.getTime());
		d1.setRelease(release);
		d1.setDeploymentState(DeploymentState.failed);
		d1.setResourceGroup(group);
		d1.setContext(contextA);
		persistDeploymentEntityForTest(d1);

		cal.set(2014, Calendar.AUGUST, 11);
		DeploymentEntity d2 = new DeploymentEntity();
		d2.setDeploymentDate(cal.getTime());
		d2.setRelease(release);
		d2.setDeploymentState(DeploymentState.success);
		d2.setResourceGroup(group);
		d2.setContext(contextB);
		persistDeploymentEntityForTest(d2);

		cal.set(2014, Calendar.AUGUST, 21);
		DeploymentEntity d3 = new DeploymentEntity();
		d3.setDeploymentDate(cal.getTime());
		d3.setRelease(release);
		d3.setDeploymentState(DeploymentState.failed);
		d3.setResourceGroup(group);
		d3.setContext(contextA);
		persistDeploymentEntityForTest(d3);

		List<CustomFilter> filters = new LinkedList<>();
		CustomFilter lastDeploymentJobFilter = CustomFilter.builder(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV).build();
		filters.add(lastDeploymentJobFilter);
		CustomFilter stateFilter = CustomFilter.builder(DeploymentFilterTypes.DEPLOYMENT_STATE).build();
		stateFilter.setValue(DeploymentState.failed.getDisplayName());
		stateFilter.setEnumType(DeploymentState.class);
		filters.add(stateFilter);
		CustomFilter appServerFilter = CustomFilter.builder(DeploymentFilterTypes.APPSERVER_NAME).build();
		appServerFilter.setValue(group.getName());
		filters.add(appServerFilter);
		String colToSort = DeploymentFilterTypes.RELEASE.getFilterTabColumnName();

		// when
		Tuple<Set<DeploymentEntity>, Integer> result = deploymentBoundary.getFilteredDeployments(true, 0, 10, filters, colToSort, CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result);
		assertThat(result.getA().size(), is(1));
		assertThat(result.getB(), is(1));
		Iterator<DeploymentEntity> it = result.getA().iterator();
		assertEquals(d3.getId(), it.next().getId());
	}

	@Test
	public void shouldReturnDeploymentWithHighestIdPerEnvironmentIfLatestDeploymentJobFilterAndStateAreSetAndMultipleDeploymentsWithSameDeploymentDateExist() {
		// given
		Calendar cal = new GregorianCalendar();
		ReleaseEntity release = new ReleaseEntity();
		release.setName("release");
		cal.set(2014, Calendar.JANUARY, 1);
		release.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(release);

		ResourceEntity resource = ResourceEntityBuilder.createResourceEntity("Test", null);
		entityManager.persist(resource);
		Set<ResourceEntity> resources = new HashSet<>();
		resources.add(resource);
		ResourceGroupEntity group = new ResourceGroupEntityBuilder().buildResourceGroupEntity("TestGroup", resources, false);
		entityManager.persist(group);

		ContextEntity contextA = new ContextEntity();
		contextA.setName("A");
		entityManager.persist(contextA);

		ContextEntity contextB = new ContextEntity();
		contextB.setName("B");
		entityManager.persist(contextB);

		cal.set(2014, Calendar.AUGUST, 1);
		Date augustFirst = cal.getTime();
		DeploymentEntity d1 = new DeploymentEntity();
		d1.setDeploymentDate(augustFirst);
		d1.setRelease(release);
		d1.setDeploymentState(DeploymentState.failed);
		d1.setResourceGroup(group);
		d1.setContext(contextA);
		persistDeploymentEntityForTest(d1);

		cal.set(2014, Calendar.AUGUST, 11);
		DeploymentEntity d2 = new DeploymentEntity();
		d2.setDeploymentDate(cal.getTime());
		d2.setRelease(release);
		d2.setDeploymentState(DeploymentState.success);
		d2.setResourceGroup(group);
		d2.setContext(contextB);
		persistDeploymentEntityForTest(d2);

		cal.set(2014, Calendar.AUGUST, 1);
		DeploymentEntity d3 = new DeploymentEntity();
		d3.setDeploymentDate(augustFirst);
		d3.setRelease(release);
		d3.setDeploymentState(DeploymentState.failed);
		d3.setResourceGroup(group);
		d3.setContext(contextA);
		persistDeploymentEntityForTest(d3);

		List<CustomFilter> filters = new LinkedList<>();
		CustomFilter lastDeploymentJobFilter = CustomFilter.builder(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV).build();
		filters.add(lastDeploymentJobFilter);
		CustomFilter stateFilter = CustomFilter.builder(DeploymentFilterTypes.DEPLOYMENT_STATE).build();
		stateFilter.setValue(DeploymentState.failed.getDisplayName());
		stateFilter.setEnumType(DeploymentState.class);
		filters.add(stateFilter);
		CustomFilter appServerFilter = CustomFilter.builder(DeploymentFilterTypes.APPSERVER_NAME).build();
		appServerFilter.setValue(group.getName());
		filters.add(appServerFilter);
		String colToSort = DeploymentFilterTypes.RELEASE.getFilterTabColumnName();

		// when
		Tuple<Set<DeploymentEntity>, Integer> result = deploymentBoundary.getFilteredDeployments(true, 0, 10, filters, colToSort, CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result);
		assertThat(result.getA().size(), is(1));
		assertThat(result.getB(), is(1));
		Iterator<DeploymentEntity> it = result.getA().iterator();
		assertEquals(d3.getId(), it.next().getId());
	}

	@Test
	public void shouldReturnDeploymentWithHighestIdPerEnvironmentIfLatestDeploymentJobFilterAndMultipleDeploymentsWithSameDeploymentDateExist() {
		// given
		Calendar cal = new GregorianCalendar();
		ReleaseEntity release = new ReleaseEntity();
		release.setName("release");
		cal.set(2014, Calendar.JANUARY, 1);
		release.setInstallationInProductionAt(cal.getTime());
		entityManager.persist(release);

		ResourceEntity resource = ResourceEntityBuilder.createResourceEntity("Test", null);
		entityManager.persist(resource);
		Set<ResourceEntity> resources = new HashSet<>();
		resources.add(resource);
		ResourceGroupEntity group = new ResourceGroupEntityBuilder().buildResourceGroupEntity("TestGroup", resources, false);
		entityManager.persist(group);

		ContextEntity contextA = new ContextEntity();
		contextA.setName("A");
		entityManager.persist(contextA);

		ContextEntity contextB = new ContextEntity();
		contextB.setName("B");
		entityManager.persist(contextB);

		cal.set(2014, Calendar.AUGUST, 1);
		Date augustFirst = cal.getTime();
		DeploymentEntity d1 = new DeploymentEntity();
		d1.setDeploymentDate(augustFirst);
		d1.setRelease(release);
		d1.setDeploymentState(DeploymentState.failed);
		d1.setResourceGroup(group);
		d1.setContext(contextA);
		d1.setTrackingId(1);
		persistDeploymentEntityForTest(d1);

		cal.set(2014, Calendar.AUGUST, 11);
		DeploymentEntity d2 = new DeploymentEntity();
		d2.setDeploymentDate(cal.getTime());
		d2.setRelease(release);
		d2.setDeploymentState(DeploymentState.success);
		d2.setResourceGroup(group);
		d2.setContext(contextB);
		d2.setTrackingId(12);
		persistDeploymentEntityForTest(d2);

		cal.set(2014, Calendar.AUGUST, 1);
		DeploymentEntity d3 = new DeploymentEntity();
		d3.setDeploymentDate(augustFirst);
		d3.setRelease(release);
		d3.setDeploymentState(DeploymentState.failed);
		d3.setResourceGroup(group);
		d3.setContext(contextA);
		d3.setTrackingId(3);
		persistDeploymentEntityForTest(d3);

		List<CustomFilter> filters = new LinkedList<>();
		CustomFilter lastDeploymentJobFilter = CustomFilter.builder(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV).build();
		filters.add(lastDeploymentJobFilter);
		CustomFilter appServerFilter = CustomFilter.builder(DeploymentFilterTypes.APPSERVER_NAME).build();
		appServerFilter.setValue(group.getName());
		filters.add(appServerFilter);

		// when sorted by tracking id asc
		String colToSort = DeploymentFilterTypes.TRACKING_ID.getFilterTabColumnName();
		Tuple<Set<DeploymentEntity>, Integer> result1 = deploymentBoundary.getFilteredDeployments(true, 0, 10, filters, colToSort, CommonFilterService.SortingDirectionType.ASC, null);

		// then
		assertNotNull(result1);
		assertThat(result1.getA().size(), is(2));
		Object[] deployments = result1.getA().toArray();
		DeploymentEntity deployment0 = (DeploymentEntity) deployments[0];
		assertThat(deployment0.getDeploymentState(), is(DeploymentState.failed));
		assertThat(deployment0.getContext(), is(contextA));
		assertThat(deployment0.getTrackingId(), is(3));
		assertThat(deployment0.getId(), is(d3.getId()));
		DeploymentEntity deployment1 = (DeploymentEntity) deployments[1];
		assertThat(deployment1.getDeploymentState(), is(DeploymentState.success));
		assertThat(deployment1.getTrackingId(), is(12));
		assertThat(result1.getB(), is(2));

		// when sorted by tracking id desc
		colToSort = DeploymentFilterTypes.TRACKING_ID.getFilterTabColumnName();
		Tuple<Set<DeploymentEntity>, Integer> result2 = deploymentBoundary.getFilteredDeployments(true, 0, 10, filters, colToSort, CommonFilterService.SortingDirectionType.DESC, null);

		// then
		assertNotNull(result2);
		assertThat(result2.getA().size(), is(2));
		deployments = result2.getA().toArray();
		deployment0 = (DeploymentEntity) deployments[0];
		assertThat(deployment0.getDeploymentState(), is(DeploymentState.success));
		assertThat(deployment0.getTrackingId(), is(12));
		deployment1 = (DeploymentEntity) deployments[1];
		assertThat(deployment1.getDeploymentState(), is(DeploymentState.failed));
		assertThat(deployment1.getTrackingId(), is(3));
		assertThat(deployment1.getId(), is(d3.getId()));
		assertThat(result2.getB(), is(2));

	}

}
