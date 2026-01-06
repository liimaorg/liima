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
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenerationPackageTest {

	EntityBuilder builder = new AmwEntityBuilder();
	GenerationPackage g = new GenerationPackage();
	
	private List<GenerationUnit> gus = new ArrayList<GenerationUnit>();

	@Test
	public void should_stay_one() {
		// given
		gus.add(new GenerationUnit(null, null, null, null));
		// when
		List<GenerationUnit> result = GenerationPackage.removeDuplicates(gus);
		
		// then
		assertEquals(1, result.size());
	}
	
	@Test
	public void should_remove_one_APP() {
		// given
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		// when
		List<GenerationUnit> result = GenerationPackage.removeDuplicates(gus);
		
		// then
		assertEquals(1, result.size());
	}
	
	@Test
	public void should_not_remove_one_APP_because_same_parent() {
		// given
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), builder.resourceFor(EntityBuilderType.WS), null, null));
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), builder.resourceFor(EntityBuilderType.AS), null, null));
		// when
		List<GenerationUnit> result = GenerationPackage.removeDuplicates(gus);
		
		// then
		assertEquals(2, result.size());
	}
	
	@Test
	public void should_remove_one_APP_because_p1_null() {
		// given
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), builder.resourceFor(EntityBuilderType.AS), null, null));
		// when
		List<GenerationUnit> result = GenerationPackage.removeDuplicates(gus);
		
		// then
		assertEquals(1, result.size());
	}
	
	@Test
	public void should_not_remove_one_APP_because_p2_null() {
		// given
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), builder.resourceFor(EntityBuilderType.AS), null, null));
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		// when
		List<GenerationUnit> result = GenerationPackage.removeDuplicates(gus);
		
		// then
		assertEquals(2, result.size());
	}
	
	
	@Test
	public void should_not_remove_WS() {
		// given
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), null, null, null));
		gus.add(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), null, null, null));
		// when
		List<GenerationUnit> result = GenerationPackage.removeDuplicates(gus);
		
		// then
		assertEquals(2, result.size());
	}
	
	@Test
	public void should_add_one_subPackage() {
		// given
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		// when
		g.addGenerationSubPackage(subPackage1);
		
		// then
		assertEquals(2, g.getAsSet().size());
	}
	
	@Test
	public void should_add_two_subPackage() {
		// given
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.WS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), null, null, null));
		
		// when
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// then
		List<GenerationUnit> asSet = new ArrayList<GenerationUnit>(g.getAsSet());
		assertEquals(4, asSet.size());
		assertEquals(EntityBuilderType.DB2.type, asSet.get(0).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.WS.type, asSet.get(1).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.WS.type, asSet.get(2).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.APP.type, asSet.get(3).getSlaveResource().getResourceType().getName());
		assertEquals(2, g.getGenerationSubPackages().size());
		
		assertEquals(2, g.getGenerationSubPackages().get(1).getSubGenerationUnitsAsList().size());
		assertEquals(4, g.getGenerationSubPackages().get(0).getSubGenerationUnitsAsList().size());
	}
	
	@Test
	public void should_add_two_subPackage_order() {
		// given
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.WS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), null, null, null));
		
		// when
		g.addGenerationSubPackage(subPackage2);
		g.addGenerationSubPackage(subPackage1);
		
		// then
		List<GenerationUnit> asSet = new ArrayList<GenerationUnit>(g.getAsSet());
		assertEquals(4, asSet.size());
		assertEquals(EntityBuilderType.DB2.type, asSet.get(0).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.WS.type, asSet.get(1).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.WS.type, asSet.get(2).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.APP.type, asSet.get(3).getSlaveResource().getResourceType().getName());
		assertEquals(2, g.getGenerationSubPackages().size());
		
		assertEquals(2, g.getGenerationSubPackages().get(0).getSubGenerationUnitsAsList().size());
		assertEquals(4, g.getGenerationSubPackages().get(1).getSubGenerationUnitsAsList().size());
	}
	
	@Test
	public void should_add_two_subPackage_NotAdded_as_Subpackage() {
		// given
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.WS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.LB), null, null, null));
		
		// when
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// then
		List<GenerationUnit> asSet = new ArrayList<GenerationUnit>(g.getAsSet());
		assertEquals(4, asSet.size());
		assertEquals(EntityBuilderType.WS.type, asSet.get(0).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.APP.type, asSet.get(1).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.DB2.type, asSet.get(2).getSlaveResource().getResourceType().getName());
		assertEquals(EntityBuilderType.LB.type, asSet.get(3).getSlaveResource().getResourceType().getName());
		
		assertEquals(2, g.getGenerationSubPackages().size());
		
		assertEquals(2, g.getGenerationSubPackages().get(0).getSubGenerationUnitsAsList().size());
		assertEquals(2, g.getGenerationSubPackages().get(1).getSubGenerationUnitsAsList().size());
	}
	
	@Test
	public void should_returnOneAppBatch() {
		// given
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.WS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.LB), null, null, null));
		
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// when
		Map<ResourceEntity, Set<GenerationUnit>> appGenerationBatches = g.getAppGenerationBatches();
		
		// then
		assertEquals(1, appGenerationBatches.size());
		assertEquals(2, appGenerationBatches.get(builder.resourceFor(EntityBuilderType.APP)).size());
		
	}
	
	@Test
	public void should_returnOneAppBatchWithSubPackages() {
		// given
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), builder.resourceFor(EntityBuilderType.WS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), null, null, null));
		
		g.addGenerationSubPackage(subPackage2);
		g.addGenerationSubPackage(subPackage1);
		
		// when
		Map<ResourceEntity, Set<GenerationUnit>> appGenerationBatches = g.getAppGenerationBatches();
		
		// then
		assertEquals(1, appGenerationBatches.size());
		assertEquals(4, appGenerationBatches.get(builder.resourceFor(EntityBuilderType.APP)).size());
		
	}
	
	@Test
	public void should_returnTwoAppBatches() {
		// given
		
		ResourceEntity app2 = builder.buildResource(builder.typeFor(APP.type), "amw_2");
		
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.WS), builder.resourceFor(APP), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), app2, null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(app2, null, null, null));
		
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// when
		Map<ResourceEntity, Set<GenerationUnit>> appGenerationBatches = g.getAppGenerationBatches();
		
		// then
		assertEquals(2, appGenerationBatches.size());
		assertEquals(2, appGenerationBatches.get(builder.resourceFor(EntityBuilderType.APP)).size());
		assertEquals(2, appGenerationBatches.get(app2).size());
		
	}
	
	@Test
	public void should_returnTwoAppBatches_one_on_AS() {
		// given
		
		ResourceEntity app2 = builder.buildResource(builder.typeFor(APP.type), "amw_2");
		
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), builder.resourceFor(EntityBuilderType.AS), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.AS), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.DB2), app2, null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(app2, null, null, null));
		
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// when
		Map<ResourceEntity, Set<GenerationUnit>> appGenerationBatches = g.getAppGenerationBatches();
		
		// then
		assertEquals(2, appGenerationBatches.size());
		assertEquals(1, appGenerationBatches.get(builder.resourceFor(EntityBuilderType.APP)).size());
		assertEquals(2, appGenerationBatches.get(app2).size());
		
	}
	
	@Test
	public void should_returnTwoNodeGenerationUnit() {
		// given
		
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.NODE1), builder.resourceFor(EntityBuilderType.AS), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.AS), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.NODE2), builder.resourceFor(EntityBuilderType.AS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.AS), null, null, null));
		
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// when
		Set<GenerationUnit> nodeUnits = g.getNodeGenerationUnits();
		
		// then
		assertEquals(2, nodeUnits.size());
	}
	
	@Test
	public void should_returnOneNodeGenerationUnit() {
		// given
		
		GenerationSubPackage subPackage1 = new GenerationSubPackage();
	
		subPackage1.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.NODE1), builder.resourceFor(EntityBuilderType.AS), null, null));
		subPackage1.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.AS), null, null, null));
		
		GenerationSubPackage subPackage2 = new GenerationSubPackage();
		
		subPackage2.addGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.APP), builder.resourceFor(EntityBuilderType.AS), null, null));
		subPackage2.setPackageGenerationUnit(new GenerationUnit(builder.resourceFor(EntityBuilderType.AS), null, null, null));
		
		g.addGenerationSubPackage(subPackage1);
		g.addGenerationSubPackage(subPackage2);
		
		// when
		Set<GenerationUnit> nodeUnits = g.getNodeGenerationUnits();
		
		// then
		assertEquals(1, nodeUnits.size());
	}

	private GenerationUnit createGenerationUnitMock(Integer id, boolean runtime, boolean application) {
		GenerationUnit unit = Mockito.mock(GenerationUnit.class);
		ResourceEntity slaveResource = Mockito.mock(ResourceEntity.class);
		ResourceTypeEntity slaveResourceType = Mockito.mock(ResourceTypeEntity.class);
		Mockito.when(slaveResourceType.isApplicationResourceType()).thenReturn(application);
		Mockito.when(slaveResourceType.isRuntimeType()).thenReturn(runtime);
		Mockito.when(slaveResource.getId()).thenReturn(id);
		Mockito.when(unit.getSlaveResource()).thenReturn(slaveResource);
		Mockito.when(slaveResource.getResourceType()).thenReturn(slaveResourceType);
		return unit;
	}

	@Test
	public void test_getAsWithGivenNode() {
		Integer nodeId = 1;
		Integer otherId = 2;
		Integer asId = 3;
		Integer runtimeId = 4;
		Integer appId = 5;
		GenerationSubPackage asSubPackage = new GenerationSubPackage();
		asSubPackage.addGenerationUnit(createGenerationUnitMock(nodeId, false, false));
		asSubPackage.addGenerationUnit(createGenerationUnitMock(appId, false, true));
		asSubPackage.addGenerationUnit(createGenerationUnitMock(otherId, false, false));
		asSubPackage.addGenerationUnit(createGenerationUnitMock(runtimeId, true, false));

		// The app server sub package
		asSubPackage.setPackageGenerationUnit(createGenerationUnitMock(asId, false, false));

		ResourceEntity node = Mockito.mock(ResourceEntity.class);
		Mockito.when(node.getId()).thenReturn(nodeId);

		GenerationPackage p = Mockito.mock(GenerationPackage.class);
		// Lets randomly set the order of the generation units (the logic should be able to handle this)
		Collections.shuffle(asSubPackage.getGenerationUnits());

		Mockito.when(p.getApplicationServerSubPackages()).thenReturn(Arrays.asList(asSubPackage));
		Mockito.when(p.getAsWithGivenNode(Mockito.any(ResourceEntity.class))).thenCallRealMethod();

		Set<GenerationUnit> asWithGivenNode = p.getAsWithGivenNode(node);

		// Although we have added 5 generation units (including the package generation unit), we only expect
		// 4 in the result since application units are filtered
		assertEquals(4, asWithGivenNode.size());
		// Assert order (other, node, as, runtime)
		Iterator<GenerationUnit> iterator = asWithGivenNode.iterator();
		assertEquals(otherId, iterator.next().getSlaveResource().getId());
		assertEquals(nodeId, iterator.next().getSlaveResource().getId());
		assertEquals(asId, iterator.next().getSlaveResource().getId());
		assertEquals(runtimeId, iterator.next().getSlaveResource().getId());
	}
}
