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

import static ch.puzzle.itc.mobiliar.business.domain.TestUtils.unitsFor;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.LB;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.WS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;

/**
 * 
 * @author ama
 * 
 *         A. Templates auf Ressourcen (haben wir schon im Meeting definiert)
 *         1. Aufbau: AS1 -> App1 -> WS -> Loadbalancer
 *         2. Templates ist auf WS-Typ definiert und schreib ein System Property mit den Werten von WS und
 *         Loadbalancer:
 *         ws-system.properties:
 *         url=${loadbalancer.schema}://${loadbalancer.host}/${ws.context}
 *         3. Template wird in das System.property von der Applikation geschrieben:
 *         ws-system.properties
 *         <#list consumedResTypes.ws?values as ws
 *         ${ws.templates["ws-system.properties"].content}
 *         </#list>
 * 
 *         B. Ressourcen überschreiben
 *         1. Aufbau: Gleiches Setup wie A.
 *         2. Der Loadblancer wird direkt dem AS1 angehängt und einzelne Property überschrieben.
 *         3. Auf dem Dependency Screen auf der Applikation wird der Loadbalancer vom WS mit dem
 *         Überschriebenen von AS1 ausgetauscht.
 *         4. Template wird geschrieben und enthält andere Werte.
 * 
 *         C. Mehrere gleiche Ressourcen
 *         1. Aufbau: AS1 -> App1 -> WS1
 *         -> WS2
 *         2. Wie unter A. können beide die Templates von WS1 und WS2 in ws-system.properties File auf der App
 *         geschrieben werden.
 * 
 * 
 */
public class TemplateProcessorUseCasesTest extends AmwTemplateProcessorTest {

	ResourceEntity as;
	ResourceEntity app;
	ResourceEntity ws;
	ResourceEntity lb;

	ResourceContextEntity lbContext;
	ResourceContextEntity wsContext;

	String wsTemplate = "url=${loadbalancer.schema}://${loadbalancer.host}/${context}";
	String appTemplate = "<#list consumedResTypes.Webservice?values as ws>x${ws.templates[\"ws-system.properties\"].content}</#list>";

	String wsTemplateExpected = "url=https://test.org//test_ctx";
	String appTemplateExpected = "xurl=https://test.org//test_ctx";

	@Override
	@BeforeEach
	public void before() throws Exception {
		super.before();
		CustomLogging.setup(Level.OFF);
		as = builder.resourceFor(EntityBuilderType.AS);
		app = builder.resourceFor(EntityBuilderType.APP);
		ws = builder.resourceFor(EntityBuilderType.WS);
		lb = builder.resourceFor(EntityBuilderType.LB);

		builder.buildConsumedRelation(app, ws);
		builder.buildConsumedRelation(ws, lb);

		builder.addResourceProperty(context, lb, "schema", "https");
		builder.addResourceProperty(context, lb, "host", "test.org");

		wsContext = builder.getOrCreateResourceContext(context, ws);
		builder.addResourceProperty(context, ws, "context", "/test_ctx");

		builder.buildResourceTemplate(ws, "ws-system.properties", wsTemplate, "ws-system.properties");
		builder.buildResourceTemplate(app, "app-ws-system.properties", appTemplate, "app-ws-system.properties");
	}

	@Test
	public void testTemplatesOnResourcesA() throws IOException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		prepareWorkUnits(templateExceptionHandler);
		assertWorkUnits(1, 2);

		generateTemplates();
		writeFiles();

		assertFilesWritten();
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testTemplatesOnResourcesB() throws IOException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		builder.buildConsumedRelation(as, lb);
		prepareWorkUnits(templateExceptionHandler);

		assertWorkUnits(2, 2);

		generateTemplates();
		writeFiles();

		assertFilesWritten();
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testTemplatesOnResourcesC() throws IOException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ws2 = builder.buildResource(builder.typeFor(WS.type), "ws2");
		builder.buildConsumedRelation(ws2, lb);
		prepareWorkUnits(templateExceptionHandler);
		assertWorkUnits(1, 2);

		generateTemplates();

		writeFiles();
		assertFilesWritten();
		assertTrue(templateExceptionHandler.isSuccess());
	}

	public void assertWorkUnits(int lbSize, int wsSize) {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		prepareWorkUnits(templateExceptionHandler);
		List<GenerationUnit> lbUnits = unitsFor(work.getAsSet(), LB);
		List<GenerationUnit> wsUnits = unitsFor(work.getAsSet(), WS);

		assertEquals(lbSize, lbUnits.size());
		for (GenerationUnit generationUnit : lbUnits) {
			assertEquals("https", getPropertyFromUnit(generationUnit, "schema").toString());
			assertEquals("test.org", getPropertyFromUnit(generationUnit, "host").toString());
		}

		assertEquals(wsSize, wsUnits.size());
		for (GenerationUnit generationUnit : wsUnits) {
			assertEquals("/test_ctx", getPropertyFromUnit(generationUnit, "context").toString());
		}
		assertTrue(templateExceptionHandler.isSuccess());
	}

	private void assertFilesWritten() throws IOException {
		assertEquals(2, files.size());

		assertEquals(wsTemplateExpected, readFile("ws-system.properties"));
		assertEquals(appTemplateExpected, readFile("app-ws-system.properties"));
	}

	private FreeMarkerProperty getPropertyFromUnit(GenerationUnit unit, String key) {
		return unit.getAppServerRelationProperties().getProperties().get(key);
	}

}
