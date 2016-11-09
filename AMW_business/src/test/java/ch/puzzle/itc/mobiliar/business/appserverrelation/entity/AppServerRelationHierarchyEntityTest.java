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

package ch.puzzle.itc.mobiliar.business.appserverrelation.entity;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

public class AppServerRelationHierarchyEntityTest {

	AppServerRelationHierarchyEntity entity;
	
	@Before
	public void setUp() throws Exception {
		entity = new AppServerRelationHierarchyEntity();
	}

	@Test
	public void testGetRelationConsumed() {
		ConsumedResourceRelationEntity rel = Mockito.mock(ConsumedResourceRelationEntity.class);
		entity.assignedConsumedResourceRelation = rel;
				
		HasContexts<? extends ContextDependency<?>> relation = entity.getRelation();
		
		Assert.assertTrue(relation==rel);
	}
	
	@Test
	public void testGetRelationType() {
		ResourceRelationTypeEntity rel = Mockito.mock(ResourceRelationTypeEntity.class);
		entity.assignedResourceTypeRelation = rel;
				
		HasContexts<? extends ContextDependency<?>> relation = entity.getRelation();
		
		Assert.assertTrue(relation==rel);
	}	


	@Test
	public void testSetRelationConsumedResourceRelationEntity() {
		ConsumedResourceRelationEntity rel = Mockito.mock(ConsumedResourceRelationEntity.class);
		
		entity.setRelation(rel);
		
		Assert.assertTrue(rel==entity.assignedConsumedResourceRelation);
		Assert.assertNull(entity.assignedResourceTypeRelation);
		
	}

	@Test
	public void testSetRelationResourceRelationTypeEntity() {
		ResourceRelationTypeEntity rel = Mockito.mock(ResourceRelationTypeEntity.class);
		
		entity.setRelation(rel);
		
		Assert.assertTrue(rel==entity.assignedResourceTypeRelation);
		Assert.assertNull(entity.assignedConsumedResourceRelation);
		
	}
	
	@Test
	public void testSetRelationResourceRelationTypeEntityOverwritten() {
		ConsumedResourceRelationEntity consRel = Mockito.mock(ConsumedResourceRelationEntity.class);
		
		entity.setRelation(consRel);
		
		ResourceRelationTypeEntity rel = Mockito.mock(ResourceRelationTypeEntity.class);
		
		entity.setRelation(rel);
		
		Assert.assertTrue(rel==entity.assignedResourceTypeRelation);
		Assert.assertNull(entity.assignedConsumedResourceRelation);
		
	}

	@Test
	public void testRefersToRelationConsumedResourceRelationEntity() {
		ConsumedResourceRelationEntity consRel = Mockito.mock(ConsumedResourceRelationEntity.class);	
		Mockito.when(consRel.getId()).thenReturn(1);
		ConsumedResourceRelationEntity otherRel = Mockito.mock(ConsumedResourceRelationEntity.class);	
		Mockito.when(otherRel.getId()).thenReturn(2);
		ResourceRelationTypeEntity resRelType = Mockito.mock(ResourceRelationTypeEntity.class);	
		
		entity.setRelation(consRel);
		
		Assert.assertTrue(entity.refersToRelation(consRel));
		Assert.assertFalse(entity.refersToRelation(otherRel));
		Assert.assertFalse(entity.refersToRelation(resRelType));
	}

	@Test
	public void testRefersToRelationResourceRelationTypeEntity() {
		ConsumedResourceRelationEntity consRel = Mockito.mock(ConsumedResourceRelationEntity.class);	
		ResourceRelationTypeEntity resRelType = Mockito.mock(ResourceRelationTypeEntity.class);	
		Mockito.when(resRelType.getId()).thenReturn(1);
		ResourceRelationTypeEntity otherResRelType = Mockito.mock(ResourceRelationTypeEntity.class);	
		Mockito.when(resRelType.getId()).thenReturn(2);
		
		entity.setRelation(resRelType);
		
		Assert.assertTrue(entity.refersToRelation(resRelType));
		Assert.assertFalse(entity.refersToRelation(consRel));
		Assert.assertFalse(entity.refersToRelation(otherResRelType));
	}

	@Test
	public void testGetApplicationServerDirect() {
		ResourceEntity appServer = Mockito.mock(ResourceEntity.class);
		ConsumedResourceRelationEntity appserverRel = Mockito.mock(ConsumedResourceRelationEntity.class);
		Mockito.when(appserverRel.getMasterResource()).thenReturn(appServer);

		entity.setRelation(appserverRel);
				
		ResourceEntity result = entity.getApplicationServer();
				
		Assert.assertTrue(appServer==result);		
	}
	
	@Test
	public void testGetApplicationServer1Step() {
		ResourceEntity appServer = Mockito.mock(ResourceEntity.class);
		ConsumedResourceRelationEntity appserverRel = Mockito.mock(ConsumedResourceRelationEntity.class);
		Mockito.when(appserverRel.getMasterResource()).thenReturn(appServer);

		AppServerRelationHierarchyEntity parentRelation = new AppServerRelationHierarchyEntity();
		parentRelation.setRelation(appserverRel);
		
		entity.setParentRelation(parentRelation);
				
		ResourceEntity result = entity.getApplicationServer();
				
		Assert.assertTrue(appServer==result);		
	}
	
