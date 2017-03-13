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

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionRepository;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.control.RestrictionRepository;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.common.exception.CheckedNotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * ALL boundary for checking permissions of view elements
 */
@Stateless
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
    ResourceTypeProvider resourceTypeProvider;

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin: shakedown_admin: can edit all
     * properties when TestingMode is true config_admin: can edit all properties. app_developer: can edit
     * properties of instances of APPLICATION
     *
     * @param resourceId
     * @param isTestingMode
     * @return
     */
    public boolean hasPermissionToEditPropertiesByResource(Integer resourceId, boolean isTestingMode) {
        ResourceEntity resource = entityManager.find(ResourceEntity.class, resourceId);
        return permissionService.hasPermissionToEditPropertiesByResource(resource, isTestingMode);
    }

    /**
     * Check that the user is app_developer or config_admin: app_developer: can edit properties of instances
     * of APPLICATION config_admin: can edit all properties.
     *
     * @param
     * @return
     */
    public boolean hasPermissionToEditPropertiesOfResource(Integer parentResourceTypeIdOfResource) {

        ResourceTypeEntity resourceTypeEntity = entityManager.find(ResourceTypeEntity.class,
                parentResourceTypeIdOfResource);
        return permissionService.hasPermissionToEditPropertiesOfResource(resourceTypeEntity);

    }

    public boolean hasPermission(String permissionName) {
        Permission permission = Permission.valueOf(permissionName);
        return permission != null && permissionService.hasPermission(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissionService.hasPermission(permission);
    }

    /**
     * Checks if given permission is available. If not a checked exception is thrown with error message
     * containing extraInfo part.
     * Replace with annotations - do not handle business permission exceptions on gui!!!
     *
     * @param permission
     * @param extraInfo
     */
    @Deprecated
    public void checkPermissionAndFireCheckedException(Permission permission, String extraInfo)
            throws CheckedNotAuthorizedException {
        permissionService.checkPermissionAndFireCheckedException(permission, extraInfo);
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
     * The ResourceName is modifiable by the config_admin or by the server_admin when the resource is
     * instance's resource of deaultResourceType: APPLICATION/APPLICATIONSERVER/NODE's instance
     *
     * @param res
     * @return
     */
    public boolean hasPermissionToRenameResource(ResourceEntity res) {
        ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, res.getId());
        return permissionService.hasPermissionToRenameResource(mergedResource);
    }

    /**
     * The ResourceTypeName is modifiable by the config_admin. DefaultResourceType (APPLICATION,
     * APPLICATIONSERVER or NODE) is not modifiable.
     */
    public boolean hasPermissionToRenameResourceType(ResourceTypeEntity resType) {
        ResourceTypeEntity mergedResourceType = entityManager.find(ResourceTypeEntity.class, resType.getId());
        return permissionService.hasPermissionToRenameResourceType(mergedResourceType);
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

    public boolean hasPermissionToRemoveDefaultInstanceOfResType(boolean isDefaultResourceType) {
        return permissionService.hasPermissionToRemoveDefaultInstanceOfResType(isDefaultResourceType);
    }

    /**
     * Check that the user is config_admin, app_developer or shakedown_admin : shakedown_admin: can
     * modify(add/edit/delete) all testing templates config_admin: can modify(add/edit/delete) all templates
     * app_developer: can modify(add/edit/delete) only templates in instances of APPLICATION
     */
    public boolean hasPermissionToTemplateModify(Identifiable resourceOrResourceTypeEntity, boolean isTestingMode) {
        if (resourceOrResourceTypeEntity != null) {
            if (resourceOrResourceTypeEntity instanceof ResourceEntity) {
                ResourceEntity mergedResource = entityManager.find(ResourceEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToTemplateModify(mergedResource, isTestingMode);
            } else if (resourceOrResourceTypeEntity instanceof ResourceTypeEntity) {
                ResourceTypeEntity mergedResourceType = entityManager.find(ResourceTypeEntity.class, resourceOrResourceTypeEntity.getId());
                return permissionService.hasPermissionToTemplateModify(mergedResourceType, isTestingMode);
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

        // Abwärtskompatibilität: ADD_NEW_RES_OF_DEFAULT_RESTYPE

        if (type.isApplicationResourceType()) {
            return permissionService.hasPermission(Permission.ADD_APP) || permissionService.hasPermission(Permission.ADD_NEW_RES_OF_DEFAULT_RESTYPE);
        }
        if (type.isNodeResourceType()) {
            return permissionService.hasPermission(Permission.ADD_NODE) || permissionService.hasPermission(Permission.ADD_NEW_RES_OF_DEFAULT_RESTYPE);
        }
        if (type.isApplicationServerResourceType()) {
            return permissionService.hasPermission(Permission.ADD_APPSERVER) || permissionService.hasPermission(Permission.ADD_NEW_RES_OF_DEFAULT_RESTYPE);
        }

        return permissionService.hasPermission(Permission.NEW_RES);
    }

    public boolean canCreateAppAndAddToAppServer() {
        return permissionService.hasPermission(Permission.ADD_APP) && permissionService.hasPermission(Permission.ADD_APP_TO_APP_SERVER);
    }

    /**
     * @param resourceEntity
     * @return
     */
    public boolean canCopyFromResource(ResourceEntity resourceEntity) {
        if (resourceEntity == null || resourceEntity.getResourceType() == null) {
            return false;
        }
        if (resourceEntity.getResourceType().isApplicationServerResourceType()) {
            return permissionService.hasPermission(Permission.COPY_FROM_RESOURCE_APPSERVER);
        }
        if (resourceEntity.getResourceType().isApplicationResourceType()) {
            return permissionService.hasPermission(Permission.COPY_FROM_RESOURCE_APP);
        }
        if (resourceEntity.getResourceType().isNodeResourceType()) {
            return permissionService.hasPermission(Permission.COPY_FROM_RESOURCE_NODE);
        }

        return permissionService.hasPermission(Permission.COPY_FROM_RESOURCE);

    }

    public boolean hasPermissionToDeploy() {
        return permissionService.hasPermissionToDeploy();
    }

    /**
     * Creates a new RestrictionEntity and returns its id
     *
     * @param role
     * @param permission
     * @param context
     * @param action
     * @return Id of the newly created RestrictionEntity
     */
    public Integer createRestriction(RoleEntity role, PermissionEntity permission, ContextEntity context, Action action) {
        return restrictionRepository.create(role, permission, context, action);
    }

    public RoleEntity getRoleByName(String roleName) {
        return permissionRepository.getRoleByName(roleName);
    }

    public PermissionEntity getPermissionByName(String permissionName) {
        return permissionRepository.getPermissionByName(permissionName);
    }

    public RestrictionEntity findRestriction(Integer id) {
        return restrictionRepository.find(id);
    }

    public List<RestrictionEntity> findAll() {
        return restrictionRepository.findAll();
    }
}
