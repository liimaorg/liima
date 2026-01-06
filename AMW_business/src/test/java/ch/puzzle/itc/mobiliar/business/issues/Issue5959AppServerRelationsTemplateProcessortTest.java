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

import static ch.puzzle.itc.mobiliar.business.domain.TestUtils.readRecursionTemplate;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.logging.Level;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.TemplateProcessorBaseTest;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.SimpleEntityBuilder;

@ExtendWith(MockitoExtension.class)
public class Issue5959AppServerRelationsTemplateProcessortTest extends TemplateProcessorBaseTest<SimpleEntityBuilder> {

	String appTemplate = "name=${providedResTypes.Webservice.ws.name}";
	String expected = "name=ws";

	@BeforeEach
	public void before() throws Exception {
		CustomLogging.setup(Level.OFF);
		builder = new SimpleEntityBuilder();
		context = builder.context;
	}

	@Test
	public void testConsumingOnly() throws IOException {
		// given
		builder.buildConsumedRelation(builder.app, builder.ad, ForeignableOwner.AMW);

		String appTemplate = "name=${consumedResTypes.ActiveDirectory.ad.name}";
		builder.buildResourceTypeTemplate(builder.app.getResourceType(), "recursion", readRecursionTemplate(), "recursion");

		builder.buildResourceTypeTemplate(builder.app.getResourceType(), "tmp.properties", appTemplate, "tmp.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(2, files.size());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testConsumingAndProvidingApp() throws IOException {
		// given
		builder.buildConsumedRelation(builder.app, builder.ad, ForeignableOwner.AMW);
		builder.buildProvidedRelation(builder.app, builder.ws, ForeignableOwner.AMW);
		builder.buildResourceProperty(builder.ws, "testProp", "testValue");
		builder.buildResourceTypeTemplate(builder.app.getResourceType(), "tmp.properties", appTemplate, "tmp.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(1, files.size());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testProvidingOnlyApp() throws IOException {
		// given
		builder.buildProvidedRelation(builder.app, builder.ws, ForeignableOwner.AMW);
		// builder.buildResourceProperty(builder.ws, "testProp", "testValue");
		builder.buildResourceTypeTemplate(builder.app.getResourceType(), "tmp.properties", appTemplate, "tmp.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(1, files.size());
		assertEquals(expected, readFile("tmp.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testTwoApps() throws IOException {
		// given
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		builder.buildResourceTypeTemplate(builder.app.getResourceType(), "tmp.properties", appTemplate, "tmp.properties");
		builder.buildResourceTypeTemplate(builder.app.getResourceType(), "recursion", readRecursionTemplate(), "recursion");

		builder.buildProvidedRelation(builder.app, builder.ws, ForeignableOwner.AMW);
		// builder.buildResourceProperty(context, builder.ws, "testProp", "testValue");

		ResourceEntity app2 = builder.buildResource(builder.typeFor(APP.type), "app2");
		builder.buildProvidedRelation(app2, builder.ws, ForeignableOwner.AMW);
		builder.buildConsumedRelation(builder.as, app2, ForeignableOwner.AMW);

		// builder.buildProvidedRelation(app2, builder.ad);
		builder.buildResourceProperty(builder.ad, "testFoo", "testBar");

		// when
		generate(templateExceptionHandler);
		assertEquals(2, files.size());
		assertEquals(expected, readFile("tmp.properties"));

		assertNotNull(expected, readFile("recursion")); // write to sysout for inspection
		assertTrue(templateExceptionHandler.isSuccess());
	}

}
