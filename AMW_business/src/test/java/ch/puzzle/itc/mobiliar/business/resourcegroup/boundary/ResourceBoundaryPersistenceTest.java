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

package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotDeletableException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PersistenceTestRunner.class)
public class ResourceBoundaryPersistenceTest {

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
    ForeignableService foreignableService;

    @Mock
    ResourceRepository resourceRepository;

    @Mock
    ResourceGroupRepository resourceGroupRepository;

    @Mock
    ReleaseMgmtPersistenceService releaseService;

    @Mock
    PermissionBoundary permissionBoundary;

    @InjectMocks
    ResourceBoundary resourceBoundary;

    @Mock
    Logger log;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_createNewResourceByName() throws ElementAlreadyExistsException, ResourceTypeNotFoundException,
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
        when(commonService.getResourceTypeEntityById(appType.getId())).thenReturn(appType);
        when(permissionBoundary.canCreateResourceInstance(appType)).thenReturn(Boolean.TRUE);

        // when
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId())).thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(null);
        Resource r1 = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1", appType.getId(),
                release1.getId());

        // then
        assertNotNull(r1);

        // when
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId())).thenReturn(r1.getEntity().getResourceGroup());
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(r1.getEntity().getResourceGroup());
        exception.expect(ElementAlreadyExistsException.class);
        Resource r2 = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1", appType.getId(),
                release2.getId());
        assertNull(r2);
    }

    @Test(expected = ElementAlreadyExistsException.class)
    public void creationOfNewResourceWithSameNameAsAnExistingOfDifferentTypeShouldFail() throws ElementAlreadyExistsException,
            ResourceTypeNotFoundException, ResourceNotFoundException {
        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("application");
        entityManager.persist(appType);
        ResourceGroupEntity app = new ResourceGroupEntity();
        app.setResourceType(appType);
        app.setName("test");
        entityManager.persist(app);

        ResourceTypeEntity asType = new ResourceTypeEntity();
        appType.setName("applicationserver");
        entityManager.persist(asType);
        ResourceGroupEntity as = new ResourceGroupEntity();
        as.setResourceType(asType);
        as.setName("test");

        ReleaseEntity release1 = new ReleaseEntity();
        release1.setName("release1");
        entityManager.persist(release1);

        when(releaseService.getById(release1.getId())).thenReturn(release1);
        when(commonService.getResourceTypeEntityById(asType.getId())).thenReturn(asType);
        when(permissionBoundary.canCreateResourceInstance(asType)).thenReturn(Boolean.TRUE);
        when(resourceGroupRepository.loadUniqueGroupByNameAndType(as.getName(), as.getResourceType().getId())).thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName(as.getName())).thenReturn(app);

        // when // then
        resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "test", asType.getId(), release1.getId());
    }

    @Test
    public void shouldDeleteRestrictionsWhenDeletingResource() throws ElementAlreadyExistsException, ResourceTypeNotFoundException,
            ResourceNotFoundException, ResourceNotDeletableException, ForeignableOwnerViolationException {
        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("application");
        entityManager.persist(appType);
        ReleaseEntity release1 = new ReleaseEntity();
        release1.setName("release1");
        entityManager.persist(release1);

        when(releaseService.getById(release1.getId())).thenReturn(release1);
        when(commonService.getResourceTypeEntityById(appType.getId())).thenReturn(appType);
        when(permissionBoundary.canCreateResourceInstance(appType)).thenReturn(Boolean.TRUE);
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId())).thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(null);
        Resource r1 = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1", appType.getId(),
                release1.getId());
        when(commonService.getResourceEntityById(r1.getId())).thenReturn(r1.getEntity());
        when(permissionBoundary.hasPermission(any(Permission.class), ArgumentMatchers.<ContextEntity>any(), any(Action.class),
                any(ResourceEntity.class), any(ResourceTypeEntity.class))).thenReturn(true);
        when(resourceGroupRepository.find(r1.getEntity().getResourceGroup().getId())).thenReturn(r1.getEntity().getResourceGroup());

        // when
        resourceBoundary.removeResource(ForeignableOwner.AMW, r1.getId());

        // then
        verify(permissionBoundary).removeAllRestrictionsForResourceGroup(r1.getEntity().getResourceGroup());
        verify(resourceGroupRepository).remove(r1.getEntity().getResourceGroup());
    }

}
