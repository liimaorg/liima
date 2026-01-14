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

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.AmwTemplateProcessorTest;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;

public class Issue6110AppServerRelationsTemplateProcessorTest extends AmwTemplateProcessorTest {

	ResourceEntity as;
	ResourceEntity app;
	ResourceEntity ad;
	ResourceEntity jboss;

	String adTemplate = "";
	String asTemplate = "";
	String appTemplate = "";
	String jbossTemplate = "";
	ConsumedResourceRelationEntity jbossAdRelation;
	ConsumedResourceRelationEntity asJbossRelation;

	@Override
	@BeforeEach
	public void before() throws Exception {
		super.before();

		as = builder.resourceFor(EntityBuilderType.AS);
		app = builder.resourceFor(EntityBuilderType.APP);
		ad = builder.resourceFor(EntityBuilderType.AD);
		jboss = builder.resourceFor(EntityBuilderType.JBOSS7MANAGEMENT);
		jbossAdRelation = builder.buildConsumedRelation(jboss, ad);
		asJbossRelation = builder.buildConsumedRelation(as, jboss);
	}

	@Test
	public void testAdTypeTemplate() throws IOException {
		// given
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		adTemplate = "name=${name}";
		appTemplate = "<#list consumedResTypes.ActiveDirectory?values as ws>x${ws.templates[\"ad-system.properties\"].content}</#list>";
		jbossTemplate = "<#list consumedResTypes.ActiveDirectory?values as ws>y${ws.templates[\"ad-system.properties\"].content}</#list>";
		builder.buildResourceTypeTemplate(ad.getResourceType(), "ad-system.properties", adTemplate, "ad-system.properties");
		builder.buildResourceTypeTemplate(jboss.getResourceType(), "jboss-system.properties", jbossTemplate, "jboss-system.properties");
		builder.buildResourceTemplate(app, "app-ws-system.properties", appTemplate, "app-ws-system.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(3, files.size());
		assertEquals("name=adIntern", readFile("ad-system.properties"));
		assertEquals("yname=adIntern", readFile("jboss-system.properties"));
		assertEquals("xname=adIntern", readFile("app-ws-system.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testAdSecondInstanceTypeTemplate() throws IOException {
		// given
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		adTemplate = "name=${name}";
		appTemplate = "<#list consumedResTypes.ActiveDirectory?values as ws>x${ws.templates[\"ad-system.properties\"].content}</#list>";
		jbossTemplate = "<#list consumedResTypes.ActiveDirectory?values as ws>y${ws.templates[\"ad-system.properties\"].content}</#list>";
		builder.buildResourceTypeTemplate(ad.getResourceType(), "ad-system.properties", adTemplate, "ad-system.properties");
		builder.buildResourceTypeTemplate(jboss.getResourceType(), "jboss-system.properties", jbossTemplate, "jboss-system.properties");
		builder.buildResourceTemplate(app, "app-ws-system.properties", appTemplate, "app-ws-system.properties");
		// changing jboss'es related as re
		ResourceEntity adExtern = builder.buildResource(builder.typeFor(AD.type), "adExtern");
		jbossAdRelation.setSlaveResource(adExtern);
		builder.buildResourceTypeTemplate(ad.getResourceType(), "ad-system.properties", adTemplate, "ad-system.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(3, files.size());
		assertEquals("name=adIntern", readFile("ad-system.properties"));
		assertEquals("yname=adExtern", readFile("jboss-system.properties"));
		assertEquals("xname=adIntern", readFile("app-ws-system.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}


	@Test
	public void testAdSecondInstancePerResourceTemplate() throws IOException {
		// given
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		adTemplate = "name=${name}";
		appTemplate = "<#list consumedResTypes.ActiveDirectory?values as ws>x${ws.templates[\"ad-system.properties\"].content}</#list>";
		jbossTemplate = "<#list consumedResTypes.ActiveDirectory?values as ws>y${ws.templates[\"ad-system.properties\"].content}</#list>";
		builder.buildResourceTypeTemplate(jboss.getResourceType(), "jboss-system.properties", jbossTemplate, "jboss-system.properties");
		builder.buildResourceTemplate(app, "app-ws-system.properties", appTemplate, "app-ws-system.properties");
		// changing jboss'es related as re
		ResourceEntity adExtern = builder.buildResource(builder.typeFor(AD.type), "adExtern");
		jbossAdRelation.setSlaveResource(adExtern);
		builder.buildResourceTemplate(ad, "ad-system.properties", adTemplate, "intern_ad-system.properties");
		builder.buildResourceTemplate(adExtern, "ad-system.properties", adTemplate, "extern_ad-system.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(4, files.size());
		assertEquals("name=adExtern", readFile("extern_ad-system.properties"));
		assertEquals("name=adIntern", readFile("intern_ad-system.properties"));
		assertEquals("yname=adExtern", readFile("jboss-system.properties"));
		assertEquals("xname=adIntern", readFile("app-ws-system.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testAppServerTemplate() throws IOException {
		// given
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		asTemplate = "Loginmodulname=${consumedResTypes.JBoss7Management.jboss7Management_Ldap.consumedResTypes.ActiveDirectory.activedirectory.name}";
		builder.buildResourceTemplate(as, "modellFromAppServer.properties", asTemplate, "modellFromAppServer.properties");

		// when
		generate(templateExceptionHandler);

		// then
		assertEquals(1, files.size());
		assertEquals("Loginmodulname=adIntern", readFile("modellFromAppServer.properties"));
		assertTrue(templateExceptionHandler.isSuccess());
	}

}
