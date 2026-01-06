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

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.JBOSS7MANAGEMENT;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.NODE1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.logging.Level;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.TemplateProcessorBaseTest;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.SimpleEntityBuilder;

public class Issue6110NestedPropertyAccessTest extends TemplateProcessorBaseTest<SimpleEntityBuilder> {

	String appTemplate = "name=${app.providedResTypes.Webservice.ws.name},label=${app.providedResTypes.Webservice.ws.propertyTypes.Custom.label[0]}";
	String expected = "name=ws,label=ws";
	ResourceEntity as;
	ResourceEntity ad;
	ResourceEntity jboss;
	ResourceEntity app;
	ResourceEntity node;

	@BeforeEach
	public void before() throws Exception {
		CustomLogging.setup(Level.OFF);
		builder = new SimpleEntityBuilder();
		context = builder.context;
		MockitoAnnotations.openMocks(this);

		as = builder.as;
		app = builder.app;
		ad = builder.ad;

		builder.buildResourceType(JBOSS7MANAGEMENT.type);
		jboss = builder.buildResource(JBOSS7MANAGEMENT, "jboss7Management_Ldap");
		builder.buildConsumedRelation(as, jboss, ForeignableOwner.AMW);
		builder.buildConsumedRelation(jboss, ad, ForeignableOwner.AMW);

		// without node, only APP is generated
		builder.buildResourceType(NODE1.type);
		node = builder.buildResource(NODE1, "node");
		builder.buildConsumedRelation(as, node, ForeignableOwner.AMW);
	}

	@Test
	public void testAsTemplate() throws TemplatePropertyException, IOException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		String asTemplate = "Loginmodulname=${consumedResTypes.JBoss7Management.jboss7Management_Ldap.consumedResTypes.ActiveDirectory.activedirectory.name}";
		builder.buildResourceTemplate(as, "modellFromAppServer.properties", asTemplate, "modellFromAppServer.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(1, files.size());
		assertEquals("Loginmodulname=ad", readFile("modellFromAppServer.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}
}
