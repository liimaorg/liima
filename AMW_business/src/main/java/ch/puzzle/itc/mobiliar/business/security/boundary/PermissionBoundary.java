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

import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionRepository;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.control.RestrictionRepository;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A boundary for checking permissions of view elements
 */
@Stateless
public class PermissionBoundary implements Serializable {

    @Inject
    EntityManager entityManager;

    @Inject
    PermissionService permissionService;

    @Inject
    RestrictionRepository restrictionRepository;

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    ContextLocator contextLocator;

    @Inject
    ResourceGroupRepository resourceGroupRepository;

    @Inject
    ResourceTypeProvider resourceTypeProvider;

    @Inject
    ResourceTypeRepository resourceTypeRepository;

    @Inject
    ResourceRepository resourceRepository;

    /**
     * Checks if the user is allowed to edit the Properties of a ResourceType in a specific Context
     *
     * @param resourceTypeId
     * @param contextId
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceTypeAndContext(Integer resourceTypeId, Integer contextId) {

            ResourceTypeEntity resourceType = entityManager.find(ResourceTypeEntity.class, resourceTypeId);
            ContextEntity context = contextId == null ? null : contextLocator.getContextById(contextId);
            return permissionService.hasPermission(Permission.RESOURCETYPE, context, Action.UPDATE, null, resourceType);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a ResourceType without checking its Context
     *
     * @param resourceTypeId
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceType(Integer resourceTypeId) {
        return hasPermissionToEditPropertiesByResourceTypeAndContext(resourceTypeId, null);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a Resource in a specific Context
     *
     * @param resourceId
     * @param context
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceAndContext(Integer resourceId, ContextEntity context
                                                                     ) {
            ResourceEntity resource = entityManager.find(ResourceEntity.class, resourceId);
            return permissionService.hasPermission(Permission.RESOURCE, context, Action.UPDATE,
                    resource.getResourceGroup(), null);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a Resource in a specific Context
     *
     * @param resourceId
     * @param contextId
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceAndContext(Integer resourceId,
                                                                     Integer contextId) {
        ContextEntity context = contextLocator.getContextById(contextId);
        return hasPermissionToEditPropertiesByResourceAndContext(resourceId, context);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a Resource without checking its Context
     *
     * @param resourceId
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResource(Integer resourceId) {
        return hasPermissionToEditPropertiesByResourceAndContext(resourceId, (ContextEntity) null);
    }

    public boolean hasPermission(String permissionName) {
        Permission permission = Permission.valueOf(permissionName);
        return permissionService.hasPermission(permission);
    }

    public boolean hasPermission(String permissionName, String actionName) {
        Permission permission = Permission.valueOf(permissionName);
        Action action = Action.valueOf(actionName);
        return permissionService.hasPermission(permission, action);
    }

    public boolean hasPermissionOnAllContext(String permissionName, String actionName) {
        Permission permission = Permission.valueOf(permissionName);
        Action action = Action.valueOf(actionName);
        return permissionService.hasPermissionOnAllContext(permission, action, null, null);
    }

    public boolean hasPermissionForResourceType(String permissionName, String actionName, String resourceTypeName) {
        Permission permission = Permission.valueOf(permissionName);
        Action action = Action.valueOf(actionName);
        final ResourceTypeEntity resourceType = resourceTypeRepository.getByName(resourceTypeName);
        return resourceType != null && permissionService.hasPermission(permission, action, resourceType);
    }

    public boolean hasPermissionForResourceType(String permissionName, String actionName, String resourceTypeName, Integer contextId) {
        Permission permission = Permission.valueOf(permissionName);
        Action action = Action.valueOf(actionName);
        final ResourceTypeEntity resourceType = resourceTypeRepository.getByName(resourceTypeName);
        final ContextEntity context = contextLocator.getContextById(contextId);
        return resourceType != null && permissionService.hasPermission(permission, context, action, null, resourceType);
    }

    public boolean hasPermission(Permission permission) {
        return permissionService.hasPermission(permission);
    }

    public boolean hasPermission(Permission permission, Action action) {
        return permissionService.hasPermission(permission, action);
    }

    public boolean hasPermission(Permission permission, ContextEntity context, Action action, ResourceEntity resource, ResourceTypeEntity resourceType) {
        if (resource != null) {
            return permissionService.hasPermission(permission, context, action, resource.getResourceGroup(), resourceType);
        }
        return permissionService.hasPermission(permission, context, action, null, resourceType);
    }

    public boolean hasPermission(Permission permission, Action action, ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType) {
        return permissionService.hasPermission(permission, null, action, resourceGroup, resourceType);
    }

    public boolean hasPermission(Permission permission, Action action, ContextEntity context, ResourceGroupEntity resourceGroup) {
        return permissionService.hasPermission(permission, context, action, resourceGroup, null);
    }

    /**
     * Checks if given permission is available. If not a checked exception is thrown with error message
     * containing extraInfo part.
     *
     * @param permission
     * @param extraInfo
     */
    public void checkPermissionAndFireException(Permission permission, String extraInfo) {
        permissionService.checkPermissionAndFireException(permission, extraInfo);
    }

