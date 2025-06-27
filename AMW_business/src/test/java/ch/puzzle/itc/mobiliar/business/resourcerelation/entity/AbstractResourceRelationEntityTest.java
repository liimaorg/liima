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

package ch.puzzle.itc.mobiliar.business.resourcerelation.entity;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.*;
import static org.junit.Assert.*;

/**
 * is tests with implementation ConsumedResourceRelationEntity
 */
public class AbstractResourceRelationEntityTest {

	ContextEntity context;

	ContextEntity context2;

	@Before
	public void setUp() {

		context = new ContextEntity();
		context.setId(Integer.valueOf(22));

		context2 = new ContextEntity();
		context2.setId(Integer.valueOf(33));
	}

	@Test
	public void getResourceRelationContext_notsame() {
		// given
		ConsumedResourceRelationEntity rel = new ConsumedResourceRelationEntity();
		ResourceRelationContextEntity relContext = rel.createContext();
		relContext.setId(11);
		relContext.setContext(context2);

		Set<ResourceRelationContextEntity> contexts = new HashSet<ResourceRelationContextEntity>();
		contexts.add(relContext);
		rel.setContexts(contexts);

		// when
		ResourceRelationContextEntity result = rel.getResourceRelationContext(context);

		// then
		assertNull(result);
	}

	@Test
	public void getResourceRelationContext_same() {
		// given
		ConsumedResourceRelationEntity rel = new ConsumedResourceRelationEntity();
		ResourceRelationContextEntity relContext = rel.createContext();
		relContext.setId(11);
		relContext.setContext(context);

		Set<ResourceRelationContextEntity> contexts = new HashSet<ResourceRelationContextEntity>();
		contexts.add(relContext);
		rel.setContexts(contexts);

		// when
		ResourceRelationContextEntity result = rel.getResourceRelationContext(context);

		// then
		assertNotNull(result);
		assertEquals(relContext, result);
	}

	@Test
	public void testBuildIdentifierIntegratesCountStoredAsIdentifier() {
		EntityBuilder builder = new AmwEntityBuilder();
		ConsumedResourceRelationEntity relation = builder.relationFor(APP, AD);
		assertEquals("adIntern", relation.buildIdentifer());

		relation.setIdentifier("2");
		assertEquals("adIntern_2", relation.buildIdentifer());
	}
	
	@Test
	public void testBuildIdentifierWithBlankIdentifier() {
		EntityBuilder builder = new AmwEntityBuilder();
		ConsumedResourceRelationEntity relation = builder.relationFor(APP, AD);
		assertEquals("adIntern", relation.buildIdentifer());

		assertThrows(IllegalArgumentException.class, () -> {
			relation.setIdentifier("");
		});
	}
	
	@Test
	public void testBuildIdentifierWithNullIdentifier() {
		EntityBuilder builder = new AmwEntityBuilder();
		ConsumedResourceRelationEntity relation = builder.relationFor(APP, AD);
		assertEquals("adIntern", relation.buildIdentifer());

		relation.setIdentifier(null);
		assertEquals("adIntern", relation.buildIdentifer());
	}
	
	@Test
	public void testBuildIdentifierWithNonNummericIdentifier() {
		EntityBuilder builder = new AmwEntityBuilder();
		ConsumedResourceRelationEntity relation = builder.relationFor(APP, AD);
		assertEquals("adIntern", relation.buildIdentifer());

		relation.setIdentifier("a123");
		assertEquals("a123", relation.buildIdentifer());
	}
	
	@Test
	public void testBuildIdentifierWithNonDefaultEntityType() {
		EntityBuilder builder = new AmwEntityBuilder();
		
		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(builder.resourceFor(WS), builder.resourceFor(AD), ForeignableOwner.AMW);
		assertEquals("activedirectory", relation.buildIdentifer());

		relation.setIdentifier("1");
		assertEquals("activedirectory_1", relation.buildIdentifer());
	}

	@Test
	public void testBuildIdentifierWithDefaultEntityType() {
		EntityBuilder builder = new AmwEntityBuilder();

		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(builder.resourceFor(APP), builder.resourceFor(AS), ForeignableOwner.AMW);
		assertEquals("amw", relation.buildIdentifer());

		relation.setIdentifier("testApp");
		assertEquals("testApp", relation.buildIdentifer());
	}

}
