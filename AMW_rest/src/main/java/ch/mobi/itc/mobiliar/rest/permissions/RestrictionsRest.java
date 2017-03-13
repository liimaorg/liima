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
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/permissions/restrictions")
@Api(value = "/permissions/restrictions", description = "Managing restrictions")
public class RestrictionsRest {

    @Inject
    PermissionBoundary permissionBoundary;
    @Inject
    ContextLocator contextLocator;

    /**
     * Creates a new restriction and returns the newly created restriction.
     *
     * @param request containing a RestrictionDTO
     * @return the new RestrictionDTO
     **/
    @POST
    @ApiOperation(value = "adds a Restriction")
    public Response addRestriction(@ApiParam("Add a Restriction") RestrictionDTO request) {
        RoleEntity roleEntity;
        PermissionEntity permissionEntity;
        ContextEntity contextEntity;
        Action action;

        if (request.getRoleName() != null) {
            roleEntity = permissionBoundary.getRoleByName(request.getRoleName());
            if (roleEntity == null) {
                return Response.status(BAD_REQUEST).entity(new ExceptionDto("Role " + request
                        .getRoleName() + " not found.")).build();
            }
        } else {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Missing RoleName")).build();
        }

        if (request.getPermission() != null) {
            permissionEntity = permissionBoundary.getPermissionByName(request.getPermission().name());
            if (permissionEntity == null) {
                return Response.status(BAD_REQUEST).entity(new ExceptionDto("Permission " + request
                        .getPermission() + " not found.")).build();
            }
        } else {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Missing PermissionName")).build();
        }

        if (request.getContextName() != null) {
            contextEntity = contextLocator.getContextByName(request.getContextName());
            if (contextEntity == null) {
                return Response.status(BAD_REQUEST).entity(new ExceptionDto("Context " + request
                        .getContextName() + " not found.")).build();
            }
        } else {
            contextEntity = null;
        }

        if (request.getAction() != null) {
            action = request.getAction();
        } else {
            action = Action.ALL;
        }

        Integer id = permissionBoundary.createRestriction(roleEntity, permissionEntity, contextEntity, action);
        return Response.status(Response.Status.CREATED).header("Location", "/permissions/restrictions/" + id).build();

    }

    /**
     * Find a Restriction by its id
     *
     * @param id
     * @return RestrictionDTO
     */
    @GET
    @Path("/{id : \\d+}")
    // support digit only
    @ApiOperation(value = "get Restriction by id")
    public Response getRestriction(@ApiParam("Restriction ID") @PathParam("id") Integer id) {
        RestrictionEntity restriction = permissionBoundary.findRestriction(id);
        if (restriction == null) {
            return Response.status(NOT_FOUND).build();
        }
        return Response.status(OK).entity(new RestrictionDTO(restriction)).build();
    }

    /**
     * Find all Restrictions
     *
     * @return List<RestrictionDTO>
     */
    @GET
    @Path("/")
    @ApiOperation(value = "get all Restrictions")
    public Response getAllRestriction() {
        List<RestrictionDTO> restrictions = new ArrayList<>();
        for (RestrictionEntity restrictionEntity : permissionBoundary.findAll()) {
            restrictions.add(new RestrictionDTO(restrictionEntity));
        }
        return Response.status(OK).entity(restrictions).build();
    }

}
