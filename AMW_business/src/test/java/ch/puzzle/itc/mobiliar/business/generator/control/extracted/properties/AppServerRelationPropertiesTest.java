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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AD;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.MAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.domain.TestUtils;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

public class AppServerRelationPropertiesTest {
	ContextEntity context = new ContextEntity();
	EntityBuilder builder = new AmwEntityBuilder();

	static {
		CustomLogging.setup(Level.OFF);
	}

	@Test
	public void test() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity owner = builder.resourceFor(APP);
		ResourceEntity ad = builder.resourceFor(AD);
		ConsumedResourceRelationEntity relation = builder.relationFor(owner.getName(), ad.getName());

		AppServerRelationProperties appServerRelationProperties = new AppServerRelationProperties(context, owner, templateExceptionHandler);
		appServerRelationProperties.addConsumedRelation("adIntern", ad, relation);

		AmwResourceTemplateModel properties = appServerRelationProperties.transformModel();

		assertEquals("1", properties.get("id").toString());
		assertEquals("ch_puzzle_itc_mobi_amw", properties.get("name").toString());

		assertNull(properties.get("propertyTypes"));
		assertTrue(((TemplateHashModel) properties.get("providedResTypes")).isEmpty());

		assertFalse(((TemplateHashModel) properties.get("consumedResTypes")).isEmpty());
		assertEquals("4", TestUtils.asHashModel(properties, "consumedResTypes", "ActiveDirectory", "adIntern").get("id").toString());
		assertEquals("adIntern", TestUtils.asHashModel(properties, "consumedResTypes", "ActiveDirectory", "adIntern").get("name").toString());

		
		assertEquals("4", TestUtils.asHashModel(properties, "adIntern").get("id").toString());
		assertEquals("adIntern", TestUtils.asHashModel(properties, "adIntern").get("name").toString());
		
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testMergeMergesAllConsumedInBothDirections() {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity owner = builder.resourceFor(APP);
		ResourceEntity slave = builder.resourceFor(AD);

		AppServerRelationProperties props1 = new AppServerRelationProperties(context, owner, templateExceptionHandler);
		AppServerRelationProperties props2 = new AppServerRelationProperties(context, owner, templateExceptionHandler);

		props1.addConsumedRelation("foo", slave, null);

		props1.merge(props2);
		assertEquals(1, props1.getConsumed().size());
		assertEquals(1, props2.getConsumed().size());

		AppServerRelationProperties props3 = new AppServerRelationProperties(context, owner, templateExceptionHandler);
		props3.merge(props2);
		assertEquals(1, props3.getConsumed().size());
		assertEquals(1, props2.getConsumed().size());
		assertEquals(1, props1.getConsumed().size());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testTemplatesInRelatedResources() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity owner = builder.resourceFor(APP);
		ResourceEntity ad = builder.resourceFor(AD);
		ConsumedResourceRelationEntity relation = builder.relationFor(owner.getName(), ad.getName());

		AppServerRelationProperties appServerRelationProperties = new AppServerRelationProperties(context, owner, templateExceptionHandler);
		appServerRelationProperties.addConsumedRelation("active_directory", ad, relation);
		Map<ResourceEntity, Set<GeneratedTemplate>> templatesCache = new LinkedHashMap<>();
		templatesCache.computeIfAbsent(ad, k -> new LinkedHashSet<>()).add(new GeneratedTemplate("name", "path", "content"));

		appServerRelationProperties.getConsumed().get(0).setTemplatesCache(templatesCache);
		appServerRelationProperties.setTemplatesCache(templatesCache);


        TemplateHashModel model = TestUtils.asHashModel(appServerRelationProperties.transformModel(), "active_directory", "templates", "name");
        assertEquals("content", model.get("content").toString());

		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testOnlyProvidedResource() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);
		ProvidedResourceRelationEntity relation = builder.buildProvidedRelation(ad, mail);
		AppServerRelationProperties properties = new AppServerRelationProperties(context, ad, templateExceptionHandler);

		properties.addProvidedRelation("mailrelay", mail, relation);

        TemplateHashModel model = TestUtils.asHashModel(properties.transformModel(), "providedResTypes", "Mail", "mailrelay");
        assertEquals("mailrelay", model.get("name").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testOnlyConsumedResource() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);
		builder.buildResourceProperty(ad, "foo", "bar");

		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(ad, mail);

		AppServerRelationProperties properties = new AppServerRelationProperties(context, ad, templateExceptionHandler);
		properties.addConsumedRelation("mailrelay", mail, relation);

        TemplateHashModel model = TestUtils.asHashModel(properties.transformModel(), "consumedResTypes", "Mail", "mailrelay");

        assertEquals("mailrelay", model.get("name").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}
}
