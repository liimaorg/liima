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

package ch.puzzle.itc.mobiliar.business.security.control;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import ch.puzzle.itc.mobiliar.common.exception.CheckedNotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.apache.commons.lang.StringUtils;

import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.ALL;

@Stateless
public class PermissionService implements Serializable {

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    Logger log;

    @Inject
    SessionContext sessionContext;

    static Map<String, List<RestrictionDTO>> deployableRolesWithRestrictions;
    static Map<String, List<RestrictionDTO>> rolesWithRestrictions;

    Map<String, List<RestrictionDTO>> getDeployableRoles() {
        boolean isReload = permissionRepository.isReloadDeployableRoleList();
        if (deployableRolesWithRestrictions == null || isReload) {
            Map<String, List<RestrictionDTO>> tmpDeployableRolesWithRestrictions = new HashMap<>();
            // get roles (with restrictions with deployment permission)
            for (RoleEntity role : permissionRepository.getDeployableRoles()) {
                addPermission(tmpDeployableRolesWithRestrictions, role);
            }
            deployableRolesWithRestrictions = Collections.unmodifiableMap(tmpDeployableRolesWithRestrictions);
            if (isReload) {
                permissionRepository.setReloadDeployableRoleList(false);
            }
        }
        return deployableRolesWithRestrictions;
    }

    /**
     * @return the List of RoleEntity read from DB
     */
    public List<RoleEntity> getDeployableRolesNonCached() {
        return permissionRepository.getDeployableRoles();
    }