    /**
     * Checks if given permission is available. If not a checked exception is thrown with error message
     * containing extraInfo part.
     *
     * @param permission
     * @param extraInfo
     */
    public void checkPermissionAndFireException(Permission permission, Action action, String extraInfo) {
        permissionService.checkPermissionAndFireException(permission, action, extraInfo);
    }

    /**
     * Checks if given permission is available. If not a checked exception is thrown with error message
     * containing extraInfo part.
     *
     * @param permission the required Permission
     * @param context the affected ContextEntity
     * @param action the required Action
     * @param resourceGroup the affected ResourceGroupEntity
     * @param resourceType the affected ResourceTypeEntity
     * @param extraInfo the message for the exception
     */
    public void checkPermissionAndFireException(Permission permission, ContextEntity context, Action action,
                                                ResourceGroupEntity resourceGroup, ResourceTypeEntity resourceType,
                                                String extraInfo) {
        permissionService.checkPermissionAndFireException(permission, context, action, resourceGroup, resourceType, extraInfo);
    }

    /**
     * Checks if user is allowed to delete a Relation of a Resource
     */
    public boolean hasPermissionToDeleteRelation(ResourceEntity resourceEntity, ContextEntity context) {
        ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceEntity.getId());
        return permissionService.hasPermissionToDeleteRelation(mergedResource, context);
    }

    /**
     * Checks if user is allowed to delete a Relation of a ResourceType
     */
    public boolean hasPermissionToDeleteRelationType(ResourceTypeEntity resourceTypeEntity) {
        ResourceTypeEntity mergedResource = entityManager.find(ResourceTypeEntity.class, resourceTypeEntity.getId());
        return permissionService.hasPermissionToDeleteRelationType(mergedResource);
    }

    /**
     * Checks if user is allowed to add a Relation to a Resource
     */
    public boolean hasPermissionToAddRelation(ResourceEntity resourceEntity, ContextEntity context) {
        ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceEntity.getId());
        return permissionService.hasPermissionToAddRelation(mergedResource, context);
    }

    /**
     * Checks if user is allowed to add a Relation to a ResourceType
     */
    public boolean hasPermissionToAddRelatedResourceType(ResourceTypeEntity resourceTypeEntity) {
        ResourceTypeEntity mergedResource = entityManager.find(ResourceTypeEntity.class, resourceTypeEntity.getId());
        return permissionService.hasPermissionToAddRelatedResourceType(mergedResource);
    }

    /**
     * Check that the user is config_admin, app_developer
     * config_admin: can modify(add/edit/delete) all templates
     * app_developer: can modify(add/edit/delete) only templates in instances of APPLICATION
     */
    public boolean hasPermissionToAddTemplate(Identifiable resourceOrResourceTypeEntity) {
        if (resourceOrResourceTypeEntity != null) {
            if (resourceOrResourceTypeEntity instanceof ResourceEntity) {
                ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToAddResourceTemplate(mergedResource);
            }
            if (resourceOrResourceTypeEntity instanceof ResourceTypeEntity) {
                ResourceTypeEntity mergedResourceType = entityManager.find(ResourceTypeEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToAddResourceTypeTemplate(mergedResourceType);
            }
        }
        return false;
    }

    public boolean canCreateFunctionOfResourceOrResourceType(Integer resourceEntityId, Integer resourceTypeEntityId) {
        return canModifyFunctionOfResourceOrResourceType(resourceEntityId, resourceTypeEntityId, Action.CREATE);
    }

    public boolean canUpdateFunctionOfResourceOrResourceType(Integer resourceEntityId, Integer resourceTypeEntityId) {
        return canModifyFunctionOfResourceOrResourceType(resourceEntityId, resourceTypeEntityId, Action.UPDATE);
    }

    public boolean canDeleteFunctionOfResourceOrResourceType(Integer resourceEntityId, Integer resourceTypeEntityId) {
        return canModifyFunctionOfResourceOrResourceType(resourceEntityId, resourceTypeEntityId, Action.DELETE);
    }

    private boolean canModifyFunctionOfResourceOrResourceType(Integer resourceEntityId, Integer resourceTypeEntityId, Action action) {
        // context is always global
        if (resourceEntityId != null) {
            ResourceEntity resource = resourceRepository.find(resourceEntityId);
            return permissionService.hasPermission(Permission.RESOURCE_AMWFUNCTION, null, action, resource.getResourceGroup(), null);
        }
        ResourceTypeEntity type = resourceTypeRepository.find(resourceTypeEntityId);
        return permissionService.hasPermission(Permission.RESOURCETYPE_AMWFUNCTION, null, action, null, type);
    }

    public boolean canModifyTemplateOfResourceOrResourceType(Integer resourceEntityId, Integer resourceTypeEntityId, Action action) {
        // context is always global
        if (resourceEntityId != null) {
            ResourceEntity resource = resourceRepository.find(resourceEntityId);
            return permissionService.hasPermission(Permission.RESOURCE_TEMPLATE, null, action, resource.getResourceGroup(), null);
        }
        ResourceTypeEntity type = resourceTypeRepository.find(resourceTypeEntityId);
        return permissionService.hasPermission(Permission.RESOURCE_TEMPLATE, null, action, null, type);
    }

    public boolean canCreateResourceInstance(DefaultResourceTypeDefinition type) {
        return canCreateResourceInstance(resourceTypeProvider.getOrCreateDefaultResourceType(type));
    }

    public boolean canCreateResourceInstance(ResourceTypeEntity type) {
        return permissionService.hasPermission(Permission.RESOURCE, Action.CREATE, type);
    }

    public boolean canCreateAppAndAddToAppServer(ResourceEntity resource) {
        return permissionService.hasPermission(Permission.RESOURCE, Action.CREATE, resource.getResourceType()) &&
                permissionService.hasPermission(Permission.RESOURCE, Action.UPDATE, resourceTypeProvider.getOrCreateDefaultResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER));
    }

    public boolean canCopyFromResource(ResourceEntity resourceEntity) {
        return !(resourceEntity == null || resourceEntity.getResourceType() == null) &&
                permissionService.hasPermission(Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE, null, Action.ALL, resourceEntity.getResourceGroup(), resourceEntity.getResourceType());
    }

    /**
     *
     * @param originResource (from)
     * @param targetResourceGroup (to)
     * @return
     */
    public boolean canCopyFromSpecificResource(ResourceEntity originResource, ResourceGroupEntity targetResourceGroup) {
        return !(originResource == null || targetResourceGroup == null) &&
                permissionService.hasPermission(Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE, null, Action.ALL, targetResourceGroup, targetResourceGroup.getResourceType()) &&
                permissionService.hasPermission(Permission.RESOURCE, null, Action.READ, originResource.getResourceGroup(), originResource.getResourceType()) &&
                permissionService.hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.READ, originResource.getResourceGroup(), originResource.getResourceType()) &&
                permissionService.hasPermission(Permission.RESOURCE_AMWFUNCTION, null, Action.READ, originResource.getResourceGroup(), originResource.getResourceType()) &&
                permissionService.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, Action.ALL, originResource.getResourceGroup(), originResource.getResourceType());
    }

    public boolean canReadFromResource(ResourceGroupEntity resourceGroup) {
        return !(resourceGroup == null)
                && permissionService.hasPermission(Permission.RESOURCE, null, Action.READ, resourceGroup, resourceGroup.getResourceType());
    }

    public boolean canToggleDecryptionOfResource(Integer resourceEntityId ) {
        if (resourceEntityId == null) {
            return false;
        }
        ResourceEntity resource = entityManager.find(ResourceEntity.class, resourceEntityId);
        return permissionService.hasPermissionOnAllContext(Permission.RESOURCE_PROPERTY_DECRYPT, Action.ALL, resource.getResourceGroup(), null);
    }

    public boolean canToggleDecryptionOfResourceType(Integer resourceTypeEntityId) {
        if (resourceTypeEntityId == null) {
            return false;
        }
        ResourceTypeEntity resourceType = entityManager.find(ResourceTypeEntity.class, resourceTypeEntityId);
        return permissionService.hasPermissionOnAllContext(Permission.RESOURCETYPE_PROPERTY_DECRYPT, Action.ALL, null, resourceType);
    }

    public boolean hasPermissionToDeploy() {
        return permissionService.hasPermissionToSeeDeployment();
    }

    public boolean hasPermissionToCreateDeployment() {
        return permissionService.hasPermissionToCreateDeployment();
    }

    @HasPermission(oneOfPermission = { Permission.ASSIGN_REMOVE_PERMISSION, Permission.PERMISSION_DELEGATION })
    public RestrictionEntity findRestriction(Integer id) {
        return restrictionRepository.find(id);
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<RestrictionEntity> findAllRestrictions() {
        return restrictionRepository.findAll();
    }

    /**
     * Creates the required Restrictions needed to manage and delegate permissions for self created resources
     * It only adds Restrictions which the caller does not already have (by its Role AND/OR as User)
     *
     * @param resource
     * @throws AMWException
     */
    public void createAutoAssignedRestrictions(ResourceEntity resource) throws AMWException {
        Integer resourceGroupId = resource.getResourceGroup().getId();
        if (resourceGroupId != null && getUserName() != null
                && permissionService.hasPermission(Permission.ADD_ADMIN_PERMISSIONS_ON_CREATED_RESOURCE)) {
            createAutoAssignedRestriction(getUserName(), Permission.RESOURCE.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
            createAutoAssignedRestriction(getUserName(), Permission.RESOURCE_AMWFUNCTION.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
            createAutoAssignedRestriction(getUserName(), Permission.RESOURCE_PROPERTY_DECRYPT.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
            createAutoAssignedRestriction(getUserName(), Permission.RESOURCE_TEMPLATE.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
            createAutoAssignedRestriction(getUserName(), Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
            if (resource.getResourceType().isApplicationServerResourceType()) {
                createAutoAssignedRestriction(getUserName(), Permission.RESOURCE_TEST_GENERATION.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
                createAutoAssignedRestriction(getUserName(), Permission.RESOURCE_TEST_GENERATION_RESULT.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
                createAutoAssignedRestriction(getUserName(), Permission.DEPLOYMENT.name(), resourceGroupId, Action.ALL, new RestrictionEntity());
            }
            reloadCache();
        }
    }

    /**
     * Creates a new RestrictionEntity and returns its id
     *
     * @param roleName
     * @param permissionName
     * @param resourceGroupId
     * @param resourceTypeName
     * @param contextName
     * @param action
     * @param delegated true if the Restriction to be created is delegated
     * @return Id of the newly created RestrictionEntity
     * @throws AMWException
     */
    @HasPermission(oneOfPermission = { Permission.ASSIGN_REMOVE_PERMISSION, Permission.PERMISSION_DELEGATION }, action = Action.CREATE)
    public Integer createRestriction(String roleName, String userName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     ResourceTypePermission resourceTypePermission, String contextName, Action action, boolean delegated, boolean reload)
            throws AMWException {
        if (!delegated || canDelegateThisPermission(permissionName, resourceGroupId, resourceTypeName, contextName, action)) {
            RestrictionEntity restriction = new RestrictionEntity();
            Integer id = createRestriction(roleName, userName, permissionName, resourceGroupId, resourceTypeName,
                    resourceTypePermission, contextName, action, restriction);
            if (reload) {
                reloadCache();
            }
            return id;
        }
        throw new AMWException("No permission to create this permission");
    }

    /**
     * Creates multiple RestrctionEntites and returns how many that have been created
     *
     * @param roleName max one Role name
     * @param userNames none or more User names
     * @param permissionNames at least one Permission name
     * @param resourceGroupIds none or more ResourceGroup ids
     * @param resourceTypeNames none or more ResourceType names
     * @param resourceTypePermission max one ResourceTypePermission
     * @param contextNames none or more Context names
     * @param actions at least one Action
     * @param delegated true if the Restrictions to be created are delegated
     * @return int number of created Restrictions
     */
    @HasPermission(oneOfPermission = { Permission.ASSIGN_REMOVE_PERMISSION, Permission.PERMISSION_DELEGATION }, action = Action.CREATE)
    public int createMultipleRestrictions(String roleName, List<String> userNames, List<String> permissionNames, List<Integer> resourceGroupIds, List<String> resourceTypeNames,
                                              ResourceTypePermission resourceTypePermission, List<String> contextNames, List<Action> actions, boolean delegated, boolean reload) throws AMWException {
        int count = 0;
        if (resourceGroupIds != null && !resourceGroupIds.isEmpty() && resourceTypeNames != null && !resourceTypeNames.isEmpty()) {
            throw new AMWException("Only ResourceGroupId(s) OR ResourceTypeName(s) must be set");
        }
        if (userNames == null) {
            userNames = new ArrayList<>();
        }
        if (resourceGroupIds == null) {
            resourceGroupIds = new ArrayList<>();
        }
        if (resourceTypeNames == null) {
            resourceTypeNames = new ArrayList<>();
        }
        if (contextNames == null || contextNames.isEmpty()) {
            contextNames = new ArrayList<>();
            contextNames.add(null);
        }

        for (String permissionName : permissionNames) {
            for (Action action : actions) {
                if (roleName != null) {
                    if (resourceGroupIds.isEmpty() && resourceTypeNames.isEmpty()) {
                        count += createRestrictionPerContext(roleName, null, permissionName, null, null, resourceTypePermission, contextNames, action, delegated);
                    } else {
                        for (Integer resourceGroupId : resourceGroupIds) {
                            count += createRestrictionPerContext(roleName, null, permissionName, resourceGroupId, null, resourceTypePermission, contextNames, action, delegated);
                        }
                        for (String resourceTypeName : resourceTypeNames) {
                            count += createRestrictionPerContext(roleName, null, permissionName, null, resourceTypeName, resourceTypePermission, contextNames, action, delegated);
                        }
                    }
                }
                for (String userName : userNames) {
                    if (resourceGroupIds.isEmpty() && resourceTypeNames.isEmpty()) {
                        count += createRestrictionPerContext(null, userName, permissionName, null, null, resourceTypePermission, contextNames, action, delegated);
                    } else {
                        for (Integer resourceGroupId : resourceGroupIds) {
                            count += createRestrictionPerContext(null, userName, permissionName, resourceGroupId, null, resourceTypePermission, contextNames, action, delegated);
                        }
                        for (String resourceTypeName : resourceTypeNames) {
                            count += createRestrictionPerContext(null, userName, permissionName, null, resourceTypeName, resourceTypePermission, contextNames, action, delegated);
                        }
                    }
                }
            }
        }
        if (reload) {
            reloadCache();
        }
        return count;
    }

    private int createRestrictionPerContext(String roleName, String userName, String permissionName, Integer resourceGroupId, String resourceTypeName, ResourceTypePermission resourceTypePermission, List<String> contextNames, Action action, boolean delegated) throws AMWException {
        int count = 0;
        for (String contextName : contextNames) {
            if (!delegated || canDelegateThisPermission(permissionName, resourceGroupId, resourceTypeName, contextName, action)) {
                RestrictionEntity restriction = new RestrictionEntity();
                if (createRestriction(roleName, userName, permissionName, resourceGroupId, resourceTypeName,
                        resourceTypePermission, contextName, action, restriction) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean canDelegatePermissionsForThisResource(ResourceEntity resource, ContextEntity context) {
        return (permissionService.hasPermission(Permission.PERMISSION_DELEGATION) && canDelegateThisPermission(Permission.RESOURCE.name(), resource.getResourceGroup().getId(), null, context.getName(), null));
    }

    private boolean canDelegateThisPermission(String permissionName, Integer resourceGroupId, String resourceTypeName, String contextName, Action action) {
        Permission permission = Permission.valueOf(permissionName);
        ResourceGroupEntity resourceGroup = resourceGroupId != null ? resourceGroupRepository.find(resourceGroupId) : null;
        ResourceTypeEntity resourceType = resourceTypeName != null ? resourceTypeRepository.getByName(resourceTypeName) : null;
        ContextEntity context = contextName != null ? contextLocator.getContextByName(contextName) : null;
        if (action == null) {
            action = Action.ALL;
        }
        return permissionService.hasPermissionToDelegatePermission(permission, resourceGroup, resourceType, context, action);
    }

    private Integer createRestriction(String roleName, String userName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     ResourceTypePermission resourceTypePermission, String contextName, Action action, RestrictionEntity restriction)
            throws AMWException {
        validateRestriction(roleName, userName, permissionName, resourceGroupId, resourceTypeName, resourceTypePermission,
                contextName, action, restriction);
        if (permissionService.identicalOrMoreGeneralRestrictionExists(restriction)) {
            return null;
        }
        return restrictionRepository.create(restriction);
    }

    private Integer createAutoAssignedRestriction(String userName, String permissionName, Integer resourceGroupId, Action action, RestrictionEntity restriction)
            throws AMWException {
        validateRestriction(null, userName, permissionName, resourceGroupId, null, null, null, action, restriction);
        if (permissionService.callerHasIdenticalOrMoreGeneralRestriction(restriction)) {
            return null;
        }
        final Integer id = restrictionRepository.create(restriction);
        return id;
    }

    /**
     * Updates an existing RestrictionEntity
     *
     * @param id
     * @param roleName
     * @param permissionName
     * @param contextName
     * @param action
     * @return boolean true successful, false if a similar permission exists
     * @throws AMWException
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION, action = Action.UPDATE)
    public boolean updateRestriction(Integer id, String roleName, String userName, String permissionName, Integer resourceId,
                                  String resourceTypeName, ResourceTypePermission resourceTypePermission,
                                  String contextName, Action action, boolean reload) throws AMWException {
        if (id == null) {
            throw new AMWException("Id must not be null");
        }
        RestrictionEntity restriction = restrictionRepository.find(id);
        if (restriction == null) {
            throw new AMWException("Restriction not found");
        }
        validateRestriction(roleName, userName, permissionName, resourceId, resourceTypeName, resourceTypePermission,
                contextName, action, restriction);
        if (permissionService.identicalOrMoreGeneralRestrictionExists(restriction)) {
            return false;
        }
        restrictionRepository.merge(restriction);
        if (reload) {
            reloadCache();
        }
        return true;
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION, action = Action.DELETE)
    public void removeRestriction(Integer id, boolean reload) throws AMWException {
        if (restrictionRepository.find(id) == null) {
            throw new AMWException("Restriction not found");
        }
        restrictionRepository.deleteRestrictionById(id);
        if (reload) {
            reloadCache();
        }
    }

    /**
     * Returns all available Roles with their Restrictions
     *
     * @return Map key=Role.name, value=RestrictionDTOs
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public Map<String, List<RestrictionDTO>> getAllPermissions() {
        return permissionService.getPermissions();
    }

    /**
     * Returns an uncached list of all available Restrictions assigned to UserRestriction (used by REST)
     *
     * @return List<RestrictionEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<RestrictionEntity> getAllUserRestriction() {
        return permissionService.getAllUserRestrictions();
    }

    /**
     * Returns an uncached list of all available UserRestriction names (used by REST)
     *
     * @return List<String> UserRestriction.name
     */
    @HasPermission(oneOfPermission = { Permission.ASSIGN_REMOVE_PERMISSION, Permission.PERMISSION_DELEGATION })
    public List<String> getAllUserRestrictionNames() {
        return permissionRepository.getAllUserRestrictionNames();
    }

    /**
     * Returns a cached list of all Restrictions assigned to a specific UserRestriction (used by REST)
     *
     * @return List<RestrictionEntity> for the logged in user
     */
    @HasPermission(oneOfPermission = { Permission.ASSIGN_REMOVE_PERMISSION, Permission.PERMISSION_DELEGATION })
    public List<RestrictionEntity> getRestrictionsForLoggedInUser() {
        return permissionService.getUserRestrictionsForLoggedInUser();
    }

    /**
     * Returns a cached list of all Restrictions assigned to a specific UserRestriction (used by REST)
     *
     * @param userName the specific User name - if omitted, the name of the logged in user is used instead
     * @return List<RestrictionEntity>
     */
    @HasPermission(oneOfPermission = { Permission.ASSIGN_REMOVE_PERMISSION, Permission.PERMISSION_DELEGATION })
    public List<RestrictionEntity> getRestrictionsByUserName(String userName) {
        return permissionService.getUserRestrictions(userName);
    }

    /**
     * Returns a list of all Restrictions assigned to a specific Role (used by REST)
     *
     * @return List<RestrictionEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<RestrictionEntity> getRestrictionsByRoleName(String roleName) {
        return permissionRepository.getRoleWithRestrictions(roleName);
    }

    /**
     * Returns a list of all Roles (used by REST)
     *
     * @return List<RoleEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<RoleEntity> getAllRoles() {
        return permissionRepository.getAllRoles();
    }

    /**
     * Removes a role with all it's permissions
     *
     * @return List<RoleEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public void deleteRole(String roleName, boolean reload) {
        permissionRepository.deleteRole(roleName);
        if (reload) {
            reloadCache();
        }
    }

    /**
     * Returns a list of all PermissionEntities (used by REST)
     *
     * @return List<PermissionEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<PermissionEntity> getAllAvailablePermissions() {
        return permissionRepository.getAllPermissions();
    }

    /**
     * Returns a list of all Permissions, that can be assigned by a user (permission delegation)
     *
     * @return List<PermissionEntity>
     */
    @HasPermission(permission = Permission.PERMISSION_DELEGATION)
    public List<PermissionEntity> getAllUserAssignablePermissions() {
        List<PermissionEntity> assignablePermissions = new ArrayList<>();
        for (RestrictionEntity restriction : permissionService.getAllCallerRestrictions()) {
            assignablePermissions.add(restriction.getPermission());
        }
        return assignablePermissions;
    }

    /**
     * Returns a list of all Permissions of the calling user
     *
     * @return List<PermissionEntity>
     */
    public List<RestrictionEntity> getAllCallerRestrictions() {
        return permissionService.getAllCallerRestrictions();
    }

    protected void validateRestriction(String roleName, String userName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     ResourceTypePermission resourceTypePermission, String contextName, Action action,
                                     RestrictionEntity restriction) throws AMWException {
        if (roleName == null && userName == null) {
            throw new AMWException("Either a Role- or UserName is mandatory");
        }

        if (roleName != null) {
            if (!isValidName(roleName)) {
                throw new AMWException("RoleName must not contain leading or trailing spaces.");
            }
            RoleEntity role = permissionRepository.getRoleByName(roleName);
            if (role != null) {
                restriction.setRole(role);
            } else {
                restriction.setRole(permissionRepository.createRole(roleName));
            }
        }

        if (userName != null) {
            if (!isValidName(userName)) {
                throw new AMWException("UserName must not contain leading or trailing spaces.");
            }
            UserRestrictionEntity userRestriction = permissionRepository.getUserRestrictionByName(userName);
            if (userRestriction != null) {
                restriction.setUser(userRestriction);
            } else {
                restriction.setUser(permissionRepository.createUserRestriciton(userName));
            }
        }

        if (permissionName != null) {
            PermissionEntity permission = permissionRepository.getPermissionByName(permissionName);
            if (permission == null) {
                throw new AMWException("Permission " + permissionName +  " not found.");
            }
            restriction.setPermission(permission);
            if (Permission.valueOf(permission.getValue()).isOld()) {
                resourceTypePermission = null;
                resourceGroupId = null;
                resourceTypeName = null;
                contextName = null;
                action = null;
            }
        } else {
            throw new AMWException("Missing PermissionName");
        }

        if (resourceTypePermission == null || resourceTypePermission.equals(ResourceTypePermission.ANY)) {
            if (resourceGroupId != null && resourceTypeName != null) {
                throw new AMWException("Only ResourceGroup OR ResourceType must be set");
            }
        } else if (resourceGroupId != null || resourceTypeName!= null) {
            throw new AMWException("ResourceGroup AND ResourceType must not be set if ResourceTypePermission is not ANY");
        }

        ResourceGroupEntity resourceGroup = null;
        if (resourceGroupId != null) {
            resourceGroup = resourceGroupRepository.find(resourceGroupId);
            if (resourceGroup == null) {
                throw new AMWException("ResourceGroup with id " + resourceGroupId +  " not found.");
            }
        }
        restriction.setResourceGroup(resourceGroup);

        ResourceTypeEntity resourceType = null;
        if (resourceTypeName != null) {
            resourceType = resourceTypeRepository.getByName(resourceTypeName);
            if (resourceType == null) {
                throw new AMWException("ResourceType " + resourceTypeName +  " not found.");
            }
        }
        restriction.setResourceType(resourceType);

        if (resourceTypePermission != null) {
            restriction.setResourceTypePermission(resourceTypePermission);
        }

        if (contextName != null) {
            try {
                restriction.setContext(contextLocator.getContextByName(contextName));
            } catch (Exception e) {
                throw new AMWException("Context " + contextName +  " not found.");
            }
        } else {
            restriction.setContext(null);
        }

        if (action != null) {
            restriction.setAction(action);
        } else {
            restriction.setAction(Action.ALL);
        }
    }

    public String getUserName() {
        return permissionService.getCurrentUserName();
    }

    @HasPermission(permission = Permission.RESOURCE, action = Action.DELETE, resourceSpecific = true)
    public void removeAllRestrictionsForResourceGroup(ResourceGroupEntity resourceGroup) {
        restrictionRepository.deleteAllWithResourceGroup(resourceGroup);
    }

    protected boolean isValidName(String rawString) {
        if (rawString != null) {
            int rawLength = rawString.length();
            int trimmedLength = rawString.trim().length();
            return rawLength == trimmedLength;
        }
        return false;
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public void reloadCache() {
        permissionService.reloadCache();
    }
}
