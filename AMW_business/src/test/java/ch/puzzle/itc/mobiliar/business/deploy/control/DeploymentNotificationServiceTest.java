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

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.usersettings.control.UserSettingsService;
import ch.puzzle.itc.mobiliar.business.utils.notification.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.mail.Address;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DeploymentNotificationServiceTest {

	@InjectMocks
	DeploymentNotificationService deploymentNotificationService;
	
	@Mock
	private NotificationService notificationService;
	
	@Mock 
	private UserSettingsService userSettingsService;
	
	@Mock
	private ResourceDependencyResolverService dependencyResolverService;
	
	@Mock
	private ReleaseMgmtService releaseMgmtService;

	@Mock
	Logger log;
	
	ResourceEntity applicationServer;
	ContextEntity context;

	ReleaseEntity defaultRelease = new ReleaseEntity();
	
	@Before
	public void setUp(){
		MockitoAnnotations.openMocks(this);
		
		applicationServer =  ResourceFactory.createNewResource("appServer");
		when(dependencyResolverService.getResourceEntityForRelease((ResourceGroupEntity) any(), (ReleaseEntity) any())).thenReturn(
				applicationServer);
		
		context = new ContextEntity();
		context.setName("dev");

		defaultRelease.setDescription("default");
	}
	
	@Test
	public void createAndSendMailForDeplyoments_null() {
		// given
		
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(null);
		
		// then
		assertNull(result);
	}
	
	@Test
	public void createAndSendMailForDeplyoments_empty() {
		// given
		
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(new ArrayList<DeploymentEntity>());
		
		// then
		assertNull(result);
	}
	
	@Test
	public void createAndSendMailForDeplyoments_oneDeployment_noAddress_sending_ok() throws MessagingException {
		// given
		ArrayList<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setTrackingId(Integer.valueOf(12));
		deployment.setDeploymentState(DeploymentState.success);
		deployment.setResourceGroup(applicationServer.getResourceGroup());
		deployment.setContext(context);
		deployments.add(deployment);
		
		when(notificationService.createAndSendMail(anyString(), anyString(), any(Address[].class))).thenReturn(true);
		
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(deployments);
		
		// then
		assertNotNull(result);
		assertEquals("Notification email sent to following receipients: \n", result);
	}
	
	@Test
	public void createAndSendMailForDeplyoments_oneDeployment_noAddress_sending_nok() throws MessagingException {
		// given
		ArrayList<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setTrackingId(Integer.valueOf(12));
		deployment.setDeploymentState(DeploymentState.success);
		deployment.setResourceGroup(applicationServer.getResourceGroup());
		deployment.setContext(context);
		deployments.add(deployment);
		
		when(notificationService.createAndSendMail(anyString(), anyString(), any(Address[].class))).thenReturn(false);
		
		when(releaseMgmtService.getDefaultRelease()).thenReturn(defaultRelease);
		
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(deployments);
		
		// then
		assertNull(result);
		verify(log, times(0)).log(any(Level.class),anyString());
	}
	
	@Test
	public void createAndSendMailForDeplyoments_oneDeployment_noAddress_sending_nok_release_onDeployment() throws MessagingException {
		// given
		ArrayList<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setTrackingId(Integer.valueOf(12));
		deployment.setDeploymentState(DeploymentState.success);
		deployment.setResourceGroup(applicationServer.getResourceGroup());
		deployment.setContext(context);
		deployment.setRelease(defaultRelease);
		deployments.add(deployment);
		
		
		when(notificationService.createAndSendMail(anyString(), anyString(), any(Address[].class))).thenReturn(false);
		
		
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(deployments);
		
		// then
		assertNull(result);
		verify(log, times(0)).log(any(Level.class),anyString());
		verify(releaseMgmtService, times(0)).getDefaultRelease();
	}
	
	@Test
	public void createAndSendMailForDeplyoments_oneDeployment_noAddress_sending_Exception() throws MessagingException {
		// given
		ArrayList<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setTrackingId(Integer.valueOf(12));
		deployment.setDeploymentState(DeploymentState.success);
		deployment.setResourceGroup(applicationServer.getResourceGroup());
		deployment.setContext(context);
		deployments.add(deployment);
		
		when(notificationService.createAndSendMail(anyString(), anyString(), any(Address[].class))).thenThrow(new MessagingException("ErrorSending"));
		when(releaseMgmtService.getDefaultRelease()).thenReturn(defaultRelease);
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(deployments);
		
		// then
		assertNotNull(result);
		assertEquals("Failure occoured while sending notification email: ErrorSending", result);
		verify(log, times(1)).log(any(Level.class),anyString(), any(Exception.class));
	}
	
	
	@Test
	public void createAndSendMailForDeplyoments_oneDeployment_noAddress_sending_Receipients() throws MessagingException {
		// given
		ArrayList<DeploymentEntity> deployments = new ArrayList<DeploymentEntity>();
		
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setTrackingId(Integer.valueOf(12));
		deployment.setDeploymentState(DeploymentState.success);
		deployment.setResourceGroup(applicationServer.getResourceGroup());
		deployment.setApplicationsWithVersion(new ArrayList<DeploymentEntity.ApplicationWithVersion>());
		deployment.setContext(context);
		deployments.add(deployment);
		
		when(notificationService.createAndSendMail(anyString(), anyString(), any(Address[].class))).thenReturn(true);
		when(releaseMgmtService.getDefaultRelease()).thenReturn(defaultRelease);
		List<String> value = new ArrayList<String>();
		value.add("testuser1");
		when(userSettingsService.getRegisteredUsernamesForResourcesIds(any(Set.class))).thenReturn(value );
		
		// when
		String result = deploymentNotificationService.createAndSendMailForDeplyoments(deployments);
		
		// then
		assertNotNull(result);
		assertEquals("Notification email sent to following receipients: \ntestuser1@null\n", result);
	}

}