	@Test
	public void testGetApplicationServer2Step() {
		ResourceEntity appServer = Mockito.mock(ResourceEntity.class);
		ConsumedResourceRelationEntity appserverRel = Mockito.mock(ConsumedResourceRelationEntity.class);
		Mockito.when(appserverRel.getMasterResource()).thenReturn(appServer);

		AppServerRelationHierarchyEntity parentRelation = new AppServerRelationHierarchyEntity();
		
		AppServerRelationHierarchyEntity parentRelation2 = new AppServerRelationHierarchyEntity();
		parentRelation2.setRelation(appserverRel);
		
		parentRelation.setParentRelation(parentRelation2);
		
		entity.setParentRelation(parentRelation);
				
		ResourceEntity result = entity.getApplicationServer();
				
		Assert.assertTrue(appServer==result);		
	}
	
	/**
	 * There is no relation defined in the uppermost hierarchy instance - this is an inconsistency and shall result in a runtime exception
	 */
	@Test(expected=RuntimeException.class)
	public void testGetApplicationServerNOK() {
		
		AppServerRelationHierarchyEntity parentRelation = new AppServerRelationHierarchyEntity();
		
		entity.setParentRelation(parentRelation);
				
		entity.getApplicationServer();
	}
	
	/**
	 * There is a resource relation type defined in the uppermost hierarchy instance - this is an inconsistency and shall result in a runtime exception
	 */
	@Test(expected=RuntimeException.class)
	public void testGetApplicationServerNOKWrongRelationType() {
		ResourceRelationTypeEntity appserverRel = Mockito.mock(ResourceRelationTypeEntity.class);
				
		AppServerRelationHierarchyEntity parentRelation = new AppServerRelationHierarchyEntity();
		parentRelation.setRelation(appserverRel);
		
		entity.setParentRelation(parentRelation);
				
		entity.getApplicationServer();
	}

	
	/**
	 * The uppermost consumed resource relation is 
	 */
	@Test
	public void testGetApplicationServerNullAS() {
		ConsumedResourceRelationEntity appserverRel = Mockito.mock(ConsumedResourceRelationEntity.class);
	
		AppServerRelationHierarchyEntity parentRelation = new AppServerRelationHierarchyEntity();
		parentRelation.setRelation(appserverRel);
		
		entity.setParentRelation(parentRelation);
				
		Assert.assertNull(entity.getApplicationServer());
	}
	
	
	@Test 
	public void testGetRelease(){
		ReleaseEntity firstRelease = Mockito.mock(ReleaseEntity.class);
		Mockito.when(firstRelease.getInstallationInProductionAt()).thenReturn(new Date());
		Mockito.when(firstRelease.compareTo(Mockito.any(ReleaseEntity.class))).thenCallRealMethod();
		
		ReleaseEntity laterRelease = Mockito.mock(ReleaseEntity.class);
		Mockito.when(laterRelease.getInstallationInProductionAt()).thenReturn(DateUtils.addDays(new Date(), 20));
		Mockito.when(laterRelease.compareTo(Mockito.any(ReleaseEntity.class))).thenCallRealMethod();

		ResourceEntity appServer = Mockito.mock(ResourceEntity.class);
		ConsumedResourceRelationEntity appserverRel = Mockito.mock(ConsumedResourceRelationEntity.class);
		Mockito.when(appserverRel.getMasterResource()).thenReturn(appServer);
		Mockito.when(appServer.getRelease()).thenReturn(firstRelease);		
		
		AppServerRelationHierarchyEntity parentRelation = new AppServerRelationHierarchyEntity();
		ResourceEntity parentRes = Mockito.mock(ResourceEntity.class);
		ConsumedResourceRelationEntity parentRel = new ConsumedResourceRelationEntity();
		parentRel.setMasterResource(parentRes);
		parentRelation.setRelation(parentRel);
		Mockito.when(parentRes.getRelease()).thenReturn(firstRelease);
		entity.setRelation(parentRel);
		entity.setParentRelation(parentRelation);
		
		ReleaseEntity release = entity.getRelease();
						
		AppServerRelationHierarchyEntity parentRelation2 = new AppServerRelationHierarchyEntity();
		ResourceEntity parentRes2 = Mockito.mock(ResourceEntity.class);
		ConsumedResourceRelationEntity parentRel2 = new ConsumedResourceRelationEntity();
		parentRel2.setMasterResource(parentRes2);
		parentRelation2.setRelation(parentRel2);
		Mockito.when(parentRes2.getRelease()).thenReturn(laterRelease);
		
		parentRelation.setParentRelation(parentRelation2);
		
		ReleaseEntity release2 = entity.getRelease();
		
		
		Assert.assertNotNull(release);
		Assert.assertNotNull(release2);
		Assert.assertEquals(firstRelease, release);
		Assert.assertEquals(laterRelease, release2);
	}


}
