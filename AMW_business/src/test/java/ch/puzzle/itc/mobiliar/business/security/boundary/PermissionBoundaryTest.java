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

package ch.puzzle.itc.mobiliar.business.security.boundary;

import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionRepository;
import ch.puzzle.itc.mobiliar.business.security.control.RestrictionRepository;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.persistence.NoResultException;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.CREATE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PermissionBoundaryTest {

    @InjectMocks
    PermissionBoundary permissionBoundary;
    @Mock
    RestrictionRepository restrictionRepository;
    @Mock
    PermissionRepository permissionRepository;
    @Mock
    ContextLocator contextLocator;
    @Mock
    ResourceTypeRepository resourceTypeRepository;
    @Mock
    ResourceRepository resourceRepository;

    @Before
    public void setup() {
        permissionBoundary = new PermissionBoundary();
        restrictionRepository = Mockito.mock(RestrictionRepository.class);
        permissionBoundary.restrictionRepository = restrictionRepository;
        permissionRepository = Mockito.mock(PermissionRepository.class);
        permissionBoundary.permissionRepository = permissionRepository;
        contextLocator = Mockito.mock(ContextLocator.class);
        permissionBoundary.contextLocator = contextLocator;
        resourceTypeRepository = Mockito.mock(ResourceTypeRepository.class);
        permissionBoundary.resourceTypeRepository = resourceTypeRepository;
        resourceRepository = Mockito.mock(ResourceRepository.class);
        permissionBoundary.resourceRepository = resourceRepository;
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfIdIsNull() throws AMWException {
        // given // when // then
        permissionBoundary.updateRestriction(null, null, null, null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfRestrictionCanNotBeFound() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(null);
        // when // then
        permissionBoundary.updateRestriction(1, null, null, null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfRolenameIsNull() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        // when // then
        permissionBoundary.updateRestriction(1, null, null, null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfRoleCanNotBeFound() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        when(permissionRepository.getRoleByName("notThere")).thenThrow(new NoResultException());
        // when // then
        permissionBoundary.updateRestriction(1, "notThere", null, null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfPermissionIsNull() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        // when // then
        permissionBoundary.updateRestriction(1, "existing", null, null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfPermissionCanNotBeFound() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("invalid")).thenThrow(new NoResultException());
        // when // then
        permissionBoundary.updateRestriction(1, "existing", "invalid", null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfContextCanNotBeFound() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        when(contextLocator.getContextByName("bad")).thenThrow(new NoResultException());
        // when // then
        permissionBoundary.updateRestriction(1, "existing", "good", null, null, "bad", null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnUpdateIfResourceTypeCanNotBeFound() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        when(resourceTypeRepository.getByName("bad")).thenReturn(null);
        // when // then
        permissionBoundary.updateRestriction(1, "existing", "good", null, "bad", null, null);
    }

    @Test
    public void shouldUpdateIfContextAndActionAreNull() throws AMWException {
        // given
        when(restrictionRepository.find(1)).thenReturn(new RestrictionEntity());
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        // when
        permissionBoundary.updateRestriction(1, "existing", "good", null, null, null, null);
        // then
        verify(restrictionRepository, times(1)).merge(any(RestrictionEntity.class));
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnCreateIfRolenameIsNull() throws AMWException {
        // given // when // then
        permissionBoundary.createRestriction(null, null, null, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnCreateIfRoleCanNotBeFound() throws AMWException {
        // given
        when(permissionRepository.getRoleByName("notThere")).thenThrow(new NoResultException());
        // when // then
        permissionBoundary.createRestriction("notThere", null, null, null, null, null);
    }

    @Test
    public void shouldCreateIfContextIsNull() throws AMWException {
        // given
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        // when
        permissionBoundary.createRestriction("existing", "good", null, null, null, CREATE);
        // then
        verify(restrictionRepository, times(1)).create(any(RestrictionEntity.class));
    }

    @Test
    public void shouldCreateIfContextAndActionAreNull() throws AMWException {
        // given
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        // when
        permissionBoundary.createRestriction("existing", "good", null, null, null, null);
        // then
        verify(restrictionRepository, times(1)).create(any(RestrictionEntity.class));
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnCreateIfResourceIdCanNotBeFound() throws AMWException {
        // given
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        when(resourceRepository.find(7)).thenReturn(null);
        // when // then
        permissionBoundary.createRestriction("existing", "good", 7, null, null, null);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionIfRestrictionToBeDeletedCanNotBeFound() throws AMWException {
        // given // when // then
        permissionBoundary.removeRestriction(21);
    }

    @Test(expected=AMWException.class)
    public void shouldRemoveRestrictionToBeDeleted() throws AMWException {
        // given
        when(restrictionRepository.find(42)).thenReturn(new RestrictionEntity());
        // when
        permissionBoundary.removeRestriction(21);
        // then
        verify(restrictionRepository, times(1)).remove(42);
    }



}
