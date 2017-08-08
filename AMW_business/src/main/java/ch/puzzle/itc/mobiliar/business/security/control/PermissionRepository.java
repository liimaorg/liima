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
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ch.puzzle.itc.mobiliar.business.security.entity.*;

@Stateless
public class PermissionRepository {
	@Inject
	private EntityManager entityManager;

	private static boolean reloadDeployableRoleList;
	private static boolean reloadRolesAndPermissionsList;
	private static boolean reloadUserRestrictionsList;

	@Schedule(hour = "*", minute = "*/20", persistent = false)
	public void forceReloadingOfLists() {
		reloadDeployableRoleList = true;
		reloadRolesAndPermissionsList = true;
		reloadUserRestrictionsList = true;
	}

	/**
	 * Returns Roles which are allowed to deploy
	 *
	 * @return
	 */
	public List<RoleEntity> getDeployableRoles() {
		return getRolesHavingRestrictionsWithPermissionAndAction(Permission.DEPLOYMENT, Action.CREATE);
	}

	public List<RestrictionEntity> getUserWithRestrictions(String userName) {
		TypedQuery<RestrictionEntity> query = entityManager.createQuery("select r from RestrictionEntity r where LOWER(r.user.name) =:userName", RestrictionEntity.class)
				.setParameter("userName", userName.toLowerCase());
		return query.getResultList();
	}

	public List<RestrictionEntity> getUsersWithRestrictions() {
		return entityManager.createQuery("select r from RestrictionEntity r order by LOWER(r.user.name)", RestrictionEntity.class).getResultList();
	}

	public List<RoleEntity> getRolesWithRestrictions() {
		TypedQuery<RoleEntity> query = entityManager.createQuery("select distinct r from RoleEntity r left join fetch r.restrictions", RoleEntity.class);
		return query.getResultList();
	}

	public List<RestrictionEntity> getRoleWithRestrictions(String roleName) {
		return entityManager.createQuery("select r from RestrictionEntity r  where LOWER(r.role.name) =:roleName", RestrictionEntity.class)
				.setParameter("roleName", roleName.toLowerCase()).getResultList();
	}

	public List<RoleEntity> getAllRoles() {
		TypedQuery<RoleEntity> query = entityManager.createQuery("select r from RoleEntity r order by r.name", RoleEntity.class);
		return query.getResultList();
	}

	public List<String> getAllUserRestrictionNames() {
		TypedQuery<String> query = entityManager.createQuery("select u.name from UserRestrictionEntity u order by u.name", String.class);
		return query.getResultList();
	}

	public PermissionEntity getPermissionByName(String permissionName) {
		return entityManager.createQuery("from PermissionEntity p where LOWER(p.value) =:permission", PermissionEntity.class)
				.setParameter("permission", permissionName.toLowerCase()).getSingleResult();
	}

    public List<PermissionEntity> getAllPermissions() {
        TypedQuery<PermissionEntity> query = entityManager.createQuery("select p from PermissionEntity p order by p.value", PermissionEntity.class);
        return query.getResultList();
    }

	public RoleEntity getRoleByName(String roleName) {
		List<RoleEntity> result = entityManager.createQuery("from RoleEntity r where LOWER(r.name) =:role", RoleEntity.class)
				.setParameter("role", roleName.toLowerCase()).getResultList();
		return result == null || result.isEmpty() ? null : result.get(0);
	}

	public UserRestrictionEntity getUserRestrictionByName(String userName) {
		List<UserRestrictionEntity> result = entityManager.
				createQuery("from UserRestrictionEntity u where LOWER(u.name) =:userName", UserRestrictionEntity.class)
				.setParameter("userName", userName.toLowerCase()).getResultList();
		return result == null || result.isEmpty() ? null : result.get(0);
	}

	public UserRestrictionEntity createUserRestriciton(String userName) {
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity(userName.toLowerCase());
		entityManager.persist(userRestrictionEntity);
		return userRestrictionEntity;
	}

	public RoleEntity createRole(String roleName) {
		RoleEntity roleEntity = new RoleEntity();
		roleEntity.setName(roleName.toLowerCase());
		roleEntity.setDeletable(true);
		entityManager.persist(roleEntity);
		return roleEntity;
	}

	public boolean isReloadDeployableRoleList() {
		return reloadDeployableRoleList;
	}

	public void setReloadDeployableRoleList(boolean reloadDeployableRoleList) {
		this.reloadDeployableRoleList = reloadDeployableRoleList;
	}

	public boolean isReloadRolesAndPermissionsList() {
		return reloadRolesAndPermissionsList;
	}

	public void setReloadRolesAndPermissionsList(boolean reloadRolesAndPermissionsList) {
		this.reloadRolesAndPermissionsList = reloadRolesAndPermissionsList;
	}

	public boolean isReloadUserRestrictionsList() {
		return reloadUserRestrictionsList;
	}

	public void setReloadUserRestrictionsList(boolean reloadUserRestrictionsList) {
		this.reloadUserRestrictionsList = reloadUserRestrictionsList;
	}

	/**
	 * Returns Roles which have a Restriction matching the provided Permission
	 *
	 * @param permission Permission to match
	 * @return
	 */
	private List<RoleEntity> getRolesHavingRestrictionsWithPermission(Permission permission) {
		List<RoleEntity> result = entityManager
				.createQuery("from RoleEntity r left join fetch r.restrictions res where res.permission.value =:permission", RoleEntity.class)
				.setParameter("permission", permission.name()).getResultList();
		return result == null ? new ArrayList<RoleEntity>() : result;
	}

	/**
	 * Returns Roles which have a Restriction matching the provided Permission and Action
	 *
	 * @param permission
	 * @param action
	 * @return
	 */
	private List<RoleEntity> getRolesHavingRestrictionsWithPermissionAndAction(Permission permission, Action action) {
		List<RoleEntity> result = entityManager
				.createQuery("from RoleEntity r left join fetch r.restrictions res where res.permission.value =:permission and (res.action =:action or res.action = 'ALL')", RoleEntity.class)
				.setParameter("permission", permission.name()).setParameter("action", action).getResultList();
		return result == null ? new ArrayList<RoleEntity>() : result;
	}

}