    /**
     * Diese Methode controlliert ob einen User Deployoperation darf machen oder nicht. Es wird im
     * deploy.xhtml aufgerufen und zeigt der Button "Add Deploy" wenn der user darf deploy machen.
     *
     * @return
     */
    public boolean hasPermissionToDeploy() {
        for (Map.Entry<String, List<RestrictionDTO>> entry : getDeployableRoles().entrySet()) {
            if (sessionContext.isCallerInRole(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all available roles with their restrictions
     * Legacy Permissions are mapped to the new Permission/Restriction model
     *
     * @return Map key=Role.name, value=restrictionDTOs
     */
    public Map<String, List<RestrictionDTO>> getPermissions() {
        boolean isReload = permissionRepository.isReloadRolesAndPermissionsList();
        if (rolesWithRestrictions == null || isReload) {
            Map<String, List<RestrictionDTO>> tmpRolesWithRestrictions = new HashMap<>();
            // map old permissions to new permissions with restriction
            if (permissionRepository.getRolesWithPermissions() != null) {
                for (RoleEntity role : permissionRepository.getRolesWithPermissions()) {
                    addLegacyPermission(tmpRolesWithRestrictions, role);
                }
            }
            // add new permissions with restriction
            if (permissionRepository.getRolesWithRestrictions() != null) {
                for (RoleEntity role : permissionRepository.getRolesWithRestrictions()) {
                    addPermission(tmpRolesWithRestrictions, role);
                }
            }
            //make immutable
            for (String roleName : tmpRolesWithRestrictions.keySet()) {
                List<RestrictionDTO> restrictions = tmpRolesWithRestrictions.get(roleName);
                tmpRolesWithRestrictions.put(roleName, Collections.unmodifiableList(restrictions));
            }
            rolesWithRestrictions = Collections.unmodifiableMap(tmpRolesWithRestrictions);

            if (isReload) {
                permissionRepository.setReloadRolesAndPermissionsList(false);
            }
        }
        return rolesWithRestrictions;
    }

    private void addLegacyPermission(Map<String, List<RestrictionDTO>> tmpRolesWithRestrictions, RoleEntity role) {
        String roleName = role.getName();
        if (!tmpRolesWithRestrictions.containsKey(roleName)) {
            tmpRolesWithRestrictions.put(roleName, new ArrayList<RestrictionDTO>());
        }
        for (PermissionEntity perm : role.getPermissions()) {
            // check needed as long as roles can have a direct relation to restriction and permission simultaneously
            if (perm.getRestrictions().isEmpty()) {
                // convert permission to restriction
                tmpRolesWithRestrictions.get(roleName).add(new RestrictionDTO(perm, role));
            }
        }
    }

    private void addPermission(Map<String, List<RestrictionDTO>> tmpRolesWithRestrictions, RoleEntity role) {
        String roleName = role.getName();
        if (!tmpRolesWithRestrictions.containsKey(roleName)) {
            tmpRolesWithRestrictions.put(roleName, new ArrayList<RestrictionDTO>());
        }
        for (RestrictionEntity res : role.getRestrictions()) {
            // add restriction
            tmpRolesWithRestrictions.get(roleName).add(new RestrictionDTO(res));
        }
    }

    public boolean hasPermission(Permission permission) {
        return hasRole(permission.name(), null, null, null);
    }

    public boolean hasPermissionAndAction(Permission permission, Action action) {
        return hasRole(permission.name(), null, action, null);
    }

    public boolean hasPermissionForContext(Permission permission, ContextEntity context) {
        return hasRole(permission.name(), context, null, null);
    }

    public boolean hasPermission(Permission permission, ContextEntity context, Action action) {
        return hasRole(permission.name(), context, action, null);
    }

    public boolean hasPermission(Permission permission, ContextEntity context, Action action, ResourceEntity resource) {
        return hasRole(permission.name(), context, action, resource);
    }

    /**
     * Checks if given permission is available. If not a exception is created with error message containing extraInfo part.
     *
     * @param permission
     * @param extraInfo
     */
    public void checkPermissionAndFireException(Permission permission, String extraInfo) {
        if (!hasPermission(permission)) {
            throwNotAuthorizedException(extraInfo);
        }
    }

    /**
     * Checks if given permission is available. If not a exception is created with error message containing extraInfo part.
     *
     * @param permission
     * @param extraInfo
     */
    public void checkPermissionAndFireCheckedException(Permission permission, String extraInfo) throws CheckedNotAuthorizedException {
        if (!hasPermission(permission)) {
            throwCheckedNotAuthorizedException(extraInfo);
        }
    }

    public void throwNotAuthorizedException(String extraInfo) {
        String errorMessage = "Not Authorized!";
        if (StringUtils.isNotEmpty(extraInfo)) {
            errorMessage += " You're not allowed to " + extraInfo + "!";
        }
        throw new NotAuthorizedException(errorMessage);
    }

    public void throwCheckedNotAuthorizedException(String extraInfo) throws CheckedNotAuthorizedException {
        String errorMessage = "Not Authorized!";
        if (StringUtils.isNotEmpty(extraInfo)) {
            errorMessage += " You're not allowed to " + extraInfo + "!";
        }
        throw new CheckedNotAuthorizedException(errorMessage);
    }

    public boolean hasPermissionForDeployment(DeploymentEntity deployment) {
        return deployment != null && hasPermissionForDeploymentOnContext(deployment.getContext(), deployment.getResource());
    }

    public boolean hasPermissionForCancelDeployment(DeploymentEntity deployment) {
        if (getCurrentUserName().equals(deployment.getDeploymentRequestUser()) && deployment.getDeploymentState() == DeploymentState.requested) {
            return true;
        } else if (hasPermissionForDeployment(deployment) && deployment.getDeploymentState() != DeploymentState.requested) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the caller is allowed to deploy a specific Resource on the specific Environment
     *
     * @param context
     * @return
     */
    public boolean hasPermissionForDeploymentOnContext(ContextEntity context, ResourceEntity resource) {
        if (context != null) {
            List<String> allowedRoles = new ArrayList<>();
            String permissionName = Permission.DEPLOYMENT.name();
            for (Map.Entry<String, List<RestrictionDTO>> entry : deployableRolesWithRestrictions.entrySet()) {
                matchPermissionsAndContext(permissionName, null, context, resource, allowedRoles, entry);
            }
            if (allowedRoles.isEmpty() || sessionContext == null) {
                return false;
            }
            for (String roleName : allowedRoles) {
                if (sessionContext.isCallerInRole(roleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasRole(String permissionName, ContextEntity context, Action action, ResourceEntity resource) {
        List<String> allowedRoles = new ArrayList<>();
        Set<Map.Entry<String, List<RestrictionDTO>>> entries = getPermissions().entrySet();

        for (Map.Entry<String, List<RestrictionDTO>> entry : entries) {
            if (context == null) {
                matchPermissions(permissionName, action, resource, allowedRoles, entry);
            } else {
                matchPermissionsAndContext(permissionName, action, context, resource, allowedRoles, entry);
            }
        }
        if (allowedRoles.isEmpty() || sessionContext == null) {
            return false;
        }
        for (String roleName : allowedRoles) {
            if (sessionContext.isCallerInRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a Role has the Permission perform a certain Action
     * If so, it adds the role to the list of the allowed roles
     *
     * @param permissionName
     * @param action
     * @param allowedRoles
     * @param entry
     */
    private void matchPermissions(String permissionName, Action action, ResourceEntity resource, List<String> allowedRoles, Map.Entry<String, List<RestrictionDTO>> entry) {
        String roleName = entry.getKey();
        for (RestrictionDTO restrictionDTO : entry.getValue()) {
            if (restrictionDTO.getPermissionName().equals(permissionName) && hasPermissionForAction(restrictionDTO, action)
                    && hasPermissionForResource(restrictionDTO, resource) && hasPermissionForResourceType(restrictionDTO, resource)) {
                allowedRoles.add(roleName);
            }
        }
    }

    /**
     * Checks whether a Role has the Permission perform a certain Action with a specific Resource on a specific Context (or on its parent)
     * If so, it adds the role to the list of the allowed roles
     *
     * @param permissionName
     * @param action
     * @param context
     * @param resource
     * @param allowedRoles
     * @param entry
     */
    private void matchPermissionsAndContext(String permissionName, Action action, ContextEntity context,
                                            ResourceEntity resource, List<String> allowedRoles, Map.Entry<String, List<RestrictionDTO>> entry) {
        for (RestrictionDTO restrictionDTO : entry.getValue()) {
            if (restrictionDTO.getPermissionName().equals(permissionName)) {
                checkContextAndActionAndResource(context, action, resource, allowedRoles, entry, restrictionDTO);
            }
        }
    }

    /**
     * Checks if a Role is allowed to perform a certain Action with a specific Resource on a specific Context (or on its parent)
     * If so, it adds the role to the list of the allowed roles
     *
     * @param context
     * @param action
     * @param allowedRoles
     * @param entry
     * @param restrictionDTO
     */
    private void checkContextAndActionAndResource(ContextEntity context, Action action, ResourceEntity resource,
                                                  List<String> allowedRoles, Map.Entry<String, List<RestrictionDTO>> entry,
                                                  RestrictionDTO restrictionDTO) {
        // RestrictionDTOs created with legacy Permissions have a null Context
        if (hasPermissionForContext(restrictionDTO, context) && hasPermissionForAction(restrictionDTO, action) &&
                hasPermissionForResource(restrictionDTO, resource) && hasPermissionForResourceType(restrictionDTO, resource)) {
            allowedRoles.add(entry.getKey());
        } else if (context.getParent() != null) {
            checkContextAndActionAndResource(context.getParent(), action, resource, allowedRoles, entry, restrictionDTO);
        }
    }

    /**
     * Checks if a Restriction gives permission for a specific Context
     * No Context on Restriction means all Contexts are allowed
     *
     * @param restrictionDTO
     * @param context
     * @return
     */
    private boolean hasPermissionForContext(RestrictionDTO restrictionDTO, ContextEntity context) {
        return restrictionDTO.getRestriction().getContext() == null ||
                context.getName().equals(restrictionDTO.getRestriction().getContext().getName());
    }

    /**
     * Checks if a Restriction gives permission for a specific Action
     *
     * @param restrictionDTO
     * @param action
     * @return
     */
    private boolean hasPermissionForAction(RestrictionDTO restrictionDTO, Action action) {
        return action == null || restrictionDTO.getRestriction().getAction().equals(action) ||
                restrictionDTO.getRestriction().getAction().equals(ALL);
    }

    /**
     * Checks if a Restriction gives permission for a specific Resource
     * No Resource on Restriction means all Resources are allowed
     *
     * @param restrictionDTO
     * @param resource
     * @return
     */
    private boolean hasPermissionForResource(RestrictionDTO restrictionDTO, ResourceEntity resource) {
        return resource == null || restrictionDTO.getRestriction().getResource() == null ||
                restrictionDTO.getRestriction().getResource().equals(resource);
    }

    /**
     * Checks if a Restriction gives permission for a specific ResourceType
     * No ResourceType on Restriction means all ResourceTypes are allowed
     *
     * @param restrictionDTO
     * @param resource
     * @return
     */
    private boolean hasPermissionForResourceType(RestrictionDTO restrictionDTO, ResourceEntity resource) {
        if (resource == null || restrictionDTO.getRestriction().getResourceType() == null) {
            return true;
        }
        if (restrictionDTO.getRestriction().getResourceType().equals(resource.getResourceType())) {
            return true;
        }
        // TODO do we have to check ResourceType from parent of parent as well?
        return resource.getResourceType() != null && resource.getResourceType().getParentResourceType() != null &&
                restrictionDTO.getRestriction().getResourceType().equals(resource.getResourceType().getParentResourceType());
    }

    /**
     * The Properties of ResourceType are modifiable only by the config_admin
     */
    public boolean hasPermissionToEditResourceTypeProperties() {
        return hasPermission(Permission.EDIT_NOT_DEFAULT_RES_OF_RESTYPE);
    }

    /**
     * The ResourceTypeName is modifiable by the config_admin. DefaultResourceType (APPLICATION,
     * APPLICATIONSERVER or NODE) is not modifiable.
     */
    public boolean hasPermissionToRenameResourceType(ResourceTypeEntity resType) {
        return resType != null && hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME)
                && !resType.isDefaultResourceType();
    }

    /**
     * Check that the user is app_developer or config_admin: app_developer: can edit properties of instances
     * of APPLICATION config_admin: can edit all properties.
     *
     * @param
     * @return
     */
    public boolean hasPermissionToEditPropertiesOfResource(ResourceTypeEntity parentResourceTypeOfResource) {
        // config_admin
        if (hasPermission(Permission.EDIT_ALL_PROPERTIES)) {
            return true;
        } else if (parentResourceTypeOfResource != null
                && hasPermission(Permission.EDIT_PROP_LIST_OF_INST_APP)
                && DefaultResourceTypeDefinition.APPLICATION.name().equals(
                parentResourceTypeOfResource.getName())) {
            return true;
        }
        return false;
    }

    /**
     * The ResourceName is modifiable by the config_admin or by the server_admin when the resource is
     * instance's resource of deaultResourceType: APPLICATION/APPLICATIONSERVER/NODE's instance
     *
     * @param res
     * @return
     */
    public boolean hasPermissionToRenameResource(ResourceEntity res) {
        if (hasPermission(Permission.SAVE_ALL_CHANGES) || hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME)) {
            return true;
        }
        if (res.getResourceType() == null) {
            return false;
        }
        // Abwärtskompatibilität: RENAME_INSTANCE_DEFAULT_RESOURCE
        return hasPermission(Permission.RENAME_RESOURCE, null, null, res) ||
                hasPermission(Permission.RENAME_INSTANCE_DEFAULT_RESOURCE);
    }

    /**
     * Check that the user is server_admin or config_admin: server_admin: can delete instances of Default
     * ResourceType(APPLICATION,APPLICATIONSERVER,NODE) config_admin: can delete all instances.
     *
     * @param
     * @return
     */
    public boolean hasPermissionToRemoveDefaultInstanceOfResType(boolean isDefaultResourceType) {
        // Check that the resource is an instance of DefaultResourceType.
        // Permitted to server_admin
        if (isDefaultResourceType
                && hasPermission(Permission.DELETE_RES_INSTANCE_OF_DEFAULT_RESTYPE)) {
            return true;
        } else if (hasPermission(Permission.DELETE_RES)) {
            return true;
        }
        return false;
    }

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin: shakedown_admin: can edit all
     * properties when TestingMode is true config_admin: can edit all properties. app_developer: can edit
     * properties of instances of APPLICATION
     *
     * @param resource
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResource(ResourceEntity resource, boolean isTestingMode) {
        // the config_admin can edit/add/delete all properites
        if (hasPermission(Permission.EDIT_ALL_PROPERTIES)) {
            return true;
        } else if (resource != null && resource.getResourceType().isApplicationResourceType()
                && hasPermission(Permission.EDIT_PROP_LIST_OF_INST_APP)) {
            return true;
        } else if (hasPermission(Permission.SHAKEDOWN_TEST_MODE) && isTestingMode) {
            return true;
        }

        return false;
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can add node
     * relationship config_admin: can add all relationship. app_developer: can add reletionship of instances
     * of APPLICATION
     *
     * @param
     * @return
     */
    public boolean hasPermissionToAddRelation(ResourceEntity resourceEntity, boolean provided) {
        if (resourceEntity != null && resourceEntity.getResourceType() != null) {
            // Check that the user is config_admin
            if (hasPermission(Permission.ADD_EVERY_RELATED_RESOURCE)) {
                return true;
            } else if (resourceEntity.getResourceType().isApplicationServerResourceType()
                    && hasPermission(Permission.ADD_NODE_RELATION)) {
                return true;
            } else if (resourceEntity.getResourceType().isApplicationResourceType()
                    && hasPermission(Permission.ADD_RELATED_RESOURCE)) {
                return (!provided && hasPermission(Permission.ADD_AS_CONSUMED_RESOURCE))
                        || (provided && hasPermission(Permission.ADD_AS_PROVIDED_RESOURCE));
            }
        } else {
            return hasPermission(Permission.ADD_RELATED_RESOURCETYPE);
        }
        return false;
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can delete node
     * relationship config_admin: can delete all relationship. app_developer: can delete reletionship of
     * instances of APPLICATION
     *
     * @param
     * @return
     */
    public boolean hasPermissionToDeleteRelation(ResourceEntity resourceEntity) {
        if (resourceEntity != null && resourceEntity.getResourceType() != null) {
            ResourceTypeEntity resourceTypeEntity = resourceEntity.getResourceType();
            // Check that the user is config_admin
            if (hasPermission(Permission.DELETE_EVERY_RELATED_RESOURCE)) {
                return true;
            }
            // Check that the user is server_admin
            if (hasPermission(Permission.DELETE_NODE_RELATION)
                    && resourceTypeEntity.isApplicationServerResourceType()) {
                return true;
            }
            // Check that the user is app_developer
            if (hasPermission(Permission.DELETE_CONS_OR_PROVIDED_RELATION)
                    && resourceTypeEntity.isApplicationResourceType()) {
                return true;
            }
            if (hasPermission(Permission.SELECT_RUNTIME)
                    && resourceTypeEntity.isRuntimeType()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that the user is config_admin: can delete all resourcetype relationship.
     */
    public boolean hasPermissionToDeleteRelationType(ResourceTypeEntity resourceTypeEntity) {
        return resourceTypeEntity != null && hasPermission(Permission.REMOVE_RELATED_RESOURCETYPE);
    }

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin : shakedown_admin: can
     * modify(add/edit/delete) all testing templates config_admin: can modify(add/edit/delete) all templates
     * app_developer: can modify(add/edit/delete) only templates in instances of APPLICATION
     *
     * @param resourceType  - null for resourceType templates
     * @param isTestingMode - true if testing mode is activated
     * @return
     */
    public boolean hasPermissionToTemplateModify(ResourceTypeEntity resourceType, boolean isTestingMode) {
        // check that the user is config_admin
        if (hasPermission(Permission.SAVE_RESTYPE_TEMPLATE)) {
            return true;
        }// check that the user is app_developer
        else if (isApplicationResourceType(resourceType)) {
            return hasPermission(Permission.SAVE_RES_TEMPLATE);
        }// check that the user is shakedown_admin
        else if (resourceType != null && isTestingMode && hasPermission(Permission.SHAKEDOWN_TEST_MODE)) {
            return true;
        }
        return false;
    }

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin : shakedown_admin: can
     * modify(add/edit/delete) all testing templates config_admin: can modify(add/edit/delete) all templates
     * app_developer: can modify(add/edit/delete) only templates in instances of APPLICATION
     *
     * @param resource      - null for resourceType templates
     * @param isTestingMode - true if testing mode is activated
     * @return
     */
    public boolean hasPermissionToTemplateModify(ResourceEntity resource, boolean isTestingMode) {
        // check that the user is config_admin
        if (hasPermission(Permission.SAVE_RESTYPE_TEMPLATE)) {
            return true;
        }// check that the user is app_developer
        else if (isResourceEntityWithApplicationResourceType(resource)) {
            return hasPermission(Permission.SAVE_RES_TEMPLATE);
        }// check that the user is shakedown_admin
        else if (resource != null && isTestingMode && hasPermission(Permission.SHAKEDOWN_TEST_MODE)) {
            return true;
        }
        return false;
    }

    private boolean isApplicationResourceType(ResourceTypeEntity resourceType) {
        return resourceType != null && resourceType.isApplicationResourceType();

    }

    private boolean isResourceEntityWithApplicationResourceType(ResourceEntity resource) {
        return resource != null && resource.getResourceType().isApplicationResourceType();
    }

    /**
     * Diese Methode gibt den Username zurück
     *
     * @return
     */
    public String getCurrentUserName() {
        return sessionContext.getCallerPrincipal().toString();
    }

}
