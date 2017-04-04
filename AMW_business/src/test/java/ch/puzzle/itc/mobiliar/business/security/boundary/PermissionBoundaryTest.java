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

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionRepository;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.control.RestrictionRepository;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.persistence.NoResultException;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.CREATE;
import static ch.puzzle.itc.mobiliar.business.security.entity.Action.READ;
import static ch.puzzle.itc.mobiliar.business.security.entity.Action.UPDATE;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
    ResourceGroupRepository resourceGroupRepository;
    @Mock
    PermissionService permissionService;

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
        resourceGroupRepository = Mockito.mock(ResourceGroupRepository.class);
        permissionBoundary.resourceGroupRepository = resourceGroupRepository;
        permissionService = Mockito.mock(PermissionService.class);
        permissionBoundary.permissionService = permissionService;
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
        verify(permissionRepository, times(1)).setReloadDeployableRoleList(true);
        verify(permissionRepository, times(1)).setReloadRolesAndPermissionsList(true);
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
        verify(permissionRepository, times(1)).setReloadDeployableRoleList(true);
        verify(permissionRepository, times(1)).setReloadRolesAndPermissionsList(true);
    }

    @Test(expected=AMWException.class)
    public void shouldThrowAMWExceptionOnCreateIfResourceIdCanNotBeFound() throws AMWException {
        // given
        when(permissionRepository.getRoleByName("existing")).thenReturn(new RoleEntity());
        when(permissionRepository.getPermissionByName("good")).thenReturn(new PermissionEntity());
        when(resourceGroupRepository.find(7)).thenReturn(null);
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
        verify(permissionRepository, times(1)).setReloadDeployableRoleList(true);
        verify(permissionRepository, times(1)).setReloadRolesAndPermissionsList(true);

    }

    @Test
    public void shouldInvokePermissionServiceIfPermissionHasBeenFound() {
        // given
        when(permissionService.hasPermission(Permission.RESOURCE)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermission("RESOURCE");
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE);
        assertTrue(result);
    }

    @Test
    public void shouldDelegatePermissionCheckToPermissionService() {
        // given
        when(permissionService.hasPermission(Permission.RESOURCE)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermission(Permission.RESOURCE);
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE);
        assertTrue(result);
    }

    @Test
    public void shouldInvokePermissionServiceIfPermissionAndActionHaveBeenFound() {
        // given
        when(permissionService.hasPermission(Permission.RESOURCE, READ)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermission("RESOURCE", "READ");
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, READ);
        assertTrue(result);
    }

    @Test
    public void shouldDelegatePermissionAndActionCheckToPermissionService() {
        // given
        when(permissionService.hasPermission(Permission.RESOURCE, READ)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermission(Permission.RESOURCE, READ);
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, READ);
        assertTrue(result);
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowAnExceptionIfRequestedPermissionDoesNotExist() {
        // given // when // then
        boolean result = permissionBoundary.hasPermissionForResourceType("NotAPermission", "CREATE", "APP");
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowAnExceptionIfRequestedActionDoesNotExist() {
        // given // when // then
        permissionBoundary.hasPermissionForResourceType("RESOURCE", "NADA", "APP");
    }

    @Test
    public void shouldReturnFalseIfRequestedResourceTypeDoesNotExist() {
        // given // when
        boolean result = permissionBoundary.hasPermissionForResourceType("RESOURCE", "CREATE", "DAB");
        // then
        assertFalse(result);
    }

    @Test
    public void shouldInvokeResourceTypeRepository() {
        // given // when
        permissionBoundary.hasPermissionForResourceType("RESOURCE", "CREATE", "APP");
        // then
        verify(resourceTypeRepository, times(1)).getByName("APP");
    }

    @Test
    public void shouldInvokePermissionServiceIfResourceTypeHasBeenFound() {
        // given
        ResourceTypeEntity resType = new ResourceTypeEntity();
        when(resourceTypeRepository.getByName("APP")).thenReturn(resType);
        when(permissionService.hasPermission(Permission.RESOURCE, CREATE, resType)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermissionForResourceType("RESOURCE", "CREATE", "APP");
        // then
        verify(resourceTypeRepository, times(1)).getByName("APP");
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, CREATE, resType);
        assertTrue(result);
    }

    @Test
    public void shouldInvokePermissionServiceIfResourceTypeAndContextHaveBeenFound() {
        // given
        ResourceTypeEntity resType = new ResourceTypeEntity();
        ContextEntity context = new ContextEntity();
        when(resourceTypeRepository.getByName("APP")).thenReturn(resType);
        when(contextLocator.getContextById(1)).thenReturn(context);
        when(permissionService.hasPermission(Permission.RESOURCE, context, CREATE, null, resType)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermissionForResourceType("RESOURCE", "CREATE", "APP", 1);
        // then
        verify(resourceTypeRepository, times(1)).getByName("APP");
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, context, CREATE, null, resType);
        assertTrue(result);
    }

    @Test
    public void shouldInvokePermissionServiceWithAllParams() {
        // given
        ResourceEntity resource = new ResourceEntityBuilder().build();
        ResourceGroupEntity rg = new ResourceGroupEntity();
        resource.setResourceGroup(rg);
        ResourceTypeEntity type = new ResourceTypeEntity();
        resource.setResourceType(type);
        ContextEntity context = new ContextEntity();
        when(permissionService.hasPermission(Permission.RESOURCE, context, CREATE, rg, type)).thenReturn(true);
        // when
        boolean result = permissionBoundary.hasPermission(Permission.RESOURCE, context, CREATE, resource, type);
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, context, CREATE, rg, type);
        assertTrue(result);
    }

    @Test
    public void shouldInvokeTheRightMethodOnPermissionServiceToCheckIfHasPermissionToRemoveInstanceOfResType() {
        // given
        ResourceTypeEntity resType = new ResourceTypeEntity();
        // when
        permissionBoundary.hasPermissionToRemoveInstanceOfResType(resType);
        // then
        verify(permissionService, times(1)).hasPermissionToRemoveInstanceOfResType(resType);
    }

    @Test
    public void shouldInvokePermissionServiceWithCorrectParametersOnCanCopyFromResource() {
        // given
        ResourceEntity resource = new ResourceEntityBuilder().build();
        ResourceGroupEntity rg = new ResourceGroupEntity();
        resource.setResourceGroup(rg);
        ResourceTypeEntity type = new ResourceTypeEntity();
        resource.setResourceType(type);
        // when
        permissionBoundary.canCopyFromResource(resource);
        // then
        verify(permissionService, times(1)).hasPermission(Permission.COPY_FROM_RESOURCE, null, UPDATE, rg, type);
    }

    @Test
    public void shouldInvokeTheRightMethodOnPermissionServiceToCheckIfCanCreateResourceInstance() {
        // given
        ResourceTypeEntity resType = Mockito.mock(ResourceTypeEntity.class);
        // when
        permissionBoundary.canCreateResourceInstance(resType);
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, CREATE, resType);
    }

    @Test
    public void shouldInvokeTheRightMethodsOnPermissionServiceToCheckIfCanCreateAppAndAddToAppServer() {
        // given
        ResourceEntity resource = new ResourceEntityBuilder().build();
        ResourceTypeEntity type = new ResourceTypeEntity();
        resource.setResourceType(type);
        when(permissionService.hasPermission(Permission.RESOURCE, CREATE, type)).thenReturn(true);
        // when
        permissionBoundary.canCreateAppAndAddToAppServer(resource);
        // then
        verify(permissionService, times(1)).hasPermission(Permission.RESOURCE, CREATE, type);
        verify(permissionService, times(1)).hasPermission(Permission.ADD_APP_TO_APP_SERVER);
    }

    @Test
    public void shouldObtainListOfPermissionsFromPermissionService() {
        // given // when
        permissionBoundary.getAllPermissions();
        // then
        verify(permissionService, times(1)).getPermissions();
    }

}
