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

package ch.puzzle.itc.mobiliar.business.security.interceptor;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.test.TestBoundary;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.interceptor.InvocationContext;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class HasPermissionInterceptorTest {

    @Mock
    InvocationContext context;

    @Mock
    PermissionService permissionService;

    @InjectMocks
    HasPermissionInterceptor hasPermissionInterceptor;

    @Before
    public void init() {
        initMocks(this);
        permissionService = mock(PermissionService.class);
        hasPermissionInterceptor.permissionService = permissionService;
        when(context.getTarget()).thenReturn(new TestBoundary());
    }

    @Test
    public void shouldNotCallPermissionService() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("noPermissionNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, never()).hasPermission(any(Permission.class),
                any(ContextEntity.class), any(Action.class), any(ResourceGroupEntity.class), any(ResourceTypeEntity.class));
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermission() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermission(Permission.DEPLOYMENT, null, null ,null, null);
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionAndAction() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermission(Permission.DEPLOYMENT, null, Action.CREATE, null, null);
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionAndEveryAnnotatedActions() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateOrUpdateNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(2)).hasPermission(any(Permission.class),
                any(ContextEntity.class), any(Action.class), any(ResourceGroupEntity.class), any(ResourceTypeEntity.class));
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionAndActionButSkipAsSoonAsACheckReturnsTrue() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateOrUpdateNeeded"));
        when(hasPermissionInterceptor.permissionService.hasPermission(Permission.DEPLOYMENT, null, Action.CREATE, null, null)).thenReturn(true);
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermission(Permission.DEPLOYMENT, null, Action.CREATE, null, null);
        verify(hasPermissionInterceptor.permissionService, never()).hasPermission(Permission.DEPLOYMENT, null, Action.UPDATE, null, null);
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithEveryAnnotatedPermissionAndEveryAnnotatedActions() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployOrCopyFromPermissionActionCreateOrUpdateNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(4)).hasPermission(any(Permission.class),
                any(ContextEntity.class), any(Action.class), any(ResourceGroupEntity.class), any(ResourceTypeEntity.class));
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithMultipleAnnotatedPermissionsAndActionsButSkipAsSoonACheckReturnsTrue() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployOrCopyFromPermissionActionCreateOrUpdateNeeded"));
        when(hasPermissionInterceptor.permissionService.hasPermission(Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE, null, Action.UPDATE, null, null)).thenReturn(true);
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermission(Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE, null, Action.CREATE, null, null);
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermission(Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE, null, Action.UPDATE, null, null);
        verify(hasPermissionInterceptor.permissionService, never()).hasPermission(Permission.DEPLOYMENT, null, Action.CREATE, null, null);
        verify(hasPermissionInterceptor.permissionService, never()).hasPermission(Permission.DEPLOYMENT, null, Action.UPDATE, null, null);
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionsAndResource() throws Exception {
        //given
        ResourceEntity resource = new ResourceEntity();
        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resource.setResourceGroup(resGroup);
        ResourceEntity[] resources = {resource};
        context.setParameters(resources);
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateForSpecificResourceNeeded"));
        when(context.getParameters()).thenReturn(resources);
        when(hasPermissionInterceptor.permissionService.hasPermission(Permission.DEPLOYMENT, null, Action.CREATE, resGroup, null)).thenReturn(true);
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermission(Permission.DEPLOYMENT, null, Action.CREATE, resGroup, null);
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

}
