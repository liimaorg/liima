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
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.*;

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
    @ApiOperation(value = "Add a Restriction")
    public Response addRestriction(@ApiParam("Add a Restriction") RestrictionDTO request) {
        Integer id;
        if (request.getId() != null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Id must be null")).build();
        }
        try {
            id = permissionBoundary.createRestriction(request.getRoleName(), request.getPermission().name(), request.getResourceGroupId(),
                    request.getResourceTypeName(), request.getContextName(), request.getAction());
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        return Response.status(CREATED).header("Location", "/permissions/restrictions/" + id).build();
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
    @ApiOperation(value = "Get Restriction by id")
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
    @ApiOperation(value = "Get all Restrictions")
    public Response getAllRestriction() {
        List<RestrictionDTO> restrictions = new ArrayList<>();
        for (RestrictionEntity restrictionEntity : permissionBoundary.findAllRestrictions()) {
            restrictions.add(new RestrictionDTO(restrictionEntity));
        }
        return Response.status(OK).entity(restrictions).build();
    }

    /**
     * Update a Restriction
     * @param id
     */
    @PUT
    @Path("/{id : \\d+}")
    // support digit only
    @ApiOperation(value = "Update a Restriction")
    public Response updateRestriction(@ApiParam("Restriction ID") @PathParam("id") Integer id, RestrictionDTO request) {
        try {
            permissionBoundary.updateRestriction(id, request.getRoleName(), request.getPermission().name(),
                    request.getResourceGroupId(), request.getResourceTypeName(), request.getContextName(), request.getAction());
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        return Response.status(OK).build();
    }

    /**
     * Remove a Restriction
     * @param id
     */
    @DELETE
    @Path("/{id : \\d+}")
    // support digit only
    @ApiOperation(value = "Remove a Restriction")
    public Response deleteRestriction(@ApiParam("Restriction ID") @PathParam("id") Integer id) {
        try {
            permissionBoundary.removeRestriction(id);
        } catch (AMWException e) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto(e.getMessage())).build();
        }
        return Response.status(NO_CONTENT).build();
    }

    /**
     * Find all Roles with their permissions/restrictions
     *
     * @return Map<RoleName, List<RestrictionDTO>>
     */
    @GET
    @Path("/roles/")
    @ApiOperation(value = "Get all Roles with restrictions")
    public Response getAllRoles() {
        Map<String, List<RestrictionDTO>> rolesMap = new HashMap<>();
        final Map<String, List<ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO>> allRoles = permissionBoundary.getAllPermissions();
        // converting business RestrictionDTOs to rest RestrictionDTOs
        for (Map.Entry<String, List<ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO>> roleRestrictionList : allRoles.entrySet()) {
            String roleName = roleRestrictionList.getKey();
            if (!rolesMap.containsKey(roleName)) {
                rolesMap.put(roleName, new ArrayList<RestrictionDTO>());
            }
            for (ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO restrictionDTO : roleRestrictionList.getValue()) {
                rolesMap.get(roleName).add(new RestrictionDTO(restrictionDTO.getRestriction()));
            }
        }
        return Response.status(OK).entity(rolesMap).build();
    }

}
