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


import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeploymentBoundaryTest
{
	
	@InjectMocks
	private DeploymentBoundary deploymentBoundary;

	@Mock
	private Logger log;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
	}
	
	
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

		//when
        deploymentBoundary.cleanupDeploymentFiles();
        
        //then
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
        
		//then
		assertTrue(deployment1.exists());
		assertFalse(testFile1.exists());
		assertTrue(deployment2.exists());
		assertTrue(testFile2.exists());
	}

}
