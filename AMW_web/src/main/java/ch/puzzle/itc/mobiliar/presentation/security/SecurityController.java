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

package ch.puzzle.itc.mobiliar.presentation.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.control.SecurityScreenDomainService;
import ch.puzzle.itc.mobiliar.business.security.control.SecurityScreenDomainService.PermissionToRole;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.RoleNotFoundException;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;

@Named
@RequestScoped
public class SecurityController implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Inject
	SecurityScreenDomainService service;
	
	@Inject
	PermissionService permissionService;
	
	public boolean createRole(String roleName) {
		String message;
		try {
			if(roleName == null || roleName.isEmpty()){
				message = "The name for Role must not be empty";
				GlobalMessageAppender.addErrorMessage(message);
				return false;
			}else {
				try{
					message = "Role: " + roleName + " successfully created.";
					service.createRoleByName(roleName);
					GlobalMessageAppender.addSuccessMessage(message);
					return true;
				}catch(EJBException e ){
					if(e.getCause() instanceof NotAuthorizedException) {
						GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
					} else {
						throw e;
					}
				}
			}
		} catch (ElementAlreadyExistsException e) {
			String errorMessage = "";
			if(e.getExistingObjectClass() == RoleEntity.class){
				errorMessage = "An role with the name " + e.getExistingObjectName() + " already exists.";
			}
			GlobalMessageAppender.addErrorMessage(errorMessage);
		}
		return false;
	}
	
	public boolean assignPermissionToRole(Integer selectedOldRole,Integer permissionSelected, Integer roleSelectedId) {
		String message;
			try {
				if(selectedOldRole == null){
					message = "No role selected!";
					GlobalMessageAppender.addErrorMessage(message);
				}else if(permissionSelected == null){
					message = "No permission selected!";
					GlobalMessageAppender.addErrorMessage(message);
				}else if(roleSelectedId == null){
					message = "No role selected!";
					GlobalMessageAppender.addErrorMessage(message);
				}else{
					RoleEntity r = service.getRoleById(roleSelectedId);
					PermissionEntity p = service.getUniquePermissionById(permissionSelected);
					if(service.assignPermissionToRole(selectedOldRole, permissionSelected, roleSelectedId)){
						message = "The permission " + p.getValue() + " is assigned to role " + r.getName();
						GlobalMessageAppender.addSuccessMessage(message);
						return true;
					}
					message = "The permission " + p.getValue() + " is already assigned to role " + r.getName();
					GlobalMessageAppender.addErrorMessage(message);
				}
			}catch (RoleNotFoundException e){
				message = "Could not load roles.";
				GlobalMessageAppender.addErrorMessage(message);
			}
		return false;
	}
	
	public boolean addPermissionToRole(Integer roleSelected,Integer permissionSelected) {
		String message;
		try{
			if(roleSelected == null){
				message = "No role selected!";
				GlobalMessageAppender.addErrorMessage(message);
			}else
				if(permissionSelected == null){
				message = "No permission selected!";
				GlobalMessageAppender.addErrorMessage(message);
			}
				else{
					RoleEntity r = service.getRoleById(roleSelected);
					PermissionEntity p = service.getUniquePermissionById(permissionSelected);
					if(service.addPermissionToRole(roleSelected,permissionSelected)){
						message = "The permission " +p.getValue()+ " is assigned to " + r.getName();
						GlobalMessageAppender.addSuccessMessage(message);
						return true;
					}
					message = "The selected permission " + p.getValue() + " is already assigned to role : " + r.getName() ;
					GlobalMessageAppender.addErrorMessage(message);
				}
		}catch (RoleNotFoundException e){
			message = "Could not load roles.";
			GlobalMessageAppender.addErrorMessage(message);
		}catch(EJBException e ){
			if(e.getCause() instanceof NotAuthorizedException) {
				GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
			} else {
				throw e;
			}
		}
		return false;
	}
	
	public boolean removeAndAssignPermissionToRole(Integer oldRoleSelectedId, Integer permissionSelectedId, Integer newRoleSelectedId){
		String message;
		try{
			if(permissionSelectedId == null || permissionSelectedId == 0){
				message = "No permission selected!";
				GlobalMessageAppender.addErrorMessage(message);
			}else if(oldRoleSelectedId == null ){
				message = "No role selected!";
				GlobalMessageAppender.addErrorMessage(message);
			}else if(newRoleSelectedId == null || newRoleSelectedId ==0 ){
				message = "No role selected!";
				GlobalMessageAppender.addErrorMessage(message);
			} else {
				RoleEntity r = service.getRoleById(newRoleSelectedId);
				PermissionEntity p = service.getUniquePermissionById(permissionSelectedId);
				if(service.assignPermissionToRole(oldRoleSelectedId, permissionSelectedId, newRoleSelectedId)){
					message = "The permission " + p.getValue() +" is assigned to " + r.getName();
					GlobalMessageAppender.addSuccessMessage(message);
					return true;
				}
				message = "The selected permission " + p.getValue() + " is already assigned to role : " + r.getName() ;
				GlobalMessageAppender.addErrorMessage(message);
			}
		}catch (RoleNotFoundException e){
			message = "Could not load roles.";
			GlobalMessageAppender.addErrorMessage(message);
		}catch(EJBException e ){
			if(e.getCause() instanceof NotAuthorizedException) {
				GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
			} else {
				throw e;
			}
		}
		return false;
	}
	
	public boolean movePermissionToDefaultContainer(Integer oldRoleSelectedId,Integer permissionSelectedId) {
		String message;
		try{
			if(permissionSelectedId == null){
				message = "No permission selected";
				GlobalMessageAppender.addErrorMessage(message);
				return false;
			}
			message = "The permission with id: " + permissionSelectedId + " is assigned to default container";
			service.assignPermissionToRole(oldRoleSelectedId, permissionSelectedId,0);
			GlobalMessageAppender.addSuccessMessage(message);
			return true;
		}catch (RoleNotFoundException e){
			message = "Could not load roles.";
			GlobalMessageAppender.addErrorMessage(message);
		}catch(EJBException e ){
			if(e.getCause() instanceof NotAuthorizedException) {
				GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
			} else {
				throw e;
			}
		}
		return false;
	}
	
	public boolean deleteRole(Integer roleId) {
		try {
			if(roleId == null){
				String errorMessage = "No role selected";
				GlobalMessageAppender.addErrorMessage(errorMessage);
				return false;
			}
			try{
				service.deleteRoleById(roleId);
				String message = "Role successfully deleted";
				GlobalMessageAppender.addSuccessMessage(message);
				return true;
			}catch(EJBException e ){
				if(e.getCause() instanceof NotAuthorizedException) {
					GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
				} else {
					throw e;
				}
			}
		} catch (RoleNotFoundException e) {
			String errorMessage = "Could not load selected role for deleted";
			GlobalMessageAppender.addErrorMessage(errorMessage);
		}
		return false;
	}

	public List<RoleEntity> loadRoleList() {
		return service.getAllRole();
	}

	public List<PermissionEntity> getPermissionsByRoleId(Integer roleSelected) {
		List<PermissionEntity> result = null;
		if(roleSelected != null){
			result = service.getPermissionListByRoleId(roleSelected);
		}
		return result == null ? new ArrayList<PermissionEntity>() : result;
	}
	
	public boolean hasPermissionToDeploy(){
		return permissionService.hasPermissionToDeploy();
	}

	public String getUserName() {
		return permissionService.getCurrentUserName();
	}

	public List<PermissionToRole> getAllPermissionsAndRoles(Integer roleSelectedId) {
		return service.permissionAndRole(roleSelectedId);
	}

	public Integer getDefaultPermissionsContainer(){
		return service.createOrGetPermissionWithoutAssignedRole().getId();
	}
	
}
