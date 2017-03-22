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
        verify(hasPermissionInterceptor.permissionService, never()).hasPermissionAndAction(any(Permission.class), any(Action.class));
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermission() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermissionAndAction(Permission.DEPLOYMENT, null);
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionAndAction() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermissionAndAction(Permission.DEPLOYMENT, Action.CREATE);
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionAndEveryAnnotatedActions() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateOrUpdateNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(2)).hasPermissionAndAction(any(Permission.class), any(Action.class));
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithPermissionAndActionButSkipAsSoonAsACheckReturnsTrue() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployPermissionActionCreateOrUpdateNeeded"));
        when(hasPermissionInterceptor.permissionService.hasPermissionAndAction(Permission.DEPLOYMENT, Action.CREATE)).thenReturn(true);
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermissionAndAction(Permission.DEPLOYMENT, Action.CREATE);
        verify(hasPermissionInterceptor.permissionService, never()).hasPermissionAndAction(Permission.DEPLOYMENT, Action.UPDATE);
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithEveryAnnotatedPermissionAndEveryAnnotatedActions() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployOrCopyFromPermissionActionCreateOrUpdateNeeded"));
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(4)).hasPermissionAndAction(any(Permission.class), any(Action.class));
        verify(hasPermissionInterceptor.permissionService, times(1)).throwNotAuthorizedException(null);
    }

    @Test
    public void shouldCallPermissionServiceWithMultipleAnnotatedPermissionsAndActionsButSkipAsSoonACheckReturnsTrue() throws Exception {
        //given
        when(context.getMethod()).thenReturn(TestBoundary.class.getMethod("deployOrCopyFromPermissionActionCreateOrUpdateNeeded"));
        when(hasPermissionInterceptor.permissionService.hasPermissionAndAction(Permission.COPY_FROM_RESOURCE, Action.UPDATE)).thenReturn(true);
        //when
        hasPermissionInterceptor.roleCall(context);
        //then
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermissionAndAction(Permission.COPY_FROM_RESOURCE, Action.CREATE);
        verify(hasPermissionInterceptor.permissionService, times(1)).hasPermissionAndAction(Permission.COPY_FROM_RESOURCE, Action.UPDATE);
        verify(hasPermissionInterceptor.permissionService, never()).hasPermissionAndAction(Permission.DEPLOYMENT, Action.CREATE);
        verify(hasPermissionInterceptor.permissionService, never()).hasPermissionAndAction(Permission.DEPLOYMENT, Action.UPDATE);
        verify(hasPermissionInterceptor.permissionService, never()).throwNotAuthorizedException(null);
    }

}
