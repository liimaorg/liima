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

package ch.puzzle.itc.mobiliar.business.issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.business.deploy.entity.ApplicationWithVersionEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.domain.TestUtils;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.ApplicationResolverEntityBuilder;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnit;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.TemplateProcessorBaseTest;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import freemarker.template.TemplateModelException;

@RunWith(PersistenceTestRunner.class)
public class Issue5959AppServerRelationsTemplateProcessorProvidedConsumedTest extends
		TemplateProcessorBaseTest<ApplicationResolverEntityBuilder> {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Spy
	@InjectMocks
	ResourceTypeProvider resourceTypeProvider = new ResourceTypeProvider();

	@Spy
	ResourceDependencyResolverService dependencyResolverService;

	@Before
	public void before() throws Exception {
		CustomLogging.setup(Level.OFF);

		builder = new ApplicationResolverEntityBuilder(entityManager).buildScenario();
		context = builder.context;
		MockitoAnnotations.openMocks(this);

	}

	@Override
	protected GenerationOptions createOptions() {
		DeploymentEntity d = new DeploymentEntity();
		d.setRuntime(builder.platform);
		d.setApplicationsWithVersion(new HashSet<ApplicationWithVersionEntity>());
		
		GenerationContext generationContext = new GenerationContext(context, builder.as, d, null,
				GenerationModus.SIMULATE, dependencyResolverService);
		generationContext.setNode(builder.options.getContext().getNode());
		return new GenerationOptions(generationContext);
	}

	@Test
	public void testCosumed_ofProvidedWS() throws IOException, TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		prepareWorkUnits(templateExceptionHandler);

		GenerationUnit wsUnit = TestUtils.unitFor(work.getAsSet(), builder.ws);

		AmwResourceTemplateModel model = wsUnit.getAppServerRelationProperties().transformModel();
		assertEquals("as2", TestUtils.asHashModel(model, "appServer").get("label").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testConsumed_ofAppConsumingWs() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		prepareWorkUnits(templateExceptionHandler);

		GenerationUnit appUnit = TestUtils.unitFor(work.getAsSet(), builder.app);
		AmwResourceTemplateModel map = appUnit.getAppServerRelationProperties().transformModel();

		assertEquals("as2", TestUtils.asHashModel(map, "consumedResTypes", "Webservice", "ws","appServer").get("label").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testCosumed_ofProvidedWS_read_WS_AS_Property() throws IOException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		String app2Template = "as=${consumedResTypes.Webservice.ws.appServer.name},app=${consumedResTypes.Webservice.ws.app.name}";
		builder.buildResourceTemplate(builder.app, "tmp.properties", app2Template, "tmp.properties");

		generate(templateExceptionHandler);
		assertEquals(1, files.size());
		assertEquals("as=as2,app=app2", readFile("tmp.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}
}
