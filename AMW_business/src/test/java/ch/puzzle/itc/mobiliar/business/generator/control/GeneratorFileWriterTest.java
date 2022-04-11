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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.ApplicationWithVersionEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;

public class GeneratorFileWriterTest {
	
	@InjectMocks
	GeneratorFileWriter generatorFileWriter;
	
	@Mock
	Logger log;
	
	@Before
	public void setUp(){
		MockitoAnnotations.openMocks(this);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
		System.getProperties().remove(ConfigKey.GENERATOR_PATH_SIMULATION.getValue());
		System.getProperties().remove(ConfigKey.GENERATOR_PATH_TEST.getValue());
	}
	
	@After
	public void tearDown(){
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
		System.getProperties().remove(ConfigKey.GENERATOR_PATH_SIMULATION.getValue());
		System.getProperties().remove(ConfigKey.GENERATOR_PATH_TEST.getValue());
	}
	

	@Test(expected=GeneratorException.class)
	public void should_throwAnException() throws GeneratorException {
		//given
		// no Properties are set
		
		//when
		generatorFileWriter.getGeneratorTargetPath(null);
	}
	
	@Test(expected=GeneratorException.class)
	public void should_throwAnException_with_context() throws GeneratorException {
		//given
		// no Properties are set
		GenerationContext context =Mockito.mock(GenerationContext.class);
		
		//when
		generatorFileWriter.getGeneratorTargetPath(context);
	}
	@Test
	public void should_returnGenerationPath_with_defaultDeploy() throws GeneratorException {
		//given
		
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "/genPath");
		System.setProperties(props);
		
		GenerationContext context = new GenerationContext(null, null, null, null, GenerationModus.DEPLOY, null);
		
		//when
		String path = generatorFileWriter.getGeneratorTargetPath(context);
		
		assertEquals("/genPath", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
	}
	@Test
	public void should_returnGenerationPath_with_defaultSimulate() throws GeneratorException {
		//given
		
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH_SIMULATION.getValue(), "/genPath/simulation");
		System.setProperties(props);
		
		GenerationContext context = new GenerationContext(null, null, null, null, GenerationModus.SIMULATE, null);
		
		//when
		String path = generatorFileWriter.getGeneratorTargetPath(context);
		
		assertEquals("/genPath/simulation", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH_SIMULATION.getValue());
	}
	
	@Test
	public void should_returnGenerationPath_with_defaultTest() throws GeneratorException {
		//given
		
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH_TEST.getValue(), "/genPath/test");
		System.setProperties(props);
		
		GenerationContext context = new GenerationContext(null, null, null, null, GenerationModus.TEST,  null);
		
		//when
		String path = generatorFileWriter.getGeneratorTargetPath(context);
		
		assertEquals("/genPath/test", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH_TEST.getValue());
	}
	
	@Test
	public void should_returnGenerationPath_default_for_simulation() throws GeneratorException {
		//given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "/genPath");
		System.setProperties(props);
		
		GenerationContext context = new GenerationContext(null, null, null, null, GenerationModus.SIMULATE,  null);
		
		//when
		String path = generatorFileWriter.getGeneratorTargetPath(context);
		
		assertEquals("/genPath", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
	}
	
	@Test
	public void should_returnGenerationPath_default_for_test() throws GeneratorException {
		//given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "/genPath");
		System.setProperties(props);
		
		GenerationContext context = new GenerationContext(null, null, null, null, GenerationModus.TEST,  null);
		
		//when
		String path = generatorFileWriter.getGeneratorTargetPath(context);
		
		assertEquals("/genPath", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
	}
	
	@Test
	public void should_returnSubPathGeneratorPath() throws GeneratorException {
		//given
		
		ContextEntity c = new ContextEntity();
		c.setName("B");
		ResourceEntity as = ResourceFactory.createNewResource();
		as.setName("as");
		
		ResourceEntity node = ResourceFactory.createNewResource();
		node.setName("node");
		
		GenerationContext context = new GenerationContext(c, as, null, null, GenerationModus.DEPLOY, null);
		
		context.setNode(node);
		
		//when
		String path = generatorFileWriter.getGenerationFolderForContextSubFolderPerNode(context);
		
		assertEquals("as"+File.separator+"B"+'_'+"node", path);
	}
	
	@Test
	public void should_returnGeneratorPath() throws GeneratorException {
		//given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "/genPath");
		System.setProperties(props);
		
		ContextEntity c = new ContextEntity();
		c.setName("B");
		ResourceEntity as = ResourceFactory.createNewResource();
		as.setName("as");
		
		ResourceEntity node = ResourceFactory.createNewResource();
		node.setName("node");
		
		DeploymentEntity d = new DeploymentEntity();
		d.setId(Integer.valueOf(12));
		d.setApplicationsWithVersion(new HashSet<ApplicationWithVersionEntity>());
		
		GenerationContext context = new GenerationContext(c, as, d, null, GenerationModus.DEPLOY, null);
		context.setNode(node);
		
		//when
		String path = generatorFileWriter.getGenerationFolderForContext(context);
		
		assertEquals("/genPath"+File.separator+"as"+File.separator+"B"+'_'+"node"+'_'+"12"+"_deploy", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
	}
	
	@Test
	public void should_returnGeneratorPathTesting() throws GeneratorException {
		//given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "/genPath");
		System.setProperties(props);
		
		ContextEntity c = new ContextEntity();
		c.setName("B");
		ResourceEntity as = ResourceFactory.createNewResource();
		as.setName("as");
		
		ResourceEntity node = ResourceFactory.createNewResource();
		node.setName("node");
		
		DeploymentEntity d = new DeploymentEntity();
		d.setId(Integer.valueOf(12));
		d.setApplicationsWithVersion(new HashSet<ApplicationWithVersionEntity>());
		Date deploymentDate = new Date();
		
		GenerationContext context = new GenerationContext(c, as, d, deploymentDate, GenerationModus.TEST, null);
		context.setNode(node);
		
		//when
		String path = generatorFileWriter.getGenerationFolderForContext(context);
		
		assertEquals("/genPath"+File.separator+"as"+File.separator+"B"+'_'+"node"+'_'+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.S").format(deploymentDate)+"_test", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
	}
	
	@Test
	public void should_returnGeneratorPathTesting_DeploymentDateNull() throws GeneratorException {
		//given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "/genPath");
		System.setProperties(props);
		
		ContextEntity c = new ContextEntity();
		c.setName("B");
		ResourceEntity as = ResourceFactory.createNewResource();
		as.setName("as");
		
		ResourceEntity node = ResourceFactory.createNewResource();
		node.setName("node");
		
		DeploymentEntity d = new DeploymentEntity();
		d.setApplicationsWithVersion(new HashSet<ApplicationWithVersionEntity>());
		d.setId(Integer.valueOf(12));
		
		GenerationContext context = new GenerationContext(c, as, d, null, GenerationModus.TEST, null);
		context.setNode(node);
		
		//when
		String path = generatorFileWriter.getGenerationFolderForContext(context);
		
		assertEquals("/genPath"+File.separator+"as"+File.separator+"B"+'_'+"node"+'_'+"", path);
		
		System.getProperties().remove(ConfigKey.GENERATOR_PATH.getValue());
	}
}
