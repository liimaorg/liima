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
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A boundary for checking permissions of view elements
 */
@Stateless
@Interceptors(HasPermissionInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceTypeAndContext(Integer resourceTypeId, Integer contextId,
                                                                         boolean isTestingMode) {
        if (!isTestingMode) {
            ResourceTypeEntity resourceType = entityManager.find(ResourceTypeEntity.class, resourceTypeId);
            ContextEntity context = contextId == null ? null : contextLocator.getContextById(contextId);
            return permissionService.hasPermission(Permission.RESOURCETYPE, context, Action.UPDATE, null, resourceType);
        }
        // so we must be in TestingMode
        return permissionService.hasPermission(Permission.SHAKEDOWN_TEST_MODE);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a ResourceType without checking its Context
     *
     * @param resourceTypeId
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceType(Integer resourceTypeId, boolean isTestingMode) {
        return hasPermissionToEditPropertiesByResourceTypeAndContext(resourceTypeId, null, isTestingMode);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a Resource in a specific Context
     *
     * @param resourceId
     * @param context
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceAndContext(Integer resourceId, ContextEntity context,
                                                                     boolean isTestingMode) {
        if (!isTestingMode) {
            ResourceEntity resource = entityManager.find(ResourceEntity.class, resourceId);
            return permissionService.hasPermission(Permission.RESOURCE, context, Action.UPDATE,
                    resource.getResourceGroup(), null);
        }
        // so we must be in TestingMode
        return permissionService.hasPermission(Permission.SHAKEDOWN_TEST_MODE);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a Resource in a specific Context
     *
     * @param resourceId
     * @param contextId
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResourceAndContext(Integer resourceId, boolean isTestingMode,
                                                                     Integer contextId) {
        ContextEntity context = contextLocator.getContextById(contextId);
        return hasPermissionToEditPropertiesByResourceAndContext(resourceId, context, isTestingMode);
    }

    /**
     * Checks if the user is allowed to edit the Properties of a Resource without checking its Context
     *
     * @param resourceId
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResource(Integer resourceId, boolean isTestingMode) {
        return hasPermissionToEditPropertiesByResourceAndContext(resourceId, null, isTestingMode);
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

    public boolean hasPermissionToRemoveInstanceOfResType(ResourceTypeEntity resourceType) {
        return permissionService.hasPermissionToRemoveInstanceOfResType(resourceType);
    }

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin : shakedown_admin: can
     * modify(add/edit/delete) all testing templates config_admin: can modify(add/edit/delete) all templates
     * app_developer: can modify(add/edit/delete) only templates in instances of APPLICATION
     */
    public boolean hasPermissionToAddTemplate(Identifiable resourceOrResourceTypeEntity, boolean isTestingMode) {
        if (resourceOrResourceTypeEntity != null) {
            if (resourceOrResourceTypeEntity instanceof ResourceEntity) {
                ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToAddResourceTemplate(mergedResource, isTestingMode);
            }
            if (resourceOrResourceTypeEntity instanceof ResourceTypeEntity) {
                ResourceTypeEntity mergedResourceType = entityManager.find(ResourceTypeEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToAddResourceTypeTemplate(mergedResourceType, isTestingMode);
            }
        }
        return false;
    }

    public boolean canEditFunctionOfResourceOrResourceType(Integer resourceEntityId, Integer resourceTypeEntityId) {
        // context is always global
        if (resourceEntityId != null) {
            ResourceEntity resource = resourceRepository.find(resourceEntityId);
            return permissionService.hasPermission(Permission.RESOURCE, null, Action.UPDATE, resource.getResourceGroup(), null) &&
                    permissionService.hasPermission(Permission.RESOURCE_AMWFUNCTION, null, Action.UPDATE, resource.getResourceGroup(), null);
        }
        ResourceTypeEntity type = resourceTypeRepository.find(resourceTypeEntityId);
        return permissionService.hasPermission(Permission.RESOURCETYPE, null, Action.UPDATE, null, type) &&
                permissionService.hasPermission(Permission.RESOURCETYPE_AMWFUNCTION, null, Action.UPDATE, null, type);
    }

    public boolean canCreateResourceInstance(DefaultResourceTypeDefinition type) {
        return canCreateResourceInstance(resourceTypeProvider.getOrCreateDefaultResourceType(type));
    }

    /**
     * @param type
     * @return
     */
    public boolean canCreateResourceInstance(ResourceTypeEntity type) {
        return permissionService.hasPermission(Permission.RESOURCE, Action.CREATE, type);
    }

    public boolean canCreateAppAndAddToAppServer(ResourceEntity resource) {
        return permissionService.hasPermission(Permission.RESOURCE, Action.CREATE, resource.getResourceType()) &&
                permissionService.hasPermission(Permission.RESOURCE, Action.UPDATE, resourceTypeProvider.getOrCreateDefaultResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER));
    }

    /**
     * @param resourceEntity
     * @return
     */
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
                permissionService.hasPermission(Permission.RESOURCE_RELEASE_COPY_FROM_RESOURCE, null, Action.ALL, targetResourceGroup, targetResourceGroup.getResourceType())
                && permissionService.hasPermission(Permission.RESOURCE, null, Action.READ, originResource.getResourceGroup(), originResource.getResourceType());
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

    public boolean hasPermissionToCreateShakedownTests(Integer resourceGroupId) {
        ResourceGroupEntity resourceGroupEntity = resourceGroupRepository.find(resourceGroupId);
        return permissionService.hasPermission(Permission.SHAKEDOWNTEST, null, Action.CREATE, resourceGroupEntity, null);
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public RestrictionEntity findRestriction(Integer id) {
        return restrictionRepository.find(id);
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<RestrictionEntity> findAllRestrictions() {
        return restrictionRepository.findAll();
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
     * @return Id of the newly created RestrictionEntity
     * @throws AMWException
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION, action = Action.CREATE)
    public Integer createRestriction(String roleName, String userName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     ResourceTypePermission resourceTypePermission, String contextName, Action action)
            throws AMWException {
        RestrictionEntity restriction = new RestrictionEntity();
        validateRestriction(roleName, userName, permissionName, resourceGroupId, resourceTypeName, resourceTypePermission,
                contextName, action, restriction);
        final Integer id = restrictionRepository.create(restriction);
        permissionRepository.forceReloadingOfLists();
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
     * @throws AMWException
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION, action = Action.UPDATE)
    public void updateRestriction(Integer id, String roleName, String userName, String permissionName, Integer resourceId,
                                  String resourceTypeName, ResourceTypePermission resourceTypePermission,
                                  String contextName, Action action) throws AMWException {
        if (id == null) {
            throw new AMWException("Id must not be null");
        }
        RestrictionEntity restriction = restrictionRepository.find(id);
        if (restriction == null) {
            throw new AMWException("Restriction not found");
        }
        validateRestriction(roleName, userName, permissionName, resourceId, resourceTypeName, resourceTypePermission,
                contextName, action, restriction);
        restrictionRepository.merge(restriction);
        permissionRepository.forceReloadingOfLists();
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION, action = Action.DELETE)
    public void removeRestriction(Integer id) throws AMWException {
        RestrictionEntity restriction = restrictionRepository.find(id);
        if (restriction == null) {
            throw new AMWException("Restriction not found");
        }
        restrictionRepository.remove(id);
        permissionRepository.forceReloadingOfLists();
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
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<String> getAllUserRestrictionNames() {
        return permissionRepository.getAllUserRestrictionNames();
    }

    /**
     * Returns a cached list of all Restrictions assigned to a specific UserRestriction (used by REST)
     *
     * @return List<RestrictionEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
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
     * Returns a list of all PermissionEntities (used by REST)
     *
     * @return List<PermissionEntity>
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public List<PermissionEntity> getAllAvailablePermissions() {
        return permissionRepository.getAllPermissions();
    }

    private void validateRestriction(String roleName, String userName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     ResourceTypePermission resourceTypePermission, String contextName, Action action,
                                     RestrictionEntity restriction) throws AMWException {
        if (roleName == null && userName == null) {
            throw new AMWException("Either a Role- or UserName is mandatory");
        }

        if (roleName != null) {
            if (roleName.trim().isEmpty()) {
                throw new AMWException("RoleName must not be empty.");
            }
            RoleEntity role = permissionRepository.getRoleByName(roleName);
            if (role != null) {
                restriction.setRole(role);
            } else {
                restriction.setRole(permissionRepository.createRole(roleName));
            }
        }

        if (userName != null) {
            if (userName.trim().isEmpty()) {
                throw new AMWException("UserName must not be empty.");
            }
            UserRestrictionEntity userRestriction = permissionRepository.getUserRestrictionByName(userName);
            if (userRestriction != null) {
                restriction.setUser(userRestriction);
            } else {
                restriction.setUser(permissionRepository.createUserRestriciton(userName));
            }
        }

        if (permissionName != null) {
            try {
                restriction.setPermission(permissionRepository.getPermissionByName(permissionName));
            } catch (NoResultException ne) {
                throw new AMWException("Permission " + permissionName +  " not found.");
            }
        } else {
            throw new AMWException("Missing PermissionName");
        }

        if (resourceTypePermission == null || resourceTypePermission.equals(ResourceTypePermission.ANY)) {
            if (resourceGroupId != null && resourceTypeName!= null) {
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

}
