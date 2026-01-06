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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwAppServerNodeModel;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({PersistenceTestExtension.class, MockitoExtension.class})
public class ApplicationResolverTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	ApplicationResolverEntityBuilder builder;

	@InjectMocks
	ContextEntity context;

	ApplicationResolver resolver;

     @Mock
	ResourceDependencyResolverService resourceDependencyResolverService;

	@BeforeEach
	public void before() throws Exception {
		CustomLogging.setup(Level.OFF);
		builder = new ApplicationResolverEntityBuilder(entityManager).buildScenario();
		context = builder.context;
		Mockito.when(resourceDependencyResolverService.getProvidedSlaveRelationsForRelease(ArgumentMatchers.<ResourceEntity>any(), ArgumentMatchers.<ReleaseEntity>any()))
				.thenAnswer(invocation -> ((ResourceEntity) invocation.getArguments()[0]).getProvidedSlaveRelations());
		resolver = new ApplicationResolver(builder.options, builder.ws, resourceDependencyResolverService);
		assertEquals(true, resolver.resolve());
	}

	/**
	 * FIXME ama -> what about node, doesnt seem right but that's how i understand old generation code
	 */
	@Test
	public void testResolvedEntities() throws IOException, TemplatePropertyException {
		assertEquals(builder.app2, resolver.getApplication());
		assertEquals(builder.as2, resolver.getApplicationServer());
	}

	/**
	 * FIXME ama -> what is the actual structure of the map, does it need property types?
	 */
	@Test
	public void testTransformedProperties() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		AmwAppServerNodeModel model = new AmwAppServerNodeModel();
		resolver.transform(templateExceptionHandler, model);
		assertEquals("app2", ((SimpleHash)model.get("app")).get("label").toString());
		assertEquals("as2", ((SimpleHash)model.get("appServer")).get("label").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}
}
