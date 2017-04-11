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
        ResourceTypeEntity resourceType = entityManager.find(ResourceTypeEntity.class, resourceTypeId);
        ContextEntity context = contextLocator.getContextById(contextId);
        if (permissionService.hasPermission(Permission.RESOURCETYPE, context, Action.UPDATE, null, resourceType)) {
            return true;
        }
        return permissionService.hasPermission(Permission.SHAKEDOWN_TEST_MODE) && isTestingMode;
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
        ResourceEntity resource = entityManager.find(ResourceEntity.class, resourceId);
        if (permissionService.hasPermission(Permission.RESOURCE, context, Action.UPDATE, resource.getResourceGroup(), null)) {
            return true;
        }
        return permissionService.hasPermission(Permission.SHAKEDOWN_TEST_MODE) && isTestingMode;
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
    public void checkPermissionActionAndFireException(Permission permission, Action action, String extraInfo) {
        permissionService.checkPermissionActionAndFireException(permission, action, extraInfo);
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can delete node
     * relationship config_admin: can delete all relationship. app_developer: can delete reletionship of
     * instances of APPLICATION
     */
    public boolean hasPermissionToDeleteRelation(ResourceEntity resourceEntity) {
        ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceEntity.getId());
        return permissionService.hasPermissionToDeleteRelation(mergedResource);
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can delete node
     * relationship config_admin: can delete all relationship. app_developer: can delete reletionship of
     * instances of APPLICATION
     */
    public boolean hasPermissionToDeleteRelationType(ResourceTypeEntity resourceTypeEntity) {
        ResourceTypeEntity mergedResource = entityManager.find(ResourceTypeEntity.class, resourceTypeEntity.getId());
        return permissionService.hasPermissionToDeleteRelationType(mergedResource);
    }

    /**
     * Check that the user is config_admin, server_admin or app_developer : server_admin: can add node
     * relationship config_admin: can add all relationship. app_developer: can add reletionship of instances
     * of APPLICATION
     */
    public boolean hasPermissionToAddRelation(ResourceEntity resourceEntity, boolean isProvided) {
        ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceEntity.getId());
        return permissionService.hasPermissionToAddRelation(mergedResource, isProvided);
    }

    public boolean hasPermissionToRemoveInstanceOfResType(ResourceTypeEntity resourceType) {
        return permissionService.hasPermissionToRemoveInstanceOfResType(resourceType);
    }

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin : shakedown_admin: can
     * modify(add/edit/delete) all testing templates config_admin: can modify(add/edit/delete) all templates
     * app_developer: can modify(add/edit/delete) only templates in instances of APPLICATION
     */
    public boolean hasPermissionToTemplateModify(Identifiable resourceOrResourceTypeEntity, ContextEntity context, boolean isTestingMode) {
        if (resourceOrResourceTypeEntity != null) {
            if (resourceOrResourceTypeEntity instanceof ResourceEntity) {
                ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToModifyResourceTemplate(mergedResource, context, isTestingMode);
            } else if (resourceOrResourceTypeEntity instanceof ResourceTypeEntity) {
                ResourceTypeEntity mergedResourceType = entityManager.find(ResourceTypeEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToModifyResourceTypeTemplate(mergedResourceType, context, isTestingMode);
            }
        }
        return false;
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
        return permissionService.hasPermission(Permission.RESOURCE, Action.CREATE, resource.getResourceType()) && permissionService.hasPermission(Permission.ADD_APP_TO_APP_SERVER);
    }

    /**
     * @param resourceEntity
     * @return
     */
    public boolean canCopyFromResource(ResourceEntity resourceEntity) {
        return !(resourceEntity == null || resourceEntity.getResourceType() == null) && permissionService.hasPermission(Permission.COPY_FROM_RESOURCE, null, Action.UPDATE, resourceEntity.getResourceGroup(), resourceEntity.getResourceType());
    }

    public boolean hasPermissionToDeploy() {
        return permissionService.hasPermissionToDeploy();
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
    public Integer createRestriction(String roleName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     String contextName, Action action) throws AMWException {
        RestrictionEntity restriction = new RestrictionEntity();
        validateRestriction(roleName, permissionName, resourceGroupId, resourceTypeName, contextName, action, restriction);
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
    public void updateRestriction(Integer id, String roleName, String permissionName, Integer resourceId,
                                  String resourceTypeName,String contextName, Action action) throws AMWException {
        if (id == null) {
            throw new AMWException("Id must not be null");
        }
        RestrictionEntity restriction = restrictionRepository.find(id);
        if (restriction == null) {
            throw new AMWException("Restriction not found");
        }
        validateRestriction(roleName, permissionName, resourceId, resourceTypeName, contextName, action, restriction);
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
     * Returns all available roles with their restrictions
     *
     * @return Map key=Role.name, value=restrictionDTOs
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public Map<String, List<RestrictionDTO>> getAllPermissions() {
        return permissionService.getPermissions();
    }

    private void validateRestriction(String roleName, String permissionName, Integer resourceGroupId, String resourceTypeName,
                                     String contextName, Action action, RestrictionEntity restriction) throws AMWException {
        if (roleName != null) {
            try {
                restriction.setRole(permissionRepository.getRoleByName(roleName));
            } catch (NoResultException ne) {
                throw new AMWException("Role " + roleName +  " not found.");
            }
        } else {
            throw new AMWException("Missing RoleName");
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

        if (resourceGroupId != null) {
            ResourceGroupEntity resourceGroup = resourceGroupRepository.find(resourceGroupId);
            if (resourceGroup == null) {
                throw new AMWException("ResourceGroup with id " + resourceGroupId +  " not found.");
            }
            restriction.setResourceGroup(resourceGroup);
        }

        if (resourceTypeName != null) {
            ResourceTypeEntity resourceType = resourceTypeRepository.getByName(resourceTypeName);
            if (resourceType == null) {
                throw new AMWException("ResourceType " + resourceTypeName +  " not found.");
            }
            restriction.setResourceType(resourceType);
        }

        if (contextName != null) {
            try {
                restriction.setContext(contextLocator.getContextByName(contextName));
            } catch (Exception e) {
                throw new AMWException("Context " + contextName +  " not found.");
            }
        }

        if (action != null) {
            restriction.setAction(action);
        } else {
            restriction.setAction(Action.ALL);
        }
    }

}
