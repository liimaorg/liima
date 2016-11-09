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

package ch.puzzle.itc.mobiliar.business.issues;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AD;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AS;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.JBOSS7MANAGEMENT;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.puzzle.itc.mobiliar.business.appserverrelation.control.AppServerRelationPath;
import ch.puzzle.itc.mobiliar.business.appserverrelation.boundary.AppServerRelation;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.test.PersistingEntityBuilder;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;

@RunWith(PersistenceTestRunner.class)
public class Issue6110AppServerRelationsServiceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	AppServerRelation service;
	
	@Mock
	ResourceDependencyResolverService resourceDependencyResolver;
	
	@Mock
	ResourceEditService resourceEditService;

	private PersistingEntityBuilder builder;

	private ResourceEntity as;

	private ResourceEntity app;
	
	private ReleaseEntity release;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(resourceDependencyResolver.getResourceEntityForRelease(Mockito.any(ResourceGroupEntity.class), Mockito.any(ReleaseEntity.class))).thenAnswer(new Answer<ResourceEntity>(){

			@Override
			public ResourceEntity answer(InvocationOnMock invocation)
					throws Throwable {
				return ((ResourceGroupEntity)invocation.getArguments()[0]).getResources().iterator().next();
			}});
		Mockito.when(resourceDependencyResolver.getConsumedMasterRelationsForRelease(Mockito.any(ResourceEntity.class), Mockito.any(ReleaseEntity.class))).thenAnswer(new Answer<Set<ConsumedResourceRelationEntity>>(){

			@Override
			public Set<ConsumedResourceRelationEntity> answer(InvocationOnMock invocation)
					throws Throwable {
				return ((ResourceEntity)invocation.getArguments()[0]).getConsumedMasterRelations();
			}});
		Mockito.when(resourceEditService.loadResourceEditRelationsFromType(Mockito.any(ResourceTypeEntity.class))).thenReturn(new ArrayList<ResourceEditRelation>());
		builder = new PersistingEntityBuilder(entityManager).buildSimple();
		as = builder.resourceFor(AS);
		app = builder.resourceFor(APP);
		release = new ReleaseEntity();
		release.setInstallationInProductionAt(new Date());
		entityManager.persist(release);
		as.setRelease(release);
		
	}
	
	
	@Test
	public void testPersistingDirtyRelations() throws GeneralDBException {
		ResourceEntity jboss = builder.buildResource(builder.buildResourceType(JBOSS7MANAGEMENT.type), JBOSS7MANAGEMENT.name);
		ResourceEntity adIntern = builder.buildResource(builder.buildResourceType(AD.type), AD.name);
		ResourceEntity adExtern = builder.buildResource(builder.typeFor(AD.type), "adExtern");

		builder.buildConsumedRelation(as, app, ForeignableOwner.AMW);
		builder.buildConsumedRelation(app, adIntern, ForeignableOwner.AMW);
		builder.buildConsumedRelation(app, jboss, ForeignableOwner.AMW);
		builder.buildConsumedRelation(jboss, adIntern, ForeignableOwner.AMW);

		
		
		List<AppServerRelationPath> appServerRelations = service.getAppServerRelationsFromLiveDB(as.getId(), as.getRelease());
		AppServerRelationPath appAd = AppServerRelationPath.findPathByResourceNames(appServerRelations, AS.name, APP.name, AD.name);
		AppServerRelationPath ldapAd = AppServerRelationPath.findPathByResourceNames(appServerRelations, AS.name, APP.name, JBOSS7MANAGEMENT.name, AD.name);
		
		
		// given both point to the same active directory
		assertEquals(adIntern.getId(), appAd.getSelectedResource().getId());
		assertEquals(adIntern.getId(), ldapAd.getSelectedResource().getId());
//
//		// when I change one of the active directories and persist
		ldapAd.setSelectedResourceGroup(adExtern.getResourceGroup());
		service.storeAppServerRelationPaths(appServerRelations);
		
		List<AppServerRelationPath> relations = service.getAppServerRelationsFromLiveDB(as.getId(), as.getRelease());
		AppServerRelationPath appAdNew = AppServerRelationPath.findPathByResourceNames(relations, AS.name, APP.name, AD.name);
		AppServerRelationPath ldapAdNew = AppServerRelationPath.findPathByResourceNames(relations, AS.name, APP.name, JBOSS7MANAGEMENT.name, AD.name);
		
		assertEquals(adIntern.getId(), appAdNew.getSelectedResource().getId());
		Assert.assertNotEquals(adIntern.getId(), ldapAdNew.getSelectedResource().getId());
		assertEquals(adExtern.getId(), ldapAdNew.getSelectedResource().getId());
		

	}

}
