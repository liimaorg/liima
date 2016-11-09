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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.container;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DeploymentPropertiesTest {

	@Test
	public void should_return_correctMap() {
		// given
		
		Calendar confDate = Calendar.getInstance();
		confDate.set(2014, 10, 1, 1, 1, 1);
		
		Calendar creatDate = Calendar.getInstance();
		creatDate.set(2014, 11, 1, 1, 1, 1);
		
		DeploymentProperties prop = new DeploymentProperties();
		
		DeploymentEntity deployment = new DeploymentEntity();
		deployment.setId(Integer.valueOf(1));
		deployment.setTrackingId(Integer.valueOf(2));
		deployment.setDeploymentConfirmationUser("ConfUser");
		deployment.setDeploymentConfirmationDate(confDate.getTime());
		deployment.setDeploymentRequestUser("RequestUser");
		deployment.setDeploymentJobCreationDate(creatDate.getTime());
		
		prop.setDeployment(deployment);
		
		Calendar releaseDate = Calendar.getInstance();
		releaseDate.set(2014, 11, 1, 1, 1, 1);
		
		ReleaseEntity release = new ReleaseEntity();
		release.setId(Integer.valueOf(3));
		release.setName("releaseName");
		release.setInstallationInProductionAt(releaseDate.getTime());
		
		prop.setRelease(release);
		
		prop.setGenerationModus(GenerationModus.DEPLOY);
		
		ResourceEntity targetPlatform = ResourceFactory.createNewResource("JBoss");
		targetPlatform.setId(Integer.valueOf(4));
		prop.setTargetPlatform(targetPlatform);
		
		prop.setGenerationDir("testDir");
		// when
		
		Map<String, Object> map = prop.asMap();
		
		// then
		assertEquals(Integer.valueOf(1), map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_ID));
		assertEquals(Integer.valueOf(2), map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_TRACKINGID));
		assertEquals("ConfUser", map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_CONF_USER));
		assertEquals(formatDate(confDate), map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_CONF_DATE));
		assertEquals("RequestUser", map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_REQ_USER));
		assertEquals(formatDate(creatDate), map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_CREA_DATE));
		
		assertEquals(GenerationModus.DEPLOY.name(), map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_GEN_MODUS));
		assertEquals("testDir", map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_GENERATIONDIR));
		
		Map<String, Object> releaseMap = (Map<String, Object>) map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_RELEASE);
		
		assertEquals(Integer.valueOf(3), releaseMap.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_ID));
		assertEquals("releaseName", releaseMap.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_NAME));
		assertEquals(formatDate(releaseDate), releaseMap.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_INSTALLATION_DATE));
		
		Map<String, Object> targetPlatformMap = (Map<String, Object>) map.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_RUNTIME);
		assertEquals(Integer.valueOf(4), targetPlatformMap.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_ID));
		assertEquals("JBoss", targetPlatformMap.get(DeploymentProperties.DEPLOYMENT_PROPERTIES_KEY_NAME));
		
	}

	private String formatDate(Calendar date) {
		return new SimpleDateFormat(DeploymentProperties.DATE_FORMAT).format(date.getTime());
	}

}
