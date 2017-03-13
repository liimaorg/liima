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

package ch.mobi.itc.mobiliar.rest.permissions;

import ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.ALL;
import static ch.puzzle.itc.mobiliar.business.security.entity.Permission.ADD_APP;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RestrictionTest {

    @InjectMocks
    RestrictionsRest rest;
    @Mock
    PermissionBoundary permissionBoundary;
    @Mock
    ContextLocator contextLocator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnState404IfRestrictionHasNotBeenFound() {
        // given
        Integer restrictionId = 1;
        when(rest.permissionBoundary.findRestriction(restrictionId)).thenReturn(null);

        // when
        Response response = rest.getRestriction(restrictionId);

        // then
        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnStateARestrictionDTOIfRestrictionHasBeenFound() {
        // given
        Integer restrictionId = 7;
        PermissionEntity permission = new PermissionEntity();
        permission.setValue(ADD_APP.name());
        ContextEntity context = new ContextEntity();
        context.setName("testContext");
        RoleEntity role = new RoleEntity();
        role.setName("testRole");
        RestrictionEntity restriction = new RestrictionEntity();
        restriction.setAction(ALL);
        restriction.setPermission(permission);
        restriction.setContext(context);
        restriction.setRole(role);
        when(rest.permissionBoundary.findRestriction(restrictionId)).thenReturn(restriction);

        // when
        Response response = rest.getRestriction(restrictionId);

        // then
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals("RestrictionDTO", response.getEntity().getClass().getSimpleName());
    }

    @Test
    public void shouldReturnStateBadRequestIfIdIsSet() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(1, null, null, null, null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfRoleNameIsMissing() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, null, null, null, null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfRoleNameIsInvalid() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "invalid", null, null, null);
        when(rest.permissionBoundary.getRoleByName("invalid")).thenReturn(null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfPermissionNameIsMissing() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "valid", null, null, null);
        when(rest.permissionBoundary.getRoleByName("valid")).thenReturn(new RoleEntity());

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfPermissionNameIsInvalid() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "valid", ADD_APP, null, null);
        when(rest.permissionBoundary.getRoleByName("valid")).thenReturn(new RoleEntity());
        when(rest.permissionBoundary.getPermissionByName(ADD_APP.name())).thenReturn(null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfContextNameIsInvalid() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "valid", ADD_APP, "invalid", null);
        when(rest.permissionBoundary.getRoleByName("valid")).thenReturn(new RoleEntity());
        when(rest.permissionBoundary.getPermissionByName(ADD_APP.name())).thenReturn(new PermissionEntity());
        when(rest.contextLocator.getContextByName("invalid")).thenReturn(null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateCreatedIfContextNameAndActionAreNull() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "valid", ADD_APP, null, null);
        when(rest.permissionBoundary.getRoleByName("valid")).thenReturn(new RoleEntity());
        when(rest.permissionBoundary.getPermissionByName(ADD_APP.name())).thenReturn(new PermissionEntity());
        when(rest.permissionBoundary.createRestriction(any(RoleEntity.class), any(PermissionEntity.class),
                any(ContextEntity.class), any(Action.class))).thenReturn(anyInt());

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(CREATED.getStatusCode(), response.getStatus());
    }

}
