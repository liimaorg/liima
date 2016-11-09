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

package ch.puzzle.itc.mobiliar.business.appserverrelation.boundary;

import ch.puzzle.itc.mobiliar.business.appserverrelation.control.AppServerRelationPath;
import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationCapable;
import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationHierarchyEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

public class AppServerRelationTest {

	@InjectMocks
	AppServerRelation service;
	
	@Mock
	ResourceDependencyResolverService dependencyResolver;
	
	@Mock
	ResourceEditService resourceEditService;
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(dependencyResolver.getConsumedMasterRelationsForRelease(Mockito.any(ResourceEntity.class), Mockito.any(ReleaseEntity.class))).thenAnswer(new Answer<Set<ConsumedResourceRelationEntity>>() {

			@Override
			public Set<ConsumedResourceRelationEntity> answer(InvocationOnMock invocation)
					throws Throwable {
				return ((ResourceEntity)invocation.getArguments()[0]).getConsumedMasterRelations();
			}
		});
		Mockito.when(resourceEditService.loadResourceEditRelationsFromType(Mockito.any(ResourceTypeEntity.class))).thenReturn(Collections.<ResourceEditRelation>emptyList());
	}

	
	@Test
	public void testGetAllConsumedRelationsRec() {
		ConsumedResourceRelationEntity firstRelation = createConsumedRelMock(1, null);
		ConsumedResourceRelationEntity secondRelationA = createConsumedRelMock(2, firstRelation);
		ConsumedResourceRelationEntity secondRelationB = createConsumedRelMock(3, firstRelation);
		ConsumedResourceRelationEntity thirdRelation = createConsumedRelMock(4, secondRelationA);	
		ResourceEntity finalResource = Mockito.mock(ResourceEntity.class);
		ResourceTypeEntity finalResourceType = Mockito.mock(ResourceTypeEntity.class);
		Mockito.when(finalResource.getId()).thenReturn(999);
		Mockito.when(finalResource.getResourceType()).thenReturn(finalResourceType);
		Mockito.when(thirdRelation.getSlaveResourceTypeName()).thenReturn("finalResourceType");
				
		List<AppServerRelationPath> relations = new ArrayList<>();
		service.getAllConsumedRelationsRec(firstRelation.getMasterResource().getRelease(), firstRelation.getMasterResource(), new ArrayList<AppServerRelationCapable>(0), relations);
		
		//There are 2 paths which are incomplete. This is the expected behaviour since they are filtered in a later step.
		Assert.assertEquals(4, relations.size());
		boolean foundFirstPath = false;
		boolean foundSecondPath = false;
		boolean foundPathWithFirstRelationOnly = false;
		for(AppServerRelationPath relation : relations){			
			Assert.assertNull(relation.getAppserverRelation());
			if(relation.getPath().equals(Arrays.asList(firstRelation, secondRelationA, thirdRelation))){
				foundFirstPath = true;
			}
			else if(relation.getPath().equals(Arrays.asList(firstRelation, secondRelationB))){
				foundSecondPath = true;
			}
		}
		
		Assert.assertTrue(foundFirstPath);
		Assert.assertTrue(foundSecondPath);
	}
	
	
	@Test
	public void testMergeAppServerRelations() {
		ConsumedResourceRelationEntity firstRelation = createConsumedRelMock(1, null);
		ConsumedResourceRelationEntity secondRelationA = createConsumedRelMock(2, firstRelation);
		ConsumedResourceRelationEntity secondRelationB = createConsumedRelMock(3, firstRelation);
		ConsumedResourceRelationEntity thirdRelation = createConsumedRelMock(4, secondRelationA);		

		AppServerRelationHierarchyEntity firstElement = createHierarchyMock(1, null, firstRelation);
		AppServerRelationHierarchyEntity secondElement = createHierarchyMock(2, firstElement, secondRelationA);
		AppServerRelationHierarchyEntity thirdElement = createHierarchyMock(3, secondElement, thirdRelation);
		
		AppServerRelationPath path = new AppServerRelationPath(Arrays.asList((AppServerRelationCapable)firstRelation, secondRelationA, thirdRelation));
		AppServerRelationPath path2 = new AppServerRelationPath(Arrays.asList((AppServerRelationCapable)firstRelation, secondRelationB, thirdRelation));
		
		service.entityManager =  Mockito.mock(EntityManager.class);
		TypedQuery<AppServerRelationHierarchyEntity> q = Mockito.mock(TypedQuery.class);
		Mockito.when(service.entityManager.createQuery(Mockito.anyString(), Mockito.same(AppServerRelationHierarchyEntity.class))).thenReturn(q);
		Mockito.when(q.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(q);
		Mockito.when(q.getResultList()).thenReturn(Arrays.asList(secondElement, thirdElement, firstElement));
		
		
		service.mergeAppServerRelations(Arrays.asList(path, path2), true);
		
		Assert.assertEquals(thirdElement, path.getAppserverRelation());
		Assert.assertNull(path2.getAppserverRelation());
	}
	
	@Test
	public void testFilterLastPath() {
		ConsumedResourceRelationEntity firstRelation = createConsumedRelMock(1, null);
		ConsumedResourceRelationEntity secondRelation = createConsumedRelMock(2, firstRelation);
		ConsumedResourceRelationEntity thirdRelation = createConsumedRelMock(4, secondRelation);		

		AppServerRelationHierarchyEntity firstElement = createHierarchyMock(1, null, firstRelation);
		AppServerRelationHierarchyEntity secondElement = createHierarchyMock(2, firstElement, secondRelation);
		AppServerRelationHierarchyEntity thirdElement = createHierarchyMock(3, secondElement, thirdRelation);
		
		AppServerRelationPath path = new AppServerRelationPath(Arrays.asList((AppServerRelationCapable)firstRelation, secondRelation, thirdRelation));

		List<AppServerRelationHierarchyEntity> result = service.filterLastElements(Arrays.asList(firstElement, secondElement, thirdElement), Arrays.asList(path));
		
		Assert.assertTrue(result.contains(thirdElement));
	}
	
	

	@Test
	public void testIsSamePath() {
		
		ConsumedResourceRelationEntity firstRelation = createConsumedRelMock(1, null);
		ConsumedResourceRelationEntity secondRelation = createConsumedRelMock(2, firstRelation);
		ConsumedResourceRelationEntity thirdRelation = createConsumedRelMock(3, secondRelation);		

		AppServerRelationHierarchyEntity firstElement = createHierarchyMock(1, null, firstRelation);
		AppServerRelationHierarchyEntity secondElement = createHierarchyMock(2, firstElement, secondRelation);
		AppServerRelationHierarchyEntity thirdElement = createHierarchyMock(3, secondElement, thirdRelation);
		
		AppServerRelationPath path = new AppServerRelationPath(Arrays.asList((AppServerRelationCapable)firstRelation, secondRelation, thirdRelation));
		
		boolean result = service.isSamePath(thirdElement, path);
		
		Assert.assertTrue(result);
		
	}
	
	
	@Test
	public void testIsSamePathNOK_wrongOrderOfRelations() {
		
		ConsumedResourceRelationEntity firstRelation = createConsumedRelMock(1, null);
		ConsumedResourceRelationEntity secondRelation = createConsumedRelMock(2, firstRelation);
		ConsumedResourceRelationEntity thirdRelation = createConsumedRelMock(3, secondRelation);		

		AppServerRelationHierarchyEntity firstElement = createHierarchyMock(1, null, firstRelation);
		AppServerRelationHierarchyEntity secondElement = createHierarchyMock(2, firstElement, secondRelation);
		AppServerRelationHierarchyEntity thirdElement = createHierarchyMock(3, secondElement, thirdRelation);
		
		AppServerRelationPath path = new AppServerRelationPath(Arrays.asList((AppServerRelationCapable)secondRelation, thirdRelation, firstRelation));
		
		boolean result = service.isSamePath(thirdElement, path);
		
		Assert.assertFalse(result);
		
	}
	
	@Test
	public void testIsSamePathNOK_wrongOrderOfHierarchy() {
		
		ConsumedResourceRelationEntity firstRelation = createConsumedRelMock(1, null);
		ConsumedResourceRelationEntity secondRelation = createConsumedRelMock(2, firstRelation);
		ConsumedResourceRelationEntity thirdRelation = createConsumedRelMock(3, secondRelation);		

		AppServerRelationHierarchyEntity firstElement = createHierarchyMock(1, null, secondRelation);
		AppServerRelationHierarchyEntity secondElement = createHierarchyMock(2, firstElement, firstRelation);
		AppServerRelationHierarchyEntity thirdElement = createHierarchyMock(3, secondElement, thirdRelation);
		
		AppServerRelationPath path = new AppServerRelationPath(Arrays.asList((AppServerRelationCapable)firstRelation, secondRelation, thirdRelation));
		
		boolean result = service.isSamePath(thirdElement, path);
		
		Assert.assertFalse(result);
		
	}

	
	private ConsumedResourceRelationEntity createConsumedRelMock(Integer id, ConsumedResourceRelationEntity parentRelation){
		ResourceEntity masterResource;		
		if(parentRelation!=null){
			masterResource = parentRelation.getSlaveResource();
		}
		else{
			masterResource = Mockito.mock(ResourceEntity.class);
			Mockito.when(masterResource.getId()).thenReturn(id-1);	
		}	
		ResourceTypeEntity resourceType = Mockito.mock(ResourceTypeEntity.class);
		Mockito.when(masterResource.getResourceType()).thenReturn(resourceType);
		Mockito.when(resourceType.isDefaultResourceType()).thenReturn(false);
		Mockito.when(resourceType.getName()).thenReturn("someResType");
		ResourceEntity slaveResource = Mockito.mock(ResourceEntity.class);
		Mockito.when(slaveResource.getId()).thenReturn(id);	
		Mockito.when(slaveResource.getResourceType()).thenReturn(resourceType);
		ReleaseEntity release = Mockito.mock(ReleaseEntity.class);
		Mockito.when(release.getId()).thenReturn(1);
		Mockito.when(slaveResource.getRelease()).thenReturn(release);		
		
		ConsumedResourceRelationEntity mock = Mockito.mock(ConsumedResourceRelationEntity.class);
		Mockito.when(mock.getBaseClass()).thenCallRealMethod();
		ResourceRelationTypeEntity relationType = Mockito.mock(ResourceRelationTypeEntity.class);
		Mockito.when(mock.getId()).thenReturn(id);
		Mockito.when(relationType.getId()).thenReturn(id+200);
		Mockito.when(mock.getResourceRelationType()).thenReturn(relationType);
		Mockito.when(mock.getMasterResource()).thenReturn(masterResource);
		Mockito.when(mock.getSlaveResource()).thenReturn(slaveResource);	
		Mockito.when(mock.getSlaveResourceTypeName()).thenReturn("someResType");
		
		
		Set<ConsumedResourceRelationEntity> consumedMasterRelations = masterResource.getConsumedMasterRelations();
		if(consumedMasterRelations==null){
			consumedMasterRelations = new HashSet<>();
		}
		Mockito.when(masterResource.getConsumedMasterRelations()).thenReturn(consumedMasterRelations);
		consumedMasterRelations.add(mock);
								
		Set<ConsumedResourceRelationEntity> consumedSlaveRelations = new HashSet<>();
		consumedSlaveRelations.add(mock);
		Mockito.when(slaveResource.getConsumedSlaveRelations()).thenReturn(consumedSlaveRelations);
		
		return mock;
	}
	
	
	private AppServerRelationHierarchyEntity createHierarchyMock(Integer id, AppServerRelationHierarchyEntity parent, ConsumedResourceRelationEntity assignedRelation){
		AppServerRelationHierarchyEntity mock = Mockito.mock(AppServerRelationHierarchyEntity.class);
		Mockito.when(mock.getId()).thenReturn(id);
		Mockito.when(mock.getParentRelation()).thenReturn(parent);
		Mockito.when(mock.getAssignedConsumedResourceRelation()).thenReturn(assignedRelation);
		return mock;
	}
}
