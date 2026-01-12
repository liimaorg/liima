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

package ch.puzzle.itc.mobiliar.business.generator.control.factory;

import static ch.puzzle.itc.mobiliar.business.domain.TestUtils.propertiesFor;
import static ch.puzzle.itc.mobiliar.business.domain.TestUtils.unitsFor;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AD;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AS;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.DB2;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.MAIL;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderUtils.resourceByName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorUtils;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationPackage;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnitFactory;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;

import ch.puzzle.itc.mobiliar.business.domain.TestUtils;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.AppServerRelationProperties;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnit;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;

import freemarker.template.TemplateModelException;

import ch.puzzle.itc.mobiliar.test.CustomLogging;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.logging.Level;

@ExtendWith(MockitoExtension.class)
public class GenerationUnitFactoryAmwTest {

	@InjectMocks
	GenerationUnitFactory factory;

	@Spy
	java.util.logging.Logger log = java.util.logging.Logger.getLogger(GenerationUnitFactoryAmwTest.class.getSimpleName());

	@Mock
	GeneratorUtils utils;

	@Mock
	ResourceDependencyResolverService dependencyResolver;

	@Mock
	ResourceTypeProvider resourceTypeProvider;

	@Mock
	FunctionService FunctionService;

	AmwEntityBuilder builder;

	GenerationOptions options;

	@BeforeEach
	public void before() {
		CustomLogging.setup(Level.OFF);
		builder = new AmwEntityBuilder();
	}

	@Test
	public void testSize() {
		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		assertEquals(13, work.getAsSet().size());
	}

	@Test
	public void testWsAppearsTwiceButConsumedAreMerged() throws TemplatePropertyException, IOException {
		ResourceEntity app = builder.resourceFor(EntityBuilderType.APP);
		ResourceEntity ws = builder.resourceFor(EntityBuilderType.WS);
		ResourceEntity lb = builder.resourceFor(EntityBuilderType.LB);
		builder.buildConsumedRelation(app, ws);
		builder.buildConsumedRelation(ws, lb);

		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		List<GenerationUnit> wsUnits = unitsFor(work.getAsSet(), EntityBuilderType.WS);
		assertEquals(2, wsUnits.size());

		// merges consumed properties list
		AppServerRelationProperties asProps1 = wsUnits.get(0).getAppServerRelationProperties();
		AppServerRelationProperties asProps2 = wsUnits.get(1).getAppServerRelationProperties();
		assertEquals(asProps1.getConsumed(), asProps2.getConsumed());
	}

	@Test
	public void testFirstItem() {
		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		GenerationUnit firstUnit = work.getAsSet().iterator().next();
		assertEquals(firstUnit.getSlaveResource(), resourceByName(builder, AD));
		assertNotNull(firstUnit.getPropertiesAsModel());
	}

	@Test
	public void testLastItem() {
		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		List<GenerationUnit> workList = new ArrayList<>(work.getAsSet());
		assertEquals(workList.get(workList.size() - 1).getSlaveResource(), resourceByName(builder, AS));
		assertNotNull(workList.get(workList.size() - 1).getPropertiesAsModel());
	}

	@Test
	public void testTypeRelationIdentifierIsNotSet() {
		assertNull(builder.relationFor(APP, DB2).getIdentifier());
	}

	@Test
	public void testPropertyIdentifierWhenSetOnType() throws TemplateModelException {
		builder.buildConsumedRelation(DB2, MAIL);
		builder.relationFor(DB2, MAIL).setIdentifier("mail_1");
		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);

		AmwResourceTemplateModel properties = propertiesFor(work.getAsSet(), DB2).transformModel();
		assertEquals("mailrelay", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "mail_1").get("name").toString());
		assertEquals("mailrelay", TestUtils.asHashModel(properties, "mail_1").get("name").toString());

	}

	@Test
	public void testPropertyIdentifierWhenNullIdentifierOnType() throws TemplateModelException {
		builder.buildConsumedRelation(DB2, MAIL);
		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		
        AmwResourceTemplateModel properties = propertiesFor(work.getAsSet(), DB2).transformModel();
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "mail").get("name").toString());
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "mail").get("name").toString());
    	
	}

	@Test
	public void testPropertyIdentifierWhenRelationExistsTwice() throws TemplateModelException {
		builder.buildConsumedRelation(DB2, MAIL);
		ResourceEntity other = builder.buildResource(MAIL, "the_other_mail");

		ResourceRelationTypeEntity otherTypeRelation = builder.buildTypeRelation(DB2, MAIL);
		ConsumedResourceRelationEntity otherInstanceRelation = builder.buildConsumedRelation(builder.resourceFor(DB2), other);

		otherInstanceRelation.setIdentifier("other_instance_identifier");
		otherInstanceRelation.setResourceRelationType(otherTypeRelation);
		otherTypeRelation.setIdentifier("other_instance_identifier");

		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		
		AmwResourceTemplateModel properties = propertiesFor(work.getAsSet(), DB2).transformModel();

        assertEquals("the_other_mail", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "other_instance_identifier").get("name").toString());
        assertEquals("the_other_mail", TestUtils.asHashModel(properties, "other_instance_identifier").get("name").toString());
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "mail").get("name").toString());
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "mail").get("name").toString());
    
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTemplateLoading() {
		GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);
		verify(utils, times(13)).getTemplates(any(ResourceEntity.class), any(ContextEntity.class), any(Set.class), eq(builder.platform.getId()));
		verify(utils, times(12)).getTemplates(any(ContextEntity.class), any(AbstractResourceRelationEntity.class), any(Set.class), eq(builder.platform.getId()));
	}

}
