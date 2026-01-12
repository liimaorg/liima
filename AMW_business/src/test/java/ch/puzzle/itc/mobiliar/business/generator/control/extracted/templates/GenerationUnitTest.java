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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenerationUnitTest {

	EntityBuilder builder = new AmwEntityBuilder();
	GenerationPackage generationPackage = new GenerationPackage();
	Set<TemplateDescriptorEntity> templates = Set.of();

	@Test
	public void testInsertIntoSet() {
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
		
		subPackage1.addGenerationUnit(buildGenerationUnit(builder.resourceFor(AD), builder.resourceFor(APP), templates));
		GenerationUnit package1 = buildGenerationUnit(builder.resourceFor(APP), null, templates);
		subPackage1.setPackageGenerationUnit(package1);
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		GenerationUnit package2 = buildGenerationUnit(builder.resourceFor(APP), null, templates);
		subPackage2.setPackageGenerationUnit(package2);
		
		
		GenerationSubPackage subPackage3 = new GenerationSubPackage();
		GenerationUnit package3 = buildGenerationUnit(builder.resourceFor(AS), null, templates);
		subPackage3.setPackageGenerationUnit(package3);
		
		
		generationPackage.addGenerationSubPackage(subPackage1);
		generationPackage.addGenerationSubPackage(subPackage2);
		generationPackage.addGenerationSubPackage(subPackage3);

		assertEquals(3, generationPackage.getAsSet().size());
	}

	@Test
	public void testTwoApplicationsAll() {
		ResourceEntity app2 = builder.buildResource(builder.typeFor(APP.type), "amw_2");
		ResourceEntity ad2 = builder.buildResource(builder.typeFor(AD.type), "ad_2");

		GenerationSubPackage subPackage1 = new GenerationSubPackage();
		subPackage1.addGenerationUnit(buildGenerationUnit(builder.resourceFor(AD), builder.resourceFor(APP), templates));
		GenerationUnit package1GenerationUnit = buildGenerationUnit(builder.resourceFor(APP), null, templates);
		subPackage1.setPackageGenerationUnit(package1GenerationUnit);
		generationPackage.addGenerationSubPackage(subPackage1);
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		subPackage2.addGenerationUnit(buildGenerationUnit(ad2, app2, templates));
		GenerationUnit package2GenerationUnit = buildGenerationUnit(app2, null, templates);
		subPackage2.setPackageGenerationUnit(package2GenerationUnit);
		generationPackage.addGenerationSubPackage(subPackage2);
		
		GenerationSubPackage subPackage3 = new GenerationSubPackage();
		subPackage3.addGenerationUnit(buildGenerationUnit(builder.resourceFor(NODE1), builder.resourceFor(AS), templates));
		subPackage3.addGenerationUnit(buildGenerationUnit(builder.resourceFor(NODE2), builder.resourceFor(AS), templates));
		GenerationUnit package3GenerationUnit = buildGenerationUnit(builder.resourceFor(AS), null, templates);
		subPackage3.setPackageGenerationUnit(package3GenerationUnit);
		generationPackage.addGenerationSubPackage(subPackage3);
		
		
		Map<ResourceEntity, Set<GenerationUnit>> batches = generationPackage.getAppGenerationBatches();
		assertEquals(batches.size(), 2);
		assertEquals(batches.get(builder.resourceFor(APP)).size(), 2);
		assertEquals(batches.get(app2).size(), 2);

		assertEquals(2, GenerationUnit.forAppServer(generationPackage.getAsSet(), builder.resourceFor(NODE1)).size());
		assertEquals(2, GenerationUnit.forAppServer(generationPackage.getAsSet(), builder.resourceFor(NODE2)).size());
	}

	@Test
	public void testNodeList() {
		
		GenerationUnit adU = buildGenerationUnit(builder.resourceFor(AD), null, templates);
		GenerationUnit appU = buildGenerationUnit(builder.resourceFor(APP), null, templates);
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
		subPackage1.addGenerationUnit(adU);
		subPackage1.setPackageGenerationUnit(appU);
		
		GenerationUnit node1U = buildGenerationUnit(builder.resourceFor(NODE1), null, templates);
		GenerationUnit node2U = buildGenerationUnit(builder.resourceFor(NODE2), null, templates);
		GenerationUnit asU = buildGenerationUnit(builder.resourceFor(AS), null, templates);
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		subPackage2.addGenerationUnit(node1U);
		subPackage2.addGenerationUnit(node2U);
		subPackage2.setPackageGenerationUnit(asU);
		
		generationPackage.addGenerationSubPackage(subPackage1);
		generationPackage.addGenerationSubPackage(subPackage2);

		Set<GenerationUnit> nodes = generationPackage.getNodeGenerationUnits();
		assertEquals(2, nodes.size());
	}

	private GenerationUnit buildGenerationUnit(ResourceEntity slaveResource, ResourceEntity resource, Set<TemplateDescriptorEntity> resourceTemplates) {
		return new GenerationUnit(slaveResource, resource, resourceTemplates, null);
	}
}
