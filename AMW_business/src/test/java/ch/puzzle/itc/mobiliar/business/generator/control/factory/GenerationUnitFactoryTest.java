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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static ch.puzzle.itc.mobiliar.business.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorUtils;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationPackage;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnitFactory;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import java.util.logging.Logger;


@ExtendWith(MockitoExtension.class)
public class GenerationUnitFactoryTest {

	ResourceEntity node;

	@InjectMocks
	GenerationUnitFactory factory;

	@Spy
	Logger log = Logger.getLogger(GenerationUnitFactoryTest.class.getSimpleName());

	@Mock
	GeneratorUtils utils;

	SimpleEntityBuilder builder;

	@Mock
	ResourceTypeProvider resourceTypeProvider;

	@Mock
	ResourceDependencyResolverService dependencyResolver;

	@Mock
	FunctionService FunctionService;

	GenerationOptions options;

	@BeforeEach
	public void before() {
		CustomLogging.setup(Level.OFF);
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
		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);

		assertEquals(3, properties(work.getAsSet()).size());
		assertGenerationUnitSequence(work, builder.app, node, builder.as);
		assertPropertyValues(work, builder.as, EntityBuilderType.AS.name);
		assertPropertyValues(work, builder.app, "app");
	}

	@Test
	public void testAsConsumesAppConsumesAd() {
		builder.buildConsumedRelation(builder.app, builder.ad, ForeignableOwner.AMW);

		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);

		assertGenerationUnitSequence(work, builder.ad, builder.app, node, builder.as);
		assertPropertyValues(work, builder.ad, "ad");
		assertPropertyValues(work, builder.as, EntityBuilderType.AS.name);
		assertPropertyValues(work, builder.app, "app");
	}

	@Test
	public void testAsConsumesAppProvidesAd() {
		builder.buildProvidedRelation(builder.app, builder.ad, ForeignableOwner.AMW);

		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);

		assertGenerationUnitSequence(work, builder.ad, builder.app, node, builder.as);
		assertPropertyValues(work, builder.ad, "ad");
		assertPropertyValues(work, builder.as, EntityBuilderType.AS.name);
		assertPropertyValues(work, builder.app, "app");
	}

	@Test
	public void testAsConsumesAppConsumesAdAndWs() {

		builder.buildConsumedRelation(builder.app, builder.ad, ForeignableOwner.AMW);
		builder.buildConsumedRelation(builder.app, builder.ws, ForeignableOwner.AMW);

		GenerationPackage work = GenerationUnitFactoryTestUtil.createWorkForBuilder(factory, dependencyResolver, builder);

		assertGenerationUnitSequence(work, builder.ad, builder.ws, builder.app, node, builder.as);
		assertPropertyValues(work, builder.ad, "ad");
		assertPropertyValues(work, builder.ws, "ws");
		assertPropertyValues(work, builder.as, "amw");
		assertPropertyValues(work, builder.app, "app");
	}

	void assertPropertyValues(GenerationPackage work, ResourceEntity entity, String value) {
		GenerationUnit unit = unitFor(work.getAsSet(), entity);
		Map<String, FreeMarkerProperty> properties = unit.getAppServerRelationProperties().getProperties();
		assertEquals(value, properties.get("name").getCurrentValue());
	}

	void assertGenerationUnitSequence(GenerationPackage work, ResourceEntity... entities) {
		List<GenerationUnit> list = new ArrayList<>(work.getAsSet());
		for (int i = 0; i < entities.length; i++) {
			assertEquals(entities[i], list.get(i).getSlaveResource());
		}
	}

}
