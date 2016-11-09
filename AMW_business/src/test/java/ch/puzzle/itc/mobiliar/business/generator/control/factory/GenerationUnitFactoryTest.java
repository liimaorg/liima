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

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnit;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;
import ch.puzzle.itc.mobiliar.test.SimpleEntityBuilder;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static ch.puzzle.itc.mobiliar.business.domain.TestUtils.*;
import static org.junit.Assert.assertEquals;

public class GenerationUnitFactoryTest extends GenerationUnitFactoryBaseTest<SimpleEntityBuilder> {

    ResourceEntity node;
    @Override
	public void before() {
		CustomLogging.setup(Level.OFF);
		MockitoAnnotations.initMocks(this);
		builder = new SimpleEntityBuilder();
		builder.as = builder.buildResource(EntityBuilderType.AS, EntityBuilderType.AS.name);
		builder.app = builder.buildResource(EntityBuilderType.APP, "app");
		builder.ad = builder.buildResource(EntityBuilderType.AD, "ad");
		builder.ws = builder.buildResource(EntityBuilderType.WS, "ws");
		builder.buildConsumedRelation(builder.as, builder.app, ForeignableOwner.AMW);
	    	node = builder.buildResource(EntityBuilderType.NODE1, EntityBuilderType.NODE1.name);
	     builder.buildConsumedRelation(builder.as, node, ForeignableOwner.AMW);
	}

	@Test
	public void testAsConsumesApp() {
		initialize();

		assertEquals(3, properties(work.getAsSet()).size());

		assertGenerationUnitSequence(builder.app, node, builder.as);

		assertPropertyValues(builder.as, EntityBuilderType.AS.name);
		assertPropertyValues(builder.app, "app");
	}

	@Test
	public void testAsConsumesAppConsumesAd() {
		builder.buildConsumedRelation(builder.app, builder.ad, ForeignableOwner.AMW);

		initialize();

		assertGenerationUnitSequence(builder.ad, builder.app, node, builder.as);

		assertPropertyValues(builder.ad, "ad");
		assertPropertyValues(builder.as, EntityBuilderType.AS.name);
		assertPropertyValues(builder.app, "app");
	}

	@Test
	public void testAsConsumesAppProvidesAd() {
		builder.buildProvidedRelation(builder.app, builder.ad, ForeignableOwner.AMW);

		initialize();

		assertGenerationUnitSequence(builder.ad, builder.app, node, builder.as);

		assertPropertyValues(builder.ad, "ad");
		assertPropertyValues(builder.as, EntityBuilderType.AS.name);
		assertPropertyValues(builder.app, "app");
	}

	@Test
	public void testAsConsumesAppConsumesAdAndWs() {

		builder.buildConsumedRelation(builder.app, builder.ad, ForeignableOwner.AMW);
		builder.buildConsumedRelation(builder.app, builder.ws, ForeignableOwner.AMW);

		initialize();

		assertGenerationUnitSequence(builder.ad, builder.ws, builder.app, node, builder.as);

		assertPropertyValues(builder.ad, "ad");
		assertPropertyValues(builder.ws, "ws");
		assertPropertyValues(builder.as, "amw");
		assertPropertyValues(builder.app, "app");
	}

	void assertPropertyValues(ResourceEntity entity, String value) {
		GenerationUnit unit = unitFor(work.getAsSet(), entity);
		Map<String, FreeMarkerProperty> properties = unit.getAppServerRelationProperties().getProperties();
		assertEquals(value, properties.get("name").getCurrentValue());
	}

	void assertGenerationUnitSequence(ResourceEntity... entities) {
		List<GenerationUnit> list = Lists.newArrayList(work.getAsSet());
		for (int i = 0; i < entities.length; i++) {
			assertEquals(entities[i], list.get(i).getSlaveResource());
		}
	}

}
