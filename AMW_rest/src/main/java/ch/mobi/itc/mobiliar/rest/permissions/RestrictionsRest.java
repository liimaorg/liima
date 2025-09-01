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

import ch.mobi.itc.mobiliar.rest.dtos.PermissionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.RestrictionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.RestrictionsCreationDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "/permissions/restrictions", description = "Managing permissions")
public class RestrictionsRest {

    @Inject
    PermissionBoundary permissionBoundary;

    /**
     * Creates a new restriction and returns the newly created restriction.
     *
     * @param request containing a RestrictionDTO
     * @return the new RestrictionDTO
     **/
    @POST
    @Operation(summary = "Add a Restriction")
    public Response addRestriction(@Parameter(description = "Add a Restriction, either a role- or a userName must be set") RestrictionDTO request,
                                   @QueryParam("delegation") boolean delegation, @DefaultValue("true") @QueryParam("reload") boolean reload) {
        Integer id;
        if (request.getId() != null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Id must be null")).build();
        }
        if (request.getPermission() == null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Permission must not be null")).build();
        }
        try {
            id = permissionBoundary.createRestriction(request.getRoleName(), request.getUserName(), request.getPermission().getName(), request.getResourceGroupId(),
                    request.getResourceTypeName(), request.getResourceTypePermission(), request.getContextName(), request.getAction(), delegation, reload);
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        if (id == null) {
            return Response.status(PRECONDITION_FAILED).entity(new ExceptionDto("A similar permission already exists")).build();
        }
        return Response.status(CREATED).header("Location", "/permissions/restrictions/" + id).build();
    }

    /**
     * Creates new restrictions
     *
     * @param request containing a RestrictionsCreationDTO
     * @return the total count of created restrictions in the header
     */
    @POST
    @Path("/multi/")
    @Operation(summary = "Add a multiple Restrictions")
    public Response addRestriction(@Parameter(description = "Add multiple Restrictions, either a role- or one or more userNames must be set") RestrictionsCreationDTO request,
                                   @QueryParam("delegation") boolean delegation, @DefaultValue("true") @QueryParam("reload") boolean reload) {
        if (request.getPermissionNames().isEmpty()) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("At least one Permission is required")).build();
        }
        int count;
        try {
            count = permissionBoundary.createMultipleRestrictions(request.getRoleName(), request.getUserNames(), request.getPermissionNames(), request.getResourceGroupIds(),
                    request.getResourceTypeNames(), request.getResourceTypePermission(), request.getContextNames(), request.getActions(), delegation, reload);
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        return Response.status(CREATED).header("X-Total-Count", count).build();
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
    @Operation(summary = "Get Restriction by id")
    public Response getRestriction(@Parameter(description = "Restriction ID") @PathParam("id") Integer id) {
        final RestrictionEntity restriction = permissionBoundary.findRestriction(id);
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
    @Operation(summary = "Get all Restrictions")
    public Response getAllRestriction() {
        return restrictionsToResponse(permissionBoundary.findAllRestrictions());
    }

    /**
     * Update a Restriction
     * @param id
     */
    @PUT
    @Path("/{id : \\d+}")
    // support digit only
    @Produces("application/json")
    @Operation(summary = "Update a Restriction")
    public Response updateRestriction(@Parameter(description = "Restriction ID") @PathParam("id") Integer id, RestrictionDTO request,
                                      @DefaultValue("true") @QueryParam("reload") boolean reload) {
        boolean success;
        try {
            success = permissionBoundary.updateRestriction(id, request.getRoleName(), request.getUserName(), request.getPermission().getName(),
                    request.getResourceGroupId(), request.getResourceTypeName(), request.getResourceTypePermission(),
                    request.getContextName(), request.getAction(), reload);
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        if (!success) {
            return Response.status(PRECONDITION_FAILED).entity(new ExceptionDto("A similar permission already exists")).build();
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
    @Operation(summary = "Remove a Restriction")
    public Response deleteRestriction(@Parameter(description = "Restriction ID") @PathParam("id") Integer id,
                                      @DefaultValue("true") @QueryParam("reload") boolean reload) {
        try {
            permissionBoundary.removeRestriction(id, reload);
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
    @Operation(summary = "Get all Roles with restrictions")
    public Response getAllRoles() {
        final Map<String, List<ch.puzzle.itc.mobiliar.business.security.entity.RestrictionDTO>> allRoles = permissionBoundary.getAllPermissions();
        Map<String, List<RestrictionDTO>> rolesMap = new HashMap<>(allRoles.size());
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

    /**
     * Find a specific Role with its Restrictions identified by RoleName
     *
     * @return List<RestrictionDTO>
     */
    @GET
    @Path("/roles/{roleName}")
    @Operation(summary = "Get all Restrictions assigned to a specific Role")
    public Response getRoleRestriction(@Parameter(description = "RoleName") @PathParam("roleName") String roleName) {
        return restrictionsToResponse(permissionBoundary.getRestrictionsByRoleName(roleName));
    }

    /**
     * Removes a role with all it's permissions
     * @param id
     */
    @DELETE
    @Path("/roles/{roleName}")
    @Operation(summary = "Removes a role with all it's permissions")
    public Response deleteRole(@Parameter(description = "RoleName") @PathParam("roleName") String roleName, @DefaultValue("true") @QueryParam("reload") boolean reload) {
        permissionBoundary.deleteRole(roleName, reload);
        return Response.status(OK).build();
    }

    /**
     * Get all available RoleNames
     *
     * @return List<String>
     */
    @GET
    @Path("/roleNames/")
    @Operation(summary = "Get all available RoleNames")
    public Response getRoleNames() {
        final List<RoleEntity> roles = permissionBoundary.getAllRoles();
        List<String> roleNameList = new ArrayList<>(roles.size());
        for (RoleEntity role : roles) {
            roleNameList.add(role.getName());
        }
        return Response.status(OK).entity(roleNameList).build();
    }

    /**
     * Get all available UserRestrictionNames
     *
     * @return List<String>
     */
    @GET
    @Path("/userRestrictionNames/")
    @Operation(summary = "Get all available userRestrictionNames")
    public Response getUserNames() {
        return Response.status(OK).entity(permissionBoundary.getAllUserRestrictionNames()).build();
    }

    /**
     * Get all available PermissionEnum values
     *
     * @return List<PermissionDTO>
     */
    @GET
    @Path("/permissionEnumValues/")
    @Operation(summary = "Get all available PermissionEnum values")
    public Response getPermissionEnumValues() {
        final List<PermissionEntity> permissions = permissionBoundary.getAllAvailablePermissions();
        List<PermissionDTO> permissionNameList = new ArrayList<>(permissions.size());
        for (PermissionEntity permission : permissions) {
            permissionNameList.add(new PermissionDTO(Permission.valueOf(permission.getValue())));
        }
        return Response.status(OK).entity(permissionNameList).build();
    }

    /**
     * Find all UserRestrictions with their Restrictions
     *
     * @return Map<UserRestriction, List<RestrictionDTO>>
     */
    @GET
    @Path("/users/")
    @Operation(summary = "Get all UserRestriction with their Restrictions")
    public Response getAllUsers() {
        final List<RestrictionEntity> allUserRestrictions = permissionBoundary.getAllUserRestriction();
        Map<String, List<RestrictionDTO>> usersMap = new HashMap<>(allUserRestrictions.size());
        for (RestrictionEntity restriction : allUserRestrictions) {
            String userName = restriction.getUser().getName();
            if (!usersMap.containsKey(userName)) {
                usersMap.put(userName, new ArrayList<RestrictionDTO>());
            }
            usersMap.get(userName).add(new RestrictionDTO(restriction));
        }
        return Response.status(OK).entity(usersMap).build();
    }

    /**
     * Find a specific UserRestriction with its Restrictions identified by UserName
     *
     * @param userName
     * @return List<RestrictionDTO>
     */
    @GET
    @Path("/users/{userName}")
    @Operation(summary = "Get all Restrictions assigned to a specific UserRestriction")
    public Response getUserRestriction(@Parameter(description = "UserName") @PathParam("userName") String userName) {
        return restrictionsToResponse(permissionBoundary.getRestrictionsByUserName(userName));
    }

    /**
     * Find all Restrictions of the calling user
     *
     * @return List<RestrictionDTO>
     */
    @GET
    @Path("/ownRestrictions/")
    @Operation(summary = "Get all Restrictions assigned to a specific UserRestriction")
    public Response getCallerRestrictions() {
        return restrictionsToResponse(permissionBoundary.getAllCallerRestrictions());
    }

    private Response restrictionsToResponse(List<RestrictionEntity> restrictions) {
        List<RestrictionDTO> restrictionList = new ArrayList<>(restrictions.size());
        for (RestrictionEntity restriction : restrictions) {
            restrictionList.add(new RestrictionDTO(restriction));
        }
        return Response.status(OK).entity(restrictionList).build();
    }

    /**
     * Reload the permission cache
     */
    @POST
    @Path("/reload")
    @Operation(summary = "Reload the permission cache")
    public Response reloadCache() {
        permissionBoundary.reloadCache();
        return Response.status(OK).build();
    }

}
