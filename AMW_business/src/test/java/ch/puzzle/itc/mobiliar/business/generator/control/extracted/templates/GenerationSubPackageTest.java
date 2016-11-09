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

import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GenerationSubPackageTest {
	
	EntityBuilder builder = new AmwEntityBuilder();
	
	GenerationSubPackage p = new GenerationSubPackage();
	GenerationUnit u = new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null);

	@Test
	public void should_addGenerationUnit() {
		p.addGenerationUnit(u);
		
		assertEquals(1, p.getSubGenerationUnitsAsList().size());
	}
	
	@Test
	public void should_not_add_null_addGenerationUnit() {
		p.addGenerationUnit(null);
		
		assertEquals(0, p.getSubGenerationUnitsAsList().size());
	}
	
	
	@Test
	public void should_returnAll_SubPackageUnits() {
		// given
		p.addGenerationUnit(u);
		
		GenerationUnit u2 = new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.APP), null, null);
		GenerationUnit u3 = new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null);
		
		GenerationSubPackage subPackage = new GenerationSubPackage();
		subPackage.addGenerationUnit(u2);
		subPackage.setPackageGenerationUnit(u3);
		
		// when
		p.place(subPackage);
		
		// then
		assertEquals(2, p.getSubGenerationUnitsAsList().size());
	}
	
	@Test
	public void should_place_false_Subpackage() {
		// given
		p.addGenerationUnit(u);
		
		GenerationUnit u2 = new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.AS), null, null);
		GenerationUnit u3 = new GenerationUnit(builder.resourceFor(EntityBuilderType.AS), null, null, null);
		
		GenerationSubPackage subPackage = new GenerationSubPackage();
		subPackage.addGenerationUnit(u2);
		subPackage.setPackageGenerationUnit(u3);
		
		// when
		p.place(subPackage);
		
		// then
		assertEquals(1, p.getSubGenerationUnitsAsList().size());
	}

}
