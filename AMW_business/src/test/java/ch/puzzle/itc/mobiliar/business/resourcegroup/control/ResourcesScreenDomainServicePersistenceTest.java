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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ch.puzzle.itc.mobiliar.business.security.boundary.Permissions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;

@RunWith(PersistenceTestRunner.class)
public class ResourcesScreenDomainServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	CommonDomainService commonService;

	@Mock
	private ContextDomainService contextDomainService;

	@Mock
	private ResourceTypeProvider resourceTypeProvider;

	@Mock
	private ResourcesScreenQueries queries;


	@Mock
	ResourceGroupPersistenceService resourceGroupService;

	@Mock
	ReleaseMgmtPersistenceService releaseService;

	@Mock
	Permissions permissionBoundry;

	@InjectMocks
	ResourcesScreenDomainService service;

	@Mock
	Logger log;

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_createNewResourceByName()
			throws ElementAlreadyExistsException,
			ResourceTypeNotFoundException, GeneralDBException,
			ResourceNotFoundException {
		// given
		ResourceTypeEntity appType = new ResourceTypeEntity();
		appType.setName("application");
		entityManager.persist(appType);
		ReleaseEntity release1 = new ReleaseEntity();
		release1.setName("release1");
		entityManager.persist(release1);
		ReleaseEntity release2 = new ReleaseEntity();
		release1.setName("release2");
		entityManager.persist(release2);

		when(releaseService.getById(release1.getId())).thenReturn(release1);
		when(releaseService.getById(release2.getId())).thenReturn(release2);
		when(commonService.getResourceTypeEntityById(appType.getId()))
		.thenReturn(appType);
		when(permissionBoundry.canCreateResourceInstance(appType)).thenReturn(Boolean.TRUE);

		// when
		when(
				resourceGroupService.loadUniqueGroupByNameAndType("app1",
						appType.getId())).thenReturn(null);
		Resource r1 = service.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1", appType.getId(),
                release1.getId());

		// then
		assertNotNull(r1);

		// when
		when(
				resourceGroupService.loadUniqueGroupByNameAndType("app1",
						appType.getId())).thenReturn(
								r1.getEntity().getResourceGroup());
		exception.expect(ElementAlreadyExistsException.class);
		Resource r2 = service.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1", appType.getId(),
                release2.getId());
		assertNull(r2);
	}

}
