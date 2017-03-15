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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.RenameException;
import ch.puzzle.itc.mobiliar.common.exception.RoleNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.RolePermissionContainer;

/**
 * @author aromano
 * 
 */
@Interceptors(HasPermissionInterceptor.class)
@Stateless
public class SecurityScreenDomainService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager entityManager;
    
    @Inject
    private PermissionRepository permissionRepository;


    /**
     * Diese Methode liest alle Rollen in der Datenbank durch eine Abfrage
     * 
     * @return List<RoleEntity>
     */
    public List<RoleEntity> getAllRole() {
        
        List<RoleEntity> roles = new ArrayList<RoleEntity>();
        
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleEntity> r = cb.createQuery(RoleEntity.class);
            Root<RoleEntity> root = r.from(RoleEntity.class);
            r.orderBy(cb.asc(root.get("name").as(String.class)));
            roles = entityManager.createQuery(r).getResultList();
        } catch (NoResultException nre) {
            String message = "Keine Rolle existiert auf der DB";
            log.log(Level.WARNING, message);
        } 
        
        return roles;
    }

    /**
     * Diese Methode sucht einer Rolle durch seinen Namen
     * 
     * @param roleName
     * @return roleEntity
     */
    public RoleEntity getUniqueRoleByName(String roleName) {
        RoleEntity roleEntity = null;
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleEntity> r = cb.createQuery(RoleEntity.class);
            Root<RoleEntity> root = r.from(RoleEntity.class);
            r.where(cb.equal(root.get("name"), roleName));
            roleEntity = entityManager.createQuery(r).getSingleResult();
        } catch (NoResultException nre) {
            String message = "Die Rolle: " + roleName
                    + " existiert nicht auf der DB";
            log.log(Level.WARNING, message);
        } 
        return roleEntity;
    }

    /**
     * Diese Methode sucht einer Rollen durch einen ID
     * 
     * @param id
     * @return roleEntity
     * @throws RoleNotFoundException
     */
    public RoleEntity getRoleById(int id) throws RoleNotFoundException {
        RoleEntity roleEntity = entityManager.find(RoleEntity.class, id);
        if (roleEntity == null) {
            String message = "Die Rolle mit der Id: " + id
                    + " existiert nicht auf der DB";
            log.info(message);
            throw new RoleNotFoundException(message);
        }
        return roleEntity;
    }

    /**
     * Diese Methode löscht eine Rolle. Er prüft zuerst ob die Rolle existiert
     * dann weist die Berechtigungen einen Default container zu
     * 
     * @param id
     * @throws RoleNotFoundException
     */
    @HasPermission(permission = Permission.DELETE_ROLE)
    public void deleteRoleById(int id) throws RoleNotFoundException {
        RoleEntity roleEntity = getRoleById(id);
        if (roleEntity == null) {
            String message = "Die zu löeschende Role ist nicht vorhanden";
            log.info(message);
            throw new RoleNotFoundException(message);
        }
        List<PermissionEntity> permissions = getPermissionListByRoleId(roleEntity
                .getId());
        if (permissions.size() > 0) {
            RoleEntity containerPermissionRole = createOrGetPermissionWithoutAssignedRole();
            for (PermissionEntity p : permissions) {
                p.getRoles().add(containerPermissionRole);
            }
        }

        entityManager.remove(roleEntity);
        permissionRepository.setReloadDeployableRoleList(true);
        log.info("Role mit der Id: " + id + " wurde aus der DB gelöscht");
        
    }

    /**
     * Diese Methode sucht eine Berechtigung durch einen Name
     * 
     * @param permissionValue
     * @return permissionEntity
     */
    public PermissionEntity getUniquePermissionByName(String permissionValue) {
        PermissionEntity permissionEntity = null;
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<PermissionEntity> p = cb
                    .createQuery(PermissionEntity.class);
            Root<PermissionEntity> root = p.from(PermissionEntity.class);
            p.where(cb.equal(root.get("value"), permissionValue));
            permissionEntity = entityManager.createQuery(p).getSingleResult();
        } catch (NoResultException nre) {
            String message = "Die Erlaubnis: " + permissionValue
                    + " existiert nicht auf der DB";
            log.log(Level.WARNING, message);
        }
        return permissionEntity;
    }

    public List<PermissionEntity> getAllPermissions() {
        
        List<PermissionEntity> permissions = new ArrayList<PermissionEntity>();
        
        try {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<PermissionEntity> r = cb
                        .createQuery(PermissionEntity.class);
                Root<PermissionEntity> root = r.from(PermissionEntity.class);
                r.orderBy(cb.asc(root.get("value").as(String.class)));
                permissions = entityManager.createQuery(r).getResultList();
                permissions = Collections.unmodifiableList(permissions);
        } catch (NoResultException nre) {
            String message = "Keine Erlaubnisse existiert auf der DB";
            log.log(Level.WARNING, message);
        } 
        
        return permissions;
    }

    /**
     * Diese Methode sucht eine Berechtigung durch einen ID
     * 
     * @param permissionId
     * @return
     */
    public PermissionEntity getUniquePermissionById(Integer permissionId) {
        PermissionEntity permissionEntity = null;
        try {
            permissionEntity = entityManager.find(PermissionEntity.class,
                    permissionId);
            if (permissionEntity == null) {
                String message = "Die Erlaubnis mit der id:" + permissionId
                        + " ist nicht vorhanden";
                log.info(message);
            }
        } catch (NoResultException nre) {
            String message = "Die Erlaubnis mit der Id: " + permissionId
                    + " existiert nicht auf der DB";
            log.log(Level.WARNING, message);
        } 
        
        return permissionEntity;
    }

    /**
     * Diese Methode erstellt eine neue Rolle durch einen Name. Eine Rolle kann
     * Deployable oder nicht sein
     * 
     * @param roleName
     * @param isDeployable
     * @return
     * @throws ElementAlreadyExistsException
     */
    @HasPermission(permission = Permission.CREATE_ROLE)
    public RoleEntity createRoleByName(String roleName, boolean isDeployable)
            throws ElementAlreadyExistsException {
        RoleEntity newRoleEntity = getUniqueRoleByName(roleName);
        RoleEntity result = null;
        if (newRoleEntity == null) {
            result = new RoleEntity();
            result.setName(roleName);
            result.setDeployable(isDeployable);
            result.setDeletable(true);
            entityManager.persist(result);
            log.info("Rolle " + roleName + " in DB persist");
            permissionRepository.setReloadDeployableRoleList(true);
        } else {
            String message = "Die Rolle mit dem Namen: " + roleName
                    + " ist bereits vorhanden und kann nicht erstellen werden";
            log.info(message);
            throw new ElementAlreadyExistsException(message, RoleEntity.class,
                    roleName);
        }
        return result;
    }

    /**
     * Diese Methode erstellt eine neue Berechtigung für eine Rolle. Diese
     * Berechtigung ist eine Deployberechtigung.
     * 
     * @param permissionValue
     * @param roleId
     * @param context
     * @return
     * @throws RoleNotFoundException
     * @throws ElementAlreadyExistsException
     */
    public PermissionEntity createPermissionForRole(String permissionValue,
            Integer roleId, ContextEntity context)
            throws RoleNotFoundException, ElementAlreadyExistsException {
        RoleEntity role = getRoleById(roleId);
        PermissionEntity p = getUniquePermissionByName(permissionValue);

        PermissionEntity permissionEntity = null;
        if (role == null) {
            String errorMessage = "Die rolle mir der Id: " + roleId
                    + " existiert nicht auf der DB";
            log.log(Level.WARNING, errorMessage);
            throw new RoleNotFoundException(errorMessage);
        } else if (p != null) {
            String message = "Die Erlaubnis mit dem Namen: " + permissionValue
                    + " ist bereits vorhanden und kann nicht erstellen werden";
            log.info(message);
            throw new ElementAlreadyExistsException(message,
                    PermissionEntity.class, permissionValue);
        } else if (role != null && p == null) {
            permissionEntity = new PermissionEntity();
            permissionEntity.setValue(permissionValue);
            permissionEntity.setContext(context);
            entityManager.persist(permissionEntity);
            role.getPermissions().add(permissionEntity);
            entityManager.persist(role);
            log.info("Die Erlaubnis " + permissionValue + " für die Rolle "
                    + role.getName() + " in DB Persist.");

            permissionRepository.setReloadRolesAndPermissionsList(true);
        }
        return permissionEntity;
    }

    public void renameRole(int id, String roleName)
            throws RoleNotFoundException, RenameException {
        RoleEntity roleEntity = getRoleById(id);
        RoleEntity roleByName = getUniqueRoleByName(roleName);

        if (roleByName != null
                && !roleByName.getId().equals(roleEntity.getId())) {
            String message = "Der Name wird bereits für eine Rolle verwendet.";
            throw new RenameException(message);
        }
        roleEntity.setName(roleName);
    }

    /**
     * Diese Methode benennt eine Berechtigung um
     * 
     * @param permissionValue
     * @param newPermissionValue
     */
    public void renamePermissionByName(String permissionValue,
            String newPermissionValue)  {
        PermissionEntity permissionEntity;

            permissionEntity = getUniquePermissionByName(permissionValue);
            if (permissionEntity == null) {
                String message = "Die Erlaubnis mit dem Name:"
                        + permissionValue + " ist nicht vorhanden";
                log.info(message);
            }
            if (permissionEntity != null) {
                permissionEntity.setValue(newPermissionValue);
            }

    }

    /**
     * Diese Methode sucht die Berechtigungen von eine Rolle durch seine ID.
     * 
     * @param roleId
     * @return
     */
    public List<PermissionEntity> getPermissionListByRoleId(Integer roleId) {
        List<PermissionEntity> result = null;
        try {
            result = entityManager
                    .createQuery(
                            "select p from PermissionEntity p join p.roles r left join fetch p.context c left join fetch c.contextType left join fetch c.parent where r.id=:roleId order by p.value", PermissionEntity.class)
                    .setParameter("roleId", roleId).getResultList();
        } catch (NoResultException nre) {
            String message = "Die Rolle mit der Id: " + roleId
                    + " existiert nicht auf der DB";
            log.log(Level.WARNING, message);
        }
        return result;
    }

    public List<Object[]> rolesPermissionsList() {
        List<Object[]> result = entityManager
                .createQuery(
                        "select distinct p.value, r.name from PermissionEntity p left join p.roles r")
                .getResultList();
        return result == null ? new ArrayList<Object[]>() : result;
    }

    

    /**
     * Diese Methode sucht die alle Berechtigungen ausser die Berechtigungen von
     * die Rolle mit der ID = roleId
     * 
     * @param roleId
     * @return
     */
    public List<PermissionEntity> getAllPermissionsWithoutPermissionsOfRoleSelected(
            Integer roleId) {
        List<PermissionEntity> result = entityManager
                .createQuery(
                        "select p from PermissionEntity p join p.roles r where r.id!=:roleId order by r.name,p.value", PermissionEntity.class)
                .setParameter("roleId", roleId).getResultList();
        return result == null ? new ArrayList<PermissionEntity>() : result;
    }

    /**
     * Diese Methode weist die Berechtigung mit der Id permissionId einer Rolle
     * mit der Id roleId
     * 
     * @param roleId
     * @param permissionId
     * @return
     */
    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public boolean addPermissionToRole(Integer roleId, Integer permissionId) throws RoleNotFoundException{
        boolean isSuccesfully = false;
        RoleEntity roleEntity = null;
        PermissionEntity permissionEntity = getUniquePermissionById(permissionId);

            if (roleId == 0) {
                roleEntity = createOrGetPermissionWithoutAssignedRole();
                if (roleEntity.getPermissions().contains(permissionEntity)) {
                    isSuccesfully = false;
                } else {
                    roleEntity.getPermissions().add(permissionEntity);
                    entityManager.persist(roleEntity);
                    isSuccesfully = true;
                    permissionRepository.setReloadRolesAndPermissionsList(true);
                }
            } else {
                roleEntity = getRoleById(roleId);
                if (roleEntity.getPermissions().contains(permissionEntity)) {
                    isSuccesfully = false;
                } else {
                    if (permissionEntity != null && roleEntity != null) {
                        roleEntity.getPermissions().add(permissionEntity);
                        entityManager.persist(roleEntity);
                        isSuccesfully = true;
                        permissionRepository.setReloadRolesAndPermissionsList(true);
                    }
                }
            }

        return isSuccesfully;
    }

    @HasPermission(permission = Permission.ASSIGN_REMOVE_PERMISSION)
    public boolean assignPermissionToRole(Integer oldRoleId,
            Integer permissionId, Integer newRoleId) throws RoleNotFoundException {
        boolean isSuccesfully = false;
        RoleEntity oldRole = getRoleById(oldRoleId);
        RoleEntity newRole = null;
        PermissionEntity permissionEntity = getUniquePermissionById(permissionId);

        if (newRoleId == 0) {
            newRole = createOrGetPermissionWithoutAssignedRole();

            // permission must always be assigned to at least one role
            if (permissionEntity.getRoles().size() > 1) {
                oldRole.getPermissions().remove(permissionEntity);
            } else {
                oldRole.getPermissions().remove(permissionEntity);
                newRole.getPermissions().add(permissionEntity);
            }
        } else {
            newRole = getRoleById(newRoleId);
            if (permissionEntity != null && newRole != null
                    && oldRole != null) {
                oldRole.getPermissions().remove(permissionEntity);
                newRole.getPermissions().add(permissionEntity);
            }
        }

        entityManager.persist(oldRole);
        entityManager.persist(newRole);
        isSuccesfully = true;
        permissionRepository.setReloadRolesAndPermissionsList(true);

        return isSuccesfully;
    }

    public static class PermissionToRole {
        private final PermissionEntity permission;
        private final RoleEntity role;

        public PermissionToRole(PermissionEntity permission, RoleEntity role) {
            super();
            this.permission = permission;
            this.role = role;
        }

        public PermissionEntity getPermission() {
            return permission;
        }

        public RoleEntity getRole() {
            return role;
        }
    }

    /**
     * Diese Methode erstellt eine Defaultrolle-Container wenn er noch nicht
     * existiert
     * 
     * @return
     */
    public RoleEntity createOrGetPermissionWithoutAssignedRole() {
        RoleEntity roleEntity = null;
            roleEntity = getUniqueRoleByName(RolePermissionContainer.ROLEPERMISSIONCONTAINER
                    .getDisplayName());
            if (roleEntity == null) {
                roleEntity = new RoleEntity();
                roleEntity
                        .setName(RolePermissionContainer.ROLEPERMISSIONCONTAINER
                                .getDisplayName());
                roleEntity.setDeployable(false);
                roleEntity.setDeletable(false);
                entityManager.persist(roleEntity);

            }

        return roleEntity;
    }

    public RoleEntity getPermissionByRole(Integer roleId, Integer permissionId) {
        return (RoleEntity) entityManager
                .createQuery(
                        "select r from RoleEntity r join r.permissions p where r.id=:roleId and p.id=:permissionId")
                .setParameter("roleId", roleId)
                .setParameter("permissionId", permissionId).getSingleResult();
    }

    /**
     * Gibt eine Liste von Rollen, die der angegebenen Berechtigung enthalten
     * 
     * @param permissionValue
     * @return
     * 
     */
    public List<RoleEntity> getRolesByPermission(String permissionValue) {
        List<RoleEntity> result = entityManager
                .createQuery(
                        "select r from RoleEntity r join r.permissions p where p.value=:permissionValue", RoleEntity.class)
                .setParameter("permissionValue", permissionValue)
                .getResultList();
        return result == null ? new ArrayList<RoleEntity>() : result;
    }

    public void deletePermissionFromRoles(String permissionValue) {

            List<RoleEntity> roles = getRolesByPermission(permissionValue);
            PermissionEntity permissionEntity = null;
            if (roles != null) {
                for (RoleEntity r : roles) {
                    permissionEntity = getUniquePermissionByName(permissionValue);
                    if (r.getPermissions().contains(permissionEntity)
                            && permissionEntity.getRoles().contains(r)) {
                        r.getPermissions().remove(permissionEntity);
                    }
                }
                if (permissionEntity != null) {
                    entityManager.remove(permissionEntity);
                }
            }
    }

    public List<PermissionToRole> permissionAndRole(Integer roleSelectedId) {
        List<PermissionToRole> result = new ArrayList<SecurityScreenDomainService.PermissionToRole>();
            PermissionToRole permissionToRole = null;
            for (RoleEntity role : getAllRole()) {
                if (!role.getId().equals(roleSelectedId)) {
                    for (PermissionEntity permission : getPermissionListByRoleId(role
                            .getId())) {
                        permissionToRole = new PermissionToRole(permission,
                                role);
                        result.add(permissionToRole);
                    }
                }
            }
        return result;
    }

}