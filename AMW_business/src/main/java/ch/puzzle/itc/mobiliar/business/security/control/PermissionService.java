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
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
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
    static Map<String, List<RestrictionEntity>> userRestrictions;

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
        return hasUserRestriction(Permission.DEPLOYMENT.name(), null, null, null, null);
    }

    /**
     * Returns all available Roles with their Restrictions
     * Legacy Permissions are mapped to the new Permission/Restriction model
     *
     * @return Map key=Role.name, value=RestrictionDTOs
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

    private void getUserRestrictions() {
        if (permissionRepository.isReloadUserRestrictionsList() || userRestrictions == null) {
            userRestrictions = new HashMap<>();
        }
        if (!userRestrictions.containsKey(getCurrentUserName())) {
            userRestrictions.put(getCurrentUserName(), Collections.unmodifiableList(permissionRepository.getUserWithRestrictions(getCurrentUserName())));
            if (permissionRepository.isReloadUserRestrictionsList()) {
                permissionRepository.setReloadUserRestrictionsList(false);
            }
        }
    }

    /**
     * Returns a list of all available Restrictions assigned to UserRestriction
     *
     * @return List<RestrictionEntity>
     */
    public List<RestrictionEntity> getAllUserRestrictions() {
        return permissionRepository.getUsersWithRestrictions();
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

    /**
     * Checks if a user has a role or a restriction with a certain Permission no matter for which Actions
     * Useful for displaying/hiding navigation elements in views
     * The specific Action required has to be checked when the action is involved (button)
     *
     * @param permission
     * @return
     */
    public boolean hasPermission(Permission permission) {
        return hasRole(permission.name(), null, null, null, null) ||
                hasUserRestriction(permission.name(), null, null, null, null);
    }

    public boolean hasPermission(Permission permission, Action action) {
        return hasRole(permission.name(), null, action, null, null) ||
                hasUserRestriction(permission.name(), null, action, null, null);
    }

    public boolean hasPermission(Permission permission, Action action, ResourceTypeEntity resourceType) {
        return hasRole(permission.name(), null, action, null, resourceType) ||
                hasUserRestriction(permission.name(), null, action, null, resourceType);
    }

    public boolean hasPermission(Permission permission, ContextEntity context, Action action,
                                 ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType) {
        return hasRole(permission.name(), context, action, resourceGroup, resourceType) ||
                hasUserRestriction(permission.name(), context, action, resourceGroup, resourceType);
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
    public void checkPermissionAndFireException(Permission permission, Action action, String extraInfo) {
        if (!hasPermission(permission, action)) {
            throwNotAuthorizedException(extraInfo);
        }
    }

    /**
     * Checks if given permission is available. If not a exception is created with error message containing extraInfo part.
     *
     * @param permission
     * @param context
     * @param action
     * @param resourceGroup
     * @param resourceType
     * @param extraInfo
     */
    public void checkPermissionAndFireException(Permission permission, ContextEntity context, Action action,
                                                ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType,
                                                String extraInfo) {
        if (!hasPermission(permission, context, action, resourceGroup, resourceType)) {
            throwNotAuthorizedException(extraInfo);
        }
    }

    public void throwNotAuthorizedException(String extraInfo) {
        String errorMessage = "Not Authorized!";
        if (StringUtils.isNotEmpty(extraInfo)) {
            errorMessage += " You're not allowed to " + extraInfo + "!";
        }
        throw new NotAuthorizedException(errorMessage);
    }

    public boolean hasPermissionForDeployment(DeploymentEntity deployment) {
        return deployment != null && hasPermissionForDeploymentOnContext(deployment.getContext(), deployment.getResource().getResourceGroup());
    }

    public boolean hasPermissionForCancelDeployment(DeploymentEntity deployment) {
        if (getCurrentUserName().equals(deployment.getDeploymentRequestUser()) && deployment.getDeploymentState() == DeploymentState.requested) {
            return true;
        }
        return hasPermissionForDeployment(deployment) && deployment.getDeploymentState() != DeploymentState.requested;
    }

    /**
     * Checks if the caller is allowed to deploy a specific ResourceGroup on the specific Environment
     * Note: Both, Permission/Restriction by Group and by User are checked
     *
     * @param context
     * @return
     */
    public boolean hasPermissionForDeploymentOnContext(ContextEntity context, ResourceGroupEntity resourceGroup) {
        if (context != null && sessionContext != null) {
            List<String> allowedRoles = new ArrayList<>();
            String permissionName = Permission.DEPLOYMENT.name();
            for (Map.Entry<String, List<RestrictionDTO>> entry : deployableRolesWithRestrictions.entrySet()) {
                matchPermissionsAndContext(permissionName, null, context, resourceGroup, resourceGroup.getResourceType(), allowedRoles, entry);
            }
            for (String roleName : allowedRoles) {
                if (sessionContext.isCallerInRole(roleName)) {
                    return true;
                }
            }
            return hasUserRestriction(permissionName, context, null, resourceGroup, null);
        }
        return false;
    }

    private boolean hasRole(String permissionName, ContextEntity context, Action action, ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType) {
        if (sessionContext != null) {
            List<String> allowedRoles = new ArrayList<>();
            Set<Map.Entry<String, List<RestrictionDTO>>> entries = getPermissions().entrySet();

            if (resourceType == null && resourceGroup != null) {
                resourceType = resourceGroup.getResourceType();
            }

            for (Map.Entry<String, List<RestrictionDTO>> entry : entries) {
                if (context == null) {
                    matchPermissions(permissionName, action, resourceGroup, resourceType, allowedRoles, entry);
                } else {
                    matchPermissionsAndContext(permissionName, action, context, resourceGroup, resourceType, allowedRoles, entry);
                }
            }
            for (String roleName : allowedRoles) {
                if (sessionContext.isCallerInRole(roleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the logged-in user has a Restriction with the required Permission for a specific Context, Action,
     * ResourceGroup and/or ResourceType
     *
     * @param permissionName
     * @param context
     * @param action
     * @param resourceGroup
     * @param resourceType
     * @return
     */
    private boolean hasUserRestriction(String permissionName, ContextEntity context, Action action, ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType) {
        if (sessionContext == null) {
            return false;
        }
        getUserRestrictions();
        if (!userRestrictions.get(getCurrentUserName()).isEmpty()) {
            for (RestrictionEntity restrictionEntity : userRestrictions.get(getCurrentUserName())) {
                if (restrictionEntity.getPermission().getValue().equals(permissionName)) {
                    return hasRequiredUserRestriction(context, action, resourceGroup, resourceType, restrictionEntity);
                }
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
     * @param resourceGroup
     * @param resourceType
     * @param allowedRoles
     * @param entry
     */
    private void matchPermissions(String permissionName, Action action, ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType, List<String> allowedRoles, Map.Entry<String, List<RestrictionDTO>> entry) {
        String roleName = entry.getKey();
        for (RestrictionDTO restrictionDTO : entry.getValue()) {
            if (restrictionDTO.getPermissionName().equals(permissionName) && hasPermissionForAction(restrictionDTO, action)
                    && hasPermissionForResource(restrictionDTO, resourceGroup) && hasPermissionForResourceType(restrictionDTO, resourceType)
                    && hasPermissionForDefaultResourceType(restrictionDTO, resourceType)) {
                allowedRoles.add(roleName);
            }
        }
    }

    /**
     * Checks whether a Role has the Permission perform a certain Action with a specific ResourceGroup on a specific Context (or on its parent)
     * If so, it adds the role to the list of the allowed roles
     *
     * @param permissionName
     * @param action
     * @param context
     * @param resourceGroup
     * @param allowedRoles
     * @param entry
     */
    private void matchPermissionsAndContext(String permissionName, Action action, ContextEntity context,
                                            ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType, List<String> allowedRoles, Map.Entry<String, List<RestrictionDTO>> entry) {
        for (RestrictionDTO restrictionDTO : entry.getValue()) {
            if (restrictionDTO.getPermissionName().equals(permissionName)) {
                checkContextAndActionAndResource(context, action, resourceGroup, resourceType, allowedRoles, entry, restrictionDTO);
            }
        }
    }

    /**
     * Checks if a Role is allowed to perform a certain Action with a specific ResourceGroup on a specific Context (or on its parent)
     * If so, it adds the role to the list of the allowed roles
     *
     * @param context
     * @param action
     * @param resource
     * @param resourceType
     * @param allowedRoles
     * @param entry
     * @param restrictionDTO
     */
    private void checkContextAndActionAndResource(ContextEntity context, Action action, ResourceGroupEntity resource, ResourceTypeEntity resourceType,
                                                  List<String> allowedRoles, Map.Entry<String, List<RestrictionDTO>> entry,
                                                  RestrictionDTO restrictionDTO) {
        if (hasPermissionForContext(restrictionDTO, context) && hasPermissionForAction(restrictionDTO, action) &&
                hasPermissionForResource(restrictionDTO, resource) && hasPermissionForResourceType(restrictionDTO, resourceType)
                && hasPermissionForDefaultResourceType(restrictionDTO, resourceType)) {
            allowedRoles.add(entry.getKey());
        } else if (context != null && context.getParent() != null) {
            checkContextAndActionAndResource(context.getParent(), action, resource, resourceType, allowedRoles, entry, restrictionDTO);
        }
    }

    /**
     * Checks if a User is allowed to perform a certain Action with a specific ResourceGroup on a specific Context (or on its parent)
     *
     * @param context
     * @param action
     * @param resource
     * @param resourceType
     * @param restriction
     * @return
     */
    private boolean hasRequiredUserRestriction(ContextEntity context, Action action, ResourceGroupEntity resource,
                                                  ResourceTypeEntity resourceType, RestrictionEntity restriction) {
        if (hasPermissionForContext(restriction, context) && hasPermissionForAction(restriction, action) &&
                hasPermissionForResource(restriction, resource) && hasPermissionForResourceType(restriction, resourceType)
                && hasPermissionForDefaultResourceType(restriction, resourceType)) {
            return true;
        } else if (context != null && context.getParent() != null) {
            hasRequiredUserRestriction(context.getParent(), action, resource, resourceType, restriction);
        }
        return false;
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
        return hasPermissionForContext(restrictionDTO.getRestriction(), context);
    }

    /**
     * Checks if a Restriction gives permission for a specific Context
     * No Context on Restriction means all Contexts are allowed
     *
     * @param restriction
     * @param context
     * @return
     */
    private boolean hasPermissionForContext(RestrictionEntity restriction, ContextEntity context) {
        return restriction.getContext() == null || context.getId().equals(restriction.getContext().getId());
    }

    /**
     * Checks if a Restriction gives permission for a specific Action
     *
     * @param restrictionDTO
     * @param action
     * @return
     */
    private boolean hasPermissionForAction(RestrictionDTO restrictionDTO, Action action) {
        return hasPermissionForAction(restrictionDTO.getRestriction(), action);
    }

    /**
     * Checks if a Restriction gives permission for a specific Action
     *
     * @param restriction
     * @param action
     * @return
     */
    private boolean hasPermissionForAction(RestrictionEntity restriction, Action action) {
        return action == null || restriction.getAction().equals(action) ||
                restriction.getAction().equals(ALL);
    }

    /**
     * Checks if a Restriction gives permission for a specific ResourceGroup
     * No Resource on Restriction means all ResourceGroups are allowed
     *
     * @param restrictionDTO
     * @param resourceGroup
     * @return
     */
    private boolean hasPermissionForResource(RestrictionDTO restrictionDTO, ResourceGroupEntity resourceGroup) {
        return hasPermissionForResource(restrictionDTO.getRestriction(), resourceGroup);
    }

    /**
     * Checks if a Restriction gives permission for a specific ResourceGroup
     * No Resource on Restriction means all ResourceGroups are allowed
     *
     * @param restriction
     * @param resourceGroup
     * @return
     */
    private boolean hasPermissionForResource(RestrictionEntity restriction, ResourceGroupEntity resourceGroup) {
        return resourceGroup == null || restriction.getResourceGroup() == null ||
                restriction.getResourceGroup().getId().equals(resourceGroup.getId());
    }

    /**
     * Checks if a Restriction gives permission for a specific ResourceType
     * No ResourceType on Restriction means all ResourceTypes are allowed
     *
     * @param restrictionDTO
     * @param resourceType
     * @return
     */
    private boolean hasPermissionForResourceType(RestrictionDTO restrictionDTO, ResourceTypeEntity resourceType) {
        return hasPermissionForResourceType(restrictionDTO.getRestriction(), resourceType);
    }

    /**
     * Checks if a Restriction gives permission for a specific ResourceType
     * No ResourceType on Restriction means all ResourceTypes are allowed
     *
     * @param restriction
     * @param resourceType
     * @return
     */
    private boolean hasPermissionForResourceType(RestrictionEntity restriction, ResourceTypeEntity resourceType) {
        if (resourceType == null || restriction.getResourceType() == null) {
            return true;
        }
        if (restriction.getResourceType().getId().equals(resourceType.getId())) {
            return true;
        }
        return resourceType.getParentResourceType() != null &&
                restriction.getResourceType().getId().equals(resourceType.getParentResourceType().getId());
    }

    /**
     * Checks if a Restriction gives permission for a specific (Default)ResourceType
     * No DefaultResourceType on Restriction means all ResourceTypes (including DefaultResourceTypes) are allowed
     *
     * @param restrictionDTO
     * @param resourceType
     * @return
     */
    private boolean hasPermissionForDefaultResourceType(RestrictionDTO restrictionDTO, ResourceTypeEntity resourceType) {
        return hasPermissionForDefaultResourceType(restrictionDTO.getRestriction(), resourceType);
    }

    /**
     * Checks if a Restriction gives permission for a specific (Default)ResourceType
     * No DefaultResourceType on Restriction means all ResourceTypes (including DefaultResourceTypes) are allowed
     *
     * @param restriction
     * @param resourceType
     * @return
     */
    private boolean hasPermissionForDefaultResourceType(RestrictionEntity restriction, ResourceTypeEntity resourceType) {
        // Default and non DefaultTypes are allowed
        if (resourceType == null || restriction.getResourceTypePermission().equals(ResourceTypePermission.ANY)) {
            return true;
        }
        // Only DefaultTypes are allowed
        if (restriction.getResourceTypePermission().equals(ResourceTypePermission.DEFAULT_ONLY)
                && DefaultResourceTypeDefinition.contains(resourceType.getName())) {
            return true;
        }
        // Only non DefaultTypes are allowed
        return restriction.getResourceTypePermission().equals(ResourceTypePermission.NON_DEFAULT_ONLY)
                && !DefaultResourceTypeDefinition.contains(resourceType.getName());
    }

    /**
     * Check if the user can delete instances of ResourceTypes
     *
     * @param resourceType
     * @return
     */
    public boolean hasPermissionToRemoveInstanceOfResType(ResourceTypeEntity resourceType) {
        return hasPermission(Permission.RESOURCE, Action.DELETE, resourceType);
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can add node
     * relationship config_admin: can add all relationship. app_developer: can add reletionship of instances
     * of APPLICATION
     *
     * @param
     * @return
     */
    public boolean hasPermissionToAddRelation(ResourceEntity resourceEntity, boolean provided, ContextEntity context) {
        if (resourceEntity != null && resourceEntity.getResourceType() != null) {
            if (hasPermission(Permission.RESOURCE, context, Action.UPDATE, resourceEntity.getResourceGroup(), null)) {
                return true;
            }
            // TODO migrate existing Permissions to Restrictions (?)
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
        }
        return hasPermission(Permission.ADD_RELATED_RESOURCETYPE);
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can delete node
     * relationship config_admin: can delete all relationship. app_developer: can delete reletionship of
     * instances of APPLICATION
     *
     * @param
     * @return
     */
    public boolean hasPermissionToDeleteRelation(ResourceEntity resourceEntity, ContextEntity context) {
        if (resourceEntity != null && resourceEntity.getResourceType() != null) {
            if (hasPermission(Permission.RESOURCE, context, Action.UPDATE, resourceEntity.getResourceGroup(), null)) {
                return true;
            }
            // TODO migrate existing Permissions to Restrictions (?)
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
            return hasPermission(Permission.SELECT_RUNTIME) && resourceTypeEntity.isRuntimeType();
        }
        return false;
    }

    /**
     * Check that the user is config_admin: can delete all resourcetype relationship.
     */
    public boolean hasPermissionToDeleteRelationType(ResourceTypeEntity resourceTypeEntity) {
        if (hasPermission(Permission.RESOURCE, null, Action.UPDATE, null, resourceTypeEntity)) {
            return true;
        }
        // TODO migrate Permission to Restriction (?)
        return resourceTypeEntity != null && hasPermission(Permission.REMOVE_RELATED_RESOURCETYPE);
    }

    /**
     * Checks if user may create or edit Templates of Resources
     *
     * @param resource
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToModifyResourceTemplate(ResourceEntity resource, boolean isTestingMode) {
        // ok if user has update permission on the Resource, context is always global, so we set it to null to omit the check
        if (hasPermission(Permission.RESOURCE, null, Action.UPDATE, resource.getResourceGroup(), null) ||
                hasPermission(Permission.TEMPLATE_RESOURCE, null, Action.UPDATE, resource.getResourceGroup(), null)) {
            return true;
        }
        return resource != null && isTestingMode && hasPermission(Permission.SHAKEDOWN_TEST_MODE);
    }

    /**
     * Checks if user may create or edit Templates of ResourceTypes
     *
     * @param resourceType
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToModifyResourceTypeTemplate(ResourceTypeEntity resourceType, boolean isTestingMode) {
        // ok if user has update permission on the ResourceType, context is always global, so we set it to null to omit the check
        if (hasPermission(Permission.RESOURCETYPE, null, Action.UPDATE, null, resourceType) ||
                hasPermission(Permission.TEMPLATE_RESOURCETYPE, null, Action.UPDATE, null, resourceType)) {
            return true;
        }
        return resourceType != null && isTestingMode && hasPermission(Permission.SHAKEDOWN_TEST_MODE);
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
