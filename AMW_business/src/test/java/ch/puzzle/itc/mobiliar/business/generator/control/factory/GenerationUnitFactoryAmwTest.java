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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.domain.TestUtils;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
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

import com.google.common.collect.Iterables;

import freemarker.template.TemplateModelException;

public class GenerationUnitFactoryAmwTest extends GenerationUnitFactoryBaseTest<AmwEntityBuilder> {

	@Override
	public void before() {
		builder = new AmwEntityBuilder();
		super.before();
	}

	@Test
	public void testSize() {
		assertEquals(13, work.getAsSet().size());
	}

	@Test
	public void testWsAppearsTwiceButConsumedAreMerged() throws TemplatePropertyException, IOException {
		ResourceEntity app = builder.resourceFor(EntityBuilderType.APP);
		ResourceEntity ws = builder.resourceFor(EntityBuilderType.WS);
		ResourceEntity lb = builder.resourceFor(EntityBuilderType.LB);
		builder.buildConsumedRelation(app, ws, ForeignableOwner.AMW);
		builder.buildConsumedRelation(ws, lb, ForeignableOwner.AMW);

		initialize();
		List<GenerationUnit> wsUnits = unitsFor(work.getAsSet(), EntityBuilderType.WS);
		assertEquals(2, wsUnits.size());

		// merges consumed properties list
		AppServerRelationProperties asProps1 = wsUnits.get(0).getAppServerRelationProperties();
		AppServerRelationProperties asProps2 = wsUnits.get(1).getAppServerRelationProperties();
		assertEquals(asProps1.getConsumed(), asProps2.getConsumed());
	}

	@Test
	public void testFirstItem() {
		assertEquals(Iterables.getFirst(work.getAsSet(), null).getSlaveResource(), resourceByName(builder, AD));
		assertNotNull(Iterables.getFirst(work.getAsSet(), null).getPropertiesAsModel());
	}

	@Test
	public void testLastItem() {
		assertEquals(Iterables.getLast(work.getAsSet()).getSlaveResource(), resourceByName(builder, AS));
		assertNotNull(Iterables.getLast(work.getAsSet()).getPropertiesAsModel());
	}

	@Test
	public void testTypeRelationIdentifierIsNotSet() {
		assertNull(builder.relationFor(APP, DB2).getIdentifier());
	}

	@Test
	public void testPropertyIdentifierWhenSetOnType() throws TemplateModelException {
		builder.buildConsumedRelation(DB2, MAIL, ForeignableOwner.AMW);
		builder.relationFor(DB2, MAIL).setIdentifier("mail_1");
		initialize();

		AmwResourceTemplateModel properties = propertiesFor(work.getAsSet(), DB2).transformModel();
		assertEquals("mailrelay", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "mail_1").get("name").toString());
		assertEquals("mailrelay", TestUtils.asHashModel(properties, "mail_1").get("name").toString());

	}

	@Test
	public void testPropertyIdentifierWhenNullIdentifierOnType() throws TemplateModelException {
		builder.buildConsumedRelation(DB2, MAIL, ForeignableOwner.AMW);
		initialize();

        AmwResourceTemplateModel properties = propertiesFor(work.getAsSet(), DB2).transformModel();
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "mail").get("name").toString());
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "mail").get("name").toString());
    	
	}

	@Test
	public void testPropertyIdentifierWhenRelationExistsTwice() throws TemplateModelException {
		builder.buildConsumedRelation(DB2, MAIL, ForeignableOwner.AMW);
		ResourceEntity other = builder.buildResource(MAIL, "the_other_mail");

		ResourceRelationTypeEntity otherTypeRelation = builder.buildTypeRelation(DB2, MAIL);
		ConsumedResourceRelationEntity otherInstanceRelation = builder.buildConsumedRelation(builder.resourceFor(DB2), other, ForeignableOwner.AMW);

		otherInstanceRelation.setIdentifier("other_instance_identifier");
		otherInstanceRelation.setResourceRelationType(otherTypeRelation);
		otherTypeRelation.setIdentifier("other_instance_identifier");

		initialize();
		
		AmwResourceTemplateModel properties = propertiesFor(work.getAsSet(), DB2).transformModel();

        assertEquals("the_other_mail", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "other_instance_identifier").get("name").toString());
        assertEquals("the_other_mail", TestUtils.asHashModel(properties, "other_instance_identifier").get("name").toString());
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "consumedResTypes", "Mail", "mail").get("name").toString());
        assertEquals("mailrelay", TestUtils.asHashModel(properties, "mail").get("name").toString());
    
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTemplateLoading() {
		verify(utils, times(13)).getTemplates(any(ResourceEntity.class), any(ContextEntity.class), any(Set.class), eq(builder.platform.getId()));
		verify(utils, times(12)).getTemplates(any(ContextEntity.class), any(AbstractResourceRelationEntity.class), any(Set.class), eq(builder.platform.getId()));
	}

}
