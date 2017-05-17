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
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import java.util.*;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.ALL;
import static ch.puzzle.itc.mobiliar.business.security.entity.Permission.RESOURCE;
import static java.util.Collections.EMPTY_LIST;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RestrictionTest {

    @InjectMocks
    RestrictionsRest rest;
    @Mock
    PermissionBoundary permissionBoundary;

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
    public void shouldReturnARestrictionDTOIfRestrictionHasBeenFound() {
        // given
        Integer restrictionId = 7;
        PermissionEntity permission = new PermissionEntity();
        permission.setValue(RESOURCE.name());
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
    public void shouldReturnStateOKIfNoRestrictionsHaveBeenFound() {
        // given
        when(rest.permissionBoundary.findAllRestrictions()).thenReturn(EMPTY_LIST);

        // when
        Response response = rest.getAllRestriction();

        // then
        assertEquals(OK.getStatusCode(), response.getStatus());
        List<RestrictionDTO> restrictions = (ArrayList<RestrictionDTO>) response.getEntity();
        assertEquals(0, restrictions.size());
    }

    @Test
    public void shouldReturnListOfRestrictionDTOsIfRestrictionsHaveBeenFound() {
        // given
        PermissionEntity permission = new PermissionEntity();
        permission.setValue(RESOURCE.name());
        ContextEntity context = new ContextEntity();
        context.setName("testContext");
        RoleEntity role = new RoleEntity();
        role.setName("testRole");
        RestrictionEntity restriction = new RestrictionEntity();
        restriction.setAction(ALL);
        restriction.setPermission(permission);
        restriction.setContext(context);
        restriction.setRole(role);

        when(rest.permissionBoundary.findAllRestrictions()).thenReturn(Arrays.asList(restriction));

        // when
        Response response = rest.getAllRestriction();

        // then
        assertEquals(OK.getStatusCode(), response.getStatus());
        List<RestrictionDTO> restrictions = (ArrayList<RestrictionDTO>) response.getEntity();
        assertEquals("testRole", restrictions.get(0).getRoleName());
    }


    @Test
    public void shouldReturnStateBadRequestIfIdIsSet() {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(1, null, null, RESOURCE, null, null, null, null, null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfRoleNameAndUserNameAreMissing() throws AMWException {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, null, null, RESOURCE, null, null, null, null, null);
        when(rest.permissionBoundary.createRestriction(null, null, RESOURCE.name(), null, null, null, null, null)).thenThrow(new AMWException("bad"));

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfRoleNameIsInvalid() throws AMWException {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "invalid", null, RESOURCE, null, null, null, null, null);
        when(rest.permissionBoundary.createRestriction("invalid", null, RESOURCE.name(), null, null, null, null, null)).thenThrow(new AMWException("bad"));

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldSucceedIfRoleNameIsMissingButUserNameIsProvided() throws AMWException {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "fritz", null, RESOURCE, null, null, null, null, null);

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(CREATED.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfResourceIdIsInvalid() throws AMWException {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "valid", null, RESOURCE, 1, null, null, null, null);
        when(rest.permissionBoundary.createRestriction("valid", null, RESOURCE.name(), 1, null, null, null, null)).thenThrow(new AMWException("bad"));

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateBadRequestIfResourceTypeNameIsInvalid() throws AMWException {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(null, "valid", null, RESOURCE, null, "invalid", null, null, null);
        when(rest.permissionBoundary.createRestriction("valid", null, RESOURCE.name(), null, "invalid", null, null, null)).thenThrow(new AMWException("bad"));

        // when
        Response response = rest.addRestriction(restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }


    @Test
    public void shouldReturnStateBadRequestIfUpdateRestrictionFails() throws AMWException {
        // given
        ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO restrictionDTO = new RestrictionDTO(1, "valid", null, RESOURCE, 8, null, null, null, null);
        doThrow(new AMWException("bad")).when(rest.permissionBoundary).updateRestriction(1, "valid", null, RESOURCE.name(), 8, null, null, null, null);

        // when
        Response response = rest.updateRestriction(1, restrictionDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateNotFoundIfRestrictionToBeDeletedWasNotFound() throws AMWException {
        // given
        doThrow(new AMWException("bad")).when(rest.permissionBoundary).removeRestriction(1);

        // when
        Response response = rest.deleteRestriction(1);

        // then
        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnStateNoContentIfRestrictionHasBeenDeletedSuccessfully() throws AMWException {
        // given // when
        Response response = rest.deleteRestriction(1);

        // then
        assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());;
    }

    @Test
    public void shouldReturnAMapOfListOfRestrictionDTOsIfRolesHaveBeenFound() {
        // given
        PermissionEntity permission = new PermissionEntity();
        permission.setValue(RESOURCE.name());
        RoleEntity role = new RoleEntity();
        role.setName("testRole");
        RoleEntity anotherRole = new RoleEntity();
        anotherRole.setName("anotherTestRole");
        ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO businessRestrictionDTO =
                new ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO(permission, role);
        Map<String, List<ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO>> map = new HashMap<>();
        map.put(role.getName(), Arrays.asList(businessRestrictionDTO));
        businessRestrictionDTO = new ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO(permission, anotherRole);
        map.put(anotherRole.getName(), Arrays.asList(businessRestrictionDTO));
        when(rest.permissionBoundary.getAllPermissions()).thenReturn(map);

        // when
        Response response = rest.getAllRoles();

        // then
        assertEquals(OK.getStatusCode(), response.getStatus());
        Map<String, List<RestrictionDTO>> restrictions = (Map<String, List<RestrictionDTO>>) response.getEntity();
        assertEquals(role.getName(), restrictions.get(role.getName()).get(0).getRoleName());
        assertEquals(RESOURCE.name(), restrictions.get(role.getName()).get(0).getPermission().getName());
        assertEquals(anotherRole.getName(), restrictions.get(anotherRole.getName()).get(0).getRoleName());
    }

}
