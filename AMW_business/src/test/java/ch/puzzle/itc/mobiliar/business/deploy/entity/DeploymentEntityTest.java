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

package ch.puzzle.itc.mobiliar.business.deploy.entity;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.common.exception.DeploymentStateException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests {@link ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity}
 *
 */
public class DeploymentEntityTest {

	@Test
	public void test_ApplicationsWithVersion() {
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();

		List<ApplicationWithVersion> applicationsWithVersion = new ArrayList<DeploymentEntity.ApplicationWithVersion>();

		ApplicationWithVersion applicationWithVersion = new ApplicationWithVersion("testAppname", Integer.valueOf(1), "1.1.1");
		applicationsWithVersion.add(applicationWithVersion);

		deploymentEntity.setApplicationsWithVersion(applicationsWithVersion);

		// when
		List<ApplicationWithVersion> applicationsWithVersionResult = deploymentEntity.getApplicationsWithVersion();

		// then
		assertNotNull(applicationsWithVersionResult);
		assertEquals(1, applicationsWithVersionResult.size());
		assertEquals("testAppname", applicationsWithVersionResult.get(0).getApplicationName());
		assertEquals(Integer.valueOf(1), applicationsWithVersionResult.get(0).getApplicationId());
		assertEquals("1.1.1", applicationsWithVersionResult.get(0).getVersion());
	}

	@Test
	public void test_ApplicationsWithVersion_emptyList() {
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();

		List<ApplicationWithVersion> applicationsWithVersion = new ArrayList<DeploymentEntity.ApplicationWithVersion>();
		deploymentEntity.setApplicationsWithVersion(applicationsWithVersion);

		// when
		List<ApplicationWithVersion> applicationsWithVersionResult = deploymentEntity.getApplicationsWithVersion();

		// then
		assertNotNull(applicationsWithVersionResult);
		assertEquals(0, applicationsWithVersionResult.size());

	}

	@Test(expected=DeploymentStateException.class)
	public void setDeploymentConfirmed_shouldNotChangeDeployment(){
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();
		deploymentEntity.confirm("foo");

		// when
		deploymentEntity.confirm("bar");
	}

	@Test
	public void setDeploymentConfirmed_shouldConfirmDeployment(){
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();

		// when
		deploymentEntity.confirm("foo");


		// then
		assertTrue(deploymentEntity.getDeploymentConfirmed());
		assertEquals("foo", deploymentEntity.getDeploymentConfirmationUser());
		assertEquals(DeploymentState.scheduled, deploymentEntity.getDeploymentState());
	}

	@Test
	public void setDeploymentConfirmed_shouldRejectDeployment(){
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();

		// when
		deploymentEntity.reject("foo");


		// then
		assertFalse(deploymentEntity.getDeploymentConfirmed());
		assertEquals("foo", deploymentEntity.getDeploymentConfirmationUser());
		assertEquals(DeploymentState.rejected, deploymentEntity.getDeploymentState());
	}

	@Test
	public void getDeploymentState_shouldReturnNull() {
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();

		// when
		DeploymentState dState = deploymentEntity.getDeploymentState();

		// then
		assertNull(dState);
	}

	@Test
	public void getDeploymentState_shouldReturnDeploymentState() {
		// given
		DeploymentEntity deploymentEntity = new DeploymentEntity();
		deploymentEntity.confirm("foo");

		// when
		DeploymentState dState = deploymentEntity.getDeploymentState();

		// then
		assertEquals(DeploymentState.scheduled, dState);
	}

	@Test
	public void testIsPredeploymentFinished() throws DeploymentStateException {
		// setup
		DeploymentEntity deploymentEntity = new DeploymentEntity();
		NodeJobEntity nodJob1 = new NodeJobEntity();
		deploymentEntity.setNodeJobs(new HashSet<NodeJobEntity>());
		assertEquals(false, deploymentEntity.isPredeploymentFinished());

		deploymentEntity.getNodeJobs().add(nodJob1);
		nodJob1.setDeploymentState(DeploymentState.PRE_DEPLOYMENT);
		nodJob1.setStatus(NodeJobEntity.NodeJobStatus.RUNNING);
		assertEquals(false, deploymentEntity.isPredeploymentFinished());

		nodJob1.setStatus(NodeJobEntity.NodeJobStatus.FAILED);
		assertEquals(true, deploymentEntity.isPredeploymentFinished());

		NodeJobEntity nodJob2 = new NodeJobEntity();
		deploymentEntity.getNodeJobs().add(nodJob2);
		nodJob2.setDeploymentState(DeploymentState.PRE_DEPLOYMENT);
		nodJob2.setStatus(NodeJobEntity.NodeJobStatus.RUNNING);
		assertEquals(false, deploymentEntity.isPredeploymentFinished());

		nodJob2.setStatus(NodeJobEntity.NodeJobStatus.SUCCESS);
		assertEquals(true, deploymentEntity.isPredeploymentFinished());

		NodeJobEntity nodJob3 = new NodeJobEntity();
		deploymentEntity.getNodeJobs().add(nodJob3);
		nodJob3.setDeploymentState(DeploymentState.simulating);
		nodJob3.setStatus(NodeJobEntity.NodeJobStatus.RUNNING);
		assertEquals(true, deploymentEntity.isPredeploymentFinished());
	}

		public void testIsPredeploymentSuccessful() throws DeploymentStateException {
		// setup
		DeploymentEntity deploymentEntity = new DeploymentEntity();
		NodeJobEntity nodJob1 = new NodeJobEntity();
		deploymentEntity.setNodeJobs(new HashSet<NodeJobEntity>());
		assertEquals(false, deploymentEntity.isPredeploymentSuccessful());

		deploymentEntity.getNodeJobs().add(nodJob1);
		nodJob1.setDeploymentState(DeploymentState.PRE_DEPLOYMENT);
		nodJob1.setStatus(NodeJobEntity.NodeJobStatus.RUNNING);
		assertEquals(false, deploymentEntity.isPredeploymentSuccessful());

		nodJob1.setStatus(NodeJobEntity.NodeJobStatus.FAILED);
		assertEquals(false, deploymentEntity.isPredeploymentSuccessful());

		nodJob1.setStatus(NodeJobEntity.NodeJobStatus.SUCCESS);
		assertEquals(true, deploymentEntity.isPredeploymentSuccessful());

		NodeJobEntity nodJob2 = new NodeJobEntity();
		deploymentEntity.getNodeJobs().add(nodJob2);
		nodJob2.setDeploymentState(DeploymentState.PRE_DEPLOYMENT);
		nodJob2.setStatus(NodeJobEntity.NodeJobStatus.RUNNING);
		assertEquals(false, deploymentEntity.isPredeploymentSuccessful());

		nodJob2.setStatus(NodeJobEntity.NodeJobStatus.SUCCESS);
		assertEquals(true, deploymentEntity.isPredeploymentSuccessful());

		NodeJobEntity nodJob3 = new NodeJobEntity();
		deploymentEntity.getNodeJobs().add(nodJob3);
		nodJob3.setDeploymentState(DeploymentState.simulating);
		nodJob3.setStatus(NodeJobEntity.NodeJobStatus.RUNNING);
		assertEquals(true, deploymentEntity.isPredeploymentSuccessful());
	}
}
