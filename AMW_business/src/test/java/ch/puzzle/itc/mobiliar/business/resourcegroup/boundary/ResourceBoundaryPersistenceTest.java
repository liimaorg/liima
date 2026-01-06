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

import static ch.puzzle.itc.mobiliar.business.releasing.ReleaseHelper.createRL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

@ExtendWith(PersistenceTestExtension.class)
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

    @Mock
    ReleaseLocator releaseLocator;

    @Mock
    ResourceTypeRepository resourceTypeRepository;

    @InjectMocks
    ResourceBoundary resourceBoundary;

    @Mock
    Logger log;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_createNewResourceByName() throws AMWException {
        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("application");
        entityManager.persist(appType);
        ReleaseEntity release1 = createRL("release1", null);
        entityManager.persist(release1);
        ReleaseEntity release2 = createRL("release2", null);
        entityManager.persist(release2);

        when(releaseService.getById(release1.getId())).thenReturn(release1);
        when(releaseService.getById(release2.getId())).thenReturn(release2);
        when(commonService.getResourceTypeEntityById(appType.getId())).thenReturn(appType);
        when(permissionBoundary.canCreateResourceInstance(appType)).thenReturn(Boolean.TRUE);

        // when
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId())).thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(null);
        Resource r1 = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1",
                appType.getId(),
                release1.getId());

        // then
        assertNotNull(r1);

        // when
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId()))
                .thenReturn(r1.getEntity().getResourceGroup());
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(r1.getEntity().getResourceGroup());
        assertThrows(ElementAlreadyExistsException.class,
                () -> resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1",
                        appType.getId(),
                        release2.getId()));
    }

    @Test
    public void test_createNewResourceByNameStrings() throws AMWException {
        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("application");
        entityManager.persist(appType);
        ReleaseEntity release1 = createRL("release1", null);
        entityManager.persist(release1);
        ReleaseEntity release2 = createRL("release2", null);
        entityManager.persist(release2);

        when(resourceTypeRepository.getByName(appType.getName())).thenReturn(appType);
        when(commonService.getResourceTypeEntityById(appType.getId())).thenReturn(appType);
        when(releaseLocator.getReleaseByName(release1.getName())).thenReturn(release1);
        when(releaseLocator.getReleaseByName(release2.getName())).thenReturn(release2);
        when(releaseService.getById(release1.getId())).thenReturn(release1);
        when(releaseService.getById(release2.getId())).thenReturn(release2);
        when(permissionBoundary.canCreateResourceInstance(appType)).thenReturn(Boolean.TRUE);

        // first creation
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId())).thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(null);
        Resource r1 = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1",
                appType.getName(),
                release1.getName());
        assertNotNull(r1);

        // attempt duplicate creation in different release should fail because
        // canCreateReleaseOfExisting is false
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId()))
                .thenReturn(r1.getEntity().getResourceGroup());
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(r1.getEntity().getResourceGroup());
        assertThrows(ElementAlreadyExistsException.class,
                () -> resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1",
                        appType.getName(),
                        release2.getName()));
    }

    @Test
    public void creationOfNewResourceWithSameNameAsAnExistingOfDifferentTypeShouldFail() throws AMWException {
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

        ReleaseEntity release1 = createRL("release1", null);
        entityManager.persist(release1);

        when(releaseService.getById(release1.getId())).thenReturn(release1);
        when(commonService.getResourceTypeEntityById(asType.getId())).thenReturn(asType);
        when(permissionBoundary.canCreateResourceInstance(asType)).thenReturn(Boolean.TRUE);
        when(resourceGroupRepository.loadUniqueGroupByNameAndType(as.getName(), as.getResourceType().getId()))
                .thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName(as.getName())).thenReturn(app);

        // when // then
        assertThrows(ElementAlreadyExistsException.class, () -> {
            resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "test", asType.getId(),
                    release1.getId());
        });
    }

    @Test
    public void shouldDeleteRestrictionsWhenDeletingResource() throws ForeignableOwnerViolationException, AMWException {
        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("application");
        entityManager.persist(appType);
        ReleaseEntity release1 = createRL("release1", null);
        entityManager.persist(release1);

        when(releaseService.getById(release1.getId())).thenReturn(release1);
        when(commonService.getResourceTypeEntityById(appType.getId())).thenReturn(appType);
        when(permissionBoundary.canCreateResourceInstance(appType)).thenReturn(Boolean.TRUE);
        when(resourceGroupRepository.loadUniqueGroupByNameAndType("app1", appType.getId())).thenReturn(null);
        when(resourceGroupRepository.getResourceGroupByName("app1")).thenReturn(null);
        Resource r1 = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), "app1",
                appType.getId(),
                release1.getId());
        when(commonService.getResourceEntityById(r1.getId())).thenReturn(r1.getEntity());
        when(permissionBoundary.hasPermission(any(Permission.class), ArgumentMatchers.<ContextEntity>any(),
                any(Action.class),
                any(ResourceEntity.class), any(ResourceTypeEntity.class))).thenReturn(true);
        when(resourceGroupRepository.find(r1.getEntity().getResourceGroup().getId()))
                .thenReturn(r1.getEntity().getResourceGroup());

        // when
        resourceBoundary.removeResource(ForeignableOwner.AMW, r1.getId());

        // then
        verify(permissionBoundary).removeAllRestrictionsForResourceGroup(r1.getEntity().getResourceGroup());
        verify(resourceGroupRepository).remove(r1.getEntity().getResourceGroup());
    }

}
