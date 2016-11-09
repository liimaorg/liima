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
import ch.puzzle.itc.mobiliar.common.util.RolePermissionContainer;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;

@Named
@RequestScoped
public class SecurityController implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Inject
	SecurityScreenDomainService service;
	
	@Inject
	PermissionService permissionService;
	
	public boolean createRole(String roleName, boolean isDeployable) {
		boolean isSuccessfully = false;
		String message;
		try {
			if(roleName == null || roleName.isEmpty()){
				message = "The name for Role must not be empty";
				GlobalMessageAppender.addErrorMessage(message);
			}else {
				try{
					message = "Role: " + roleName + " sucessfully created.";
					service.createRoleByName(roleName,isDeployable);
					GlobalMessageAppender.addSuccessMessage(message);
					isSuccessfully = true;
				}catch(EJBException e ){
					if(e.getCause() instanceof NotAuthorizedException) {
						GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
					} else {
						throw e;
					}
				}
			}
		} catch (ElementAlreadyExistsException e) {
			ElementAlreadyExistsException ex = (ElementAlreadyExistsException) e;
			String errorMessage = "";
			if(ex.getExistingObjectClass() == RoleEntity.class){
				errorMessage = "An role with the name " + e.getExistingObjectName() + " already exists.";
			}
			GlobalMessageAppender.addErrorMessage(errorMessage);
		}
		return isSuccessfully;
	}
	
	public boolean assignPermissionToRole(Integer selectedOldRole,Integer permissionSelected, Integer roleSelectedId) {
		boolean isSuccesfully = false;
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
					if((r.isDeployable() && p.getContext()==null) && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
						message = "The permission "+ p.getValue() +" is not a deployable permission. The role " +r.getName() + " is a deployable role. Please select a not deployable role";
						GlobalMessageAppender.addErrorMessage(message);
					}else if(!r.isDeployable() && p.getContext()!=null && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
						message = "The permission "+ p.getValue() + " is a deployable permission. The role "+ r.getName()+ " is not a deployable role. Please select a deployable role";
						GlobalMessageAppender.addErrorMessage(message);
					}else if((r.isDeployable() && p.getContext()!=null) || (!r.isDeployable() && p.getContext()==null) || r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
						if(service.assignPermissionToRole(selectedOldRole, permissionSelected, roleSelectedId)){
						message = "The permission " + p.getValue() + " is assigned to role " + r.getName();
						GlobalMessageAppender.addSuccessMessage(message);
						isSuccesfully=true;
					}else{
						message = "The permission " + p.getValue() + " is already assigned to role " + r.getName();
						GlobalMessageAppender.addErrorMessage(message);
					}
				}
				}
			}catch (RoleNotFoundException e){
				message = "Could not load roles.";
				GlobalMessageAppender.addErrorMessage(message);
			}
		return isSuccesfully;
	}
	
	public boolean addPermissionToRole(Integer roleSelected,Integer permissionSelected) {
		boolean isSuccessfully = false;
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
				if((r.isDeployable() && p.getContext()==null) && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
					message = "The permission "+ p.getValue() +" is not a deployable permission. The role " +r.getName() + " is a deployable role. Please select a not deployable role";
					GlobalMessageAppender.addErrorMessage(message);
				}else if(!r.isDeployable() && p.getContext()!=null && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
					message = "The permission "+ p.getValue() + " is a deployable permission. The role "+ r.getName()+ " is not a deployable role. Please select a deployable role";
					GlobalMessageAppender.addErrorMessage(message);
				}else if((r.isDeployable() && p.getContext()!=null) || (!r.isDeployable() && p.getContext()==null) || r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
					if(service.addPermissionToRole(roleSelected,permissionSelected)){
						message = "The permission " +p.getValue()+ " is assigned to " + r.getName();
						GlobalMessageAppender.addSuccessMessage(message);
						isSuccessfully = true;
					}
					else{
						message = "The selected permission " + p.getValue() + " is already assigned to role : " + r.getName() ;
						GlobalMessageAppender.addErrorMessage(message);
					}
						
				}
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
		return isSuccessfully;
	}
	
	public boolean removeAndAssignPermissionToRole(Integer oldRoleSelctedId, Integer permissionSelectedId, Integer newRoleSelectedId){
		boolean isSuccesfully = false;
		String message;
		try{
			if(permissionSelectedId == null || permissionSelectedId == 0){
				message = "No permission selected!";
				GlobalMessageAppender.addErrorMessage(message);
			}else if(oldRoleSelctedId == null ){
				message = "No role selected!";
				GlobalMessageAppender.addErrorMessage(message);
			}else if(newRoleSelectedId == null || newRoleSelectedId ==0 ){
				message = "No role selected!";
				GlobalMessageAppender.addErrorMessage(message);
			} else {
				RoleEntity r = service.getRoleById(newRoleSelectedId);
				PermissionEntity p = service.getUniquePermissionById(permissionSelectedId);
				if((r.isDeployable() && p.getContext()==null) && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
					message = "The permission "+ p.getValue() +" is not a deployable permission. The role " +r.getName() + " is a deployable role. Please select a not deployable role ";
					GlobalMessageAppender.addErrorMessage(message);
				}else if(!r.isDeployable() && p.getContext()!=null && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
					message = "The permission "+ p.getValue() + " is a deployable permission. The role "+ r.getName()+ " is not a deployable role. Please select a deployable role";
					GlobalMessageAppender.addErrorMessage(message);
				}else if((r.isDeployable() && p.getContext()!=null) || (!r.isDeployable() && p.getContext()==null) || r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
					
					if(service.assignPermissionToRole(oldRoleSelctedId, permissionSelectedId, newRoleSelectedId)){
						message = "The permission " + p.getValue() +" is assigned to " + r.getName();
						GlobalMessageAppender.addSuccessMessage(message);
						isSuccesfully = true;
					}else{
						message = "The selected permission " + p.getValue() + " is already assigned to role : " + r.getName() ;
						GlobalMessageAppender.addErrorMessage(message);
					}
				}
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
		return isSuccesfully;
	}
	
	public boolean movePermissionToDefaultContainer(Integer oldRoleSelectedId,Integer permissionSelectedId) {
		boolean isSuccesfully = false;
		String message;
		try{
			if(permissionSelectedId == null){
				message = "No permission selected";
				GlobalMessageAppender.addErrorMessage(message);
			}else{
				message = "The permission with id: " + permissionSelectedId + " is assigned to default container";
				service.assignPermissionToRole(oldRoleSelectedId, permissionSelectedId,0);
				GlobalMessageAppender.addSuccessMessage(message);
				isSuccesfully = true;
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
		return isSuccesfully;
	}
	
	public boolean deleteRole(Integer roleId) {
		boolean isSUccesfully = false;
		try {
			if(roleId == null){
				String errorMessage = "No role selected";
				GlobalMessageAppender.addErrorMessage(errorMessage);
			}else {
				try{
					service.deleteRoleById(roleId);
					String message = "Role successfully deleted";
					GlobalMessageAppender.addSuccessMessage(message);
					isSUccesfully = true;
				}catch(EJBException e ){
					if(e.getCause() instanceof NotAuthorizedException) {
						GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
					} else {
						throw e;
					}
				}
			}
		} catch (RoleNotFoundException e) {
			String errorMessage = "Could not load selected role for deleted";
			GlobalMessageAppender.addErrorMessage(errorMessage);
		}
		return isSUccesfully;
	}
	
	public boolean isPermissionDeployable(Integer permissionId) {

		if(permissionId == null || permissionId == 0){
			String errorMessage = "No permission selected";
			GlobalMessageAppender.addErrorMessage(errorMessage);
		}else{
			for(PermissionEntity p : service.getAllPermissions()){
				if(p.getId().equals(permissionId)){
					if(p.getContext() != null ) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public List<RoleEntity> loadRoleList() {
		List<RoleEntity> result = null;
		return service.getAllRole();
	}

	public String getRoleById(Integer roleSelected) {
		RoleEntity r = null;
		try {
			r = service.getRoleById(roleSelected);
			if(r != null) {
				return r.getName();
			}
		} catch (RoleNotFoundException e) {
			String errorMessage = "Could not load selected role";
			GlobalMessageAppender.addErrorMessage(errorMessage);
		}	
		return null;
	}

	public List<PermissionEntity> getPermissiosByRoleId(Integer roleSelected) {
		List<PermissionEntity> result = null;
		if(roleSelected != null){
			result = service.getPermissionListByRoleId(roleSelected);
		}

		return result == null ? new ArrayList<PermissionEntity>() : result;
	}

	public List<RoleEntity> getAllDeployableRoles() {
		return permissionService.getDeployableRolesNonCached();
	}

	public List<PermissionEntity> getAllPermWithoutPermOfSelectedRole(Integer roleSelected) {
		return service.getAllPermissionsWithoutPermissionsOfRoleSelected(roleSelected);
	}
	
	public boolean hasPermissionToDeploy(){
		return permissionService.hasPermissionToDeploy();
	}

	public String getUserName() {
		return permissionService.getCurrentUserName();
	}

	public boolean getDeletableRole(Integer roleSelected) {
		boolean isDeletable = false;

		for(RoleEntity roleEntity : service.getAllRole()){
			if(roleEntity.getId().equals(roleSelected) && roleEntity.isDeletable()) {
				isDeletable = true;
			}
		}

		return isDeletable;
	}
	
	public RoleEntity getPermissionByRole(Integer roleId,Integer permissionId){
		return service.getPermissionByRole(roleId, permissionId);
	}

	public List<PermissionToRole> getAllPermissionsAndRoles(Integer roleSelectedId) {
		return service.permissionAndRole(roleSelectedId);
	}

	public Integer getDefaultPermissionsContainer(){
		RoleEntity role = null;
		role =  service.createOrGetPermissionWithoutAssignedRole();

		return role.getId();
	}
	
}
