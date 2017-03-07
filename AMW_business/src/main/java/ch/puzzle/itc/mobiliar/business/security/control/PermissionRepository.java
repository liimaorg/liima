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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;

@Stateless
public class PermissionRepository {
	@Inject
	private EntityManager entityManager;

	// FIXME: Remove to Singleton Bean or make static
	private boolean reloadDeploybleRoleList;
	private boolean reloadRolesAndPermissionsList;

	public PermissionRepository() {
		this.reloadDeploybleRoleList = false;
		this.reloadRolesAndPermissionsList = false;
	}

	/**
	 * Diese Methode sucht die Rollen die Deployoperation können machen.
	 * 
	 * @return
	 */
	public List<RoleEntity> getDoployableRole() {
		List<RoleEntity> result = entityManager
				.createQuery("from RoleEntity r where r.deployable=:deployable", RoleEntity.class)
				.setParameter("deployable", Boolean.TRUE).getResultList();
		return result == null ? new ArrayList<RoleEntity>() : result;
	}

	public List<RoleEntity> getDeployableRoles() {
		List<RoleEntity> result = entityManager
				.createQuery("from RoleEntity r left join fetch r.restrictions res where res.permission.value =:deployment", RoleEntity.class)
				.setParameter("deployment", Permission.DEPLOYMENT.name()).getResultList();
		return result == null ? new ArrayList<RoleEntity>() : result;
	}

	public List<RoleEntity> getRolesWithPermissions() {
		TypedQuery<RoleEntity> query = entityManager.createQuery("select r from RoleEntity r left join fetch r.permissions", RoleEntity.class);
		return query.getResultList();
	}

	public List<RoleEntity> getRolesWithRestrictions() {
		TypedQuery<RoleEntity> query = entityManager.createQuery("select r from RoleEntity r left join fetch r.restrictions", RoleEntity.class);
		return query.getResultList();
	}

	public boolean isReloadDeploybleRoleList() {
		return reloadDeploybleRoleList;
	}

	public void setReloadDeploybleRoleList(boolean reloadDeploybleRoleList) {
		this.reloadDeploybleRoleList = reloadDeploybleRoleList;
	}

	public boolean isReloadRolesAndPermissionsList() {
		return reloadRolesAndPermissionsList;
	}

	public void setReloadRolesAndPermissionsList(boolean reloadRolesAndPermissionsList) {
		this.reloadRolesAndPermissionsList = reloadRolesAndPermissionsList;
	}

}
