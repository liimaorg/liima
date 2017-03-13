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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.control.SecurityScreenDomainService.PermissionToRole;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.RolePermissionContainer;

/**
 * First: Permissions should not be checked directly from jsf (xhtml) but rather through their corresponding view backing beans
 * Second: The permission check logic should be located in business (boundary)
 */
@Named
@SessionScoped // TODO do we need session scope or can we use view scope?
@Deprecated
public class SecurityDataProvider implements Serializable{

	private static final long serialVersionUID = 1L;

	private String roleName;
	private Integer roleSelectedId;
	private String roleSelectedName;
	private List<RoleEntity> roles;
	private List<RoleEntity> rolesWithoutRoleSelected;
	private boolean isDeployable;

	private Integer permissionSelected;
	private Integer assignedPermissionSelected;
	private boolean isDeletable;
	private String displaySelectedPermission;
	private Integer selectedRoleIdToAssignPermission;
	private Integer selectedDeployPermission;
	private String infoPermission;
	private Integer selectedOldRole;
	private List<PermissionToRole> allPermissionAndRole;
	private Integer selectedAgainRoleId;

	@Inject
	SecurityController controller;

	@Inject
	PermissionBoundary permissionBoundary;
	
	@PostConstruct
	protected void initView(){
		roles = getAllRoleWithoutDefaultContainer();
		setDefaultRoleSelected();
		setDefaultSelectedPermission();
		selectedAgainRoleId = roleSelectedId;
		allPermissionAndRole = controller.getAllPermissionsAndRoles(roleSelectedId);
	}
	
	public List<RoleEntity> getAllRoleWithoutDefaultContainer(){
		List<RoleEntity> result = new ArrayList<RoleEntity>();
		for(RoleEntity role : controller.loadRoleList()){
			if(!RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName().equals(role.getName())){
				result.add(role);
			}
		}
		return result;
	}
	
	private void setDefaultRoleSelected(){
		if(roleSelectedId==null){
			for(RoleEntity r : roles){
				if(r!=null){
					setRoleSelectedId(r.getId());
					roleSelectedName=r.getName();
					break;
				}
			}
		}
	}
	
	private void setDefaultSelectedPermission(){
		for(PermissionEntity p : controller.getPermissiosByRoleId(roleSelectedId)){
			if(p!=null){
				displaySelectedPermission = p.getValue();
				assignedPermissionSelected = p.getId();
				selectedDeployPermission = p.getId();
				break;
			}else{
				displaySelectedPermission = null;
				assignedPermissionSelected = null;
				selectedDeployPermission = null;
			}
		}
	}
		
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public void createRole() {
		controller.createRole(roleName, isDeployable);
		roleName=null;
		isDeployable=false;
		roles = getAllRoleWithoutDefaultContainer();
	}
	
	public void deleteRole(){
		controller.deleteRole(roleSelectedId);
		roleSelectedId=null;
		roles = getAllRoleWithoutDefaultContainer();
		setDefaultRoleSelected();
	}

	public Integer getRoleSelectedId() {
		return roleSelectedId;
	}

	private List<PermissionEntity> currentPermissions;
	
	public void setRoleSelectedId(Integer roleSelectedId) {
		this.roleSelectedId = roleSelectedId;
		setDefaultSelectedPermission();
		currentPermissions = controller.getPermissiosByRoleId(roleSelectedId);
		permissionSelected = null;
	}

	public List<RoleEntity> getAllRoleList() {
		return roles;
	}

	public void setAllRoleList(List<RoleEntity> roleList) {
		this.roles = roleList;
	}
	
	public List<PermissionEntity> getPermissionsByRoleId() {
		return currentPermissions;
	}

	public boolean isDeployable() {
		return isDeployable;
	}

	public void setDeployable(boolean isDeployable) {
		this.isDeployable = isDeployable;
	}
	
	public List<RoleEntity> getDeplopyableRoles() {
		return controller.getAllDeployableRoles();
	}

	/**
	 * Use {@link PermissionBoundary#hasPermission(Permission)} instead
	 * @param permissionValue
	 * @return
	 */
	@Deprecated
	public boolean hasPermission(String permissionValue){
		return permissionBoundary.hasPermission(permissionValue);
	}

	public Integer getPermissionSelected() {
		return permissionSelected;
	}

	public void setPermissionSelected(Integer permissionSelected) {
		assignedPermissionSelected=null;
		this.permissionSelected = permissionSelected;
	}
	
	public boolean hasPermissionToDeploy(){
		return controller.hasPermissionToDeploy();
	}
	
	public boolean hasPermissionToCreateShakedownTest(){
		// TODO: bsc: 13.11.2012: Permission muss erstellt werden hier
		return true;
	}

	public String getUserName() {
		return controller.getUserName();
	}

	public boolean isDeletable() {
		if(roleSelectedId==null) {
			setDefaultRoleSelected();
		}
		for(RoleEntity r : roles){
			if(r.getId().equals(roleSelectedId)){
				isDeletable = r.isDeletable();
					break;
			}
		}
		return isDeletable;
	}

	public void setDeletable(boolean isDeletable) {
		this.isDeletable = isDeletable;
	}
	
	public void addPermissionToRole(){
		if(controller.addPermissionToRole(roleSelectedId,permissionSelected)){
			assignedPermissionSelected = permissionSelected;
			permissionSelected = null;
			currentPermissions = controller.getPermissiosByRoleId(roleSelectedId);
		}
	}
	
	public void assignPermissionToRole(){
		if(controller.assignPermissionToRole(selectedOldRole,permissionSelected,roleSelectedId)){
			assignedPermissionSelected = permissionSelected;
			permissionSelected = null;
			currentPermissions = controller.getPermissiosByRoleId(roleSelectedId);
			allPermissionAndRole = controller.getAllPermissionsAndRoles(roleSelectedId);
			
		}
	}
	
	public void removeAndAssignPermissionToRole(){
		if(controller.removeAndAssignPermissionToRole(roleSelectedId, assignedPermissionSelected,selectedRoleIdToAssignPermission)){
			permissionSelected = assignedPermissionSelected;
			assignedPermissionSelected = null;
			currentPermissions = controller.getPermissiosByRoleId(roleSelectedId);
			allPermissionAndRole = controller.getAllPermissionsAndRoles(roleSelectedId);
			setSelectedOldRole(selectedRoleIdToAssignPermission);
		}
	}
	
	public void movePermissionToDefaultContainer(){
		if(controller.movePermissionToDefaultContainer(roleSelectedId,assignedPermissionSelected)){
			
			permissionSelected = assignedPermissionSelected;
			assignedPermissionSelected = null;
			currentPermissions = controller.getPermissiosByRoleId(roleSelectedId);
			allPermissionAndRole = controller.getAllPermissionsAndRoles(roleSelectedId);
			setSelectedOldRole(controller.getDefaultPermissionsContainer());
		}
	}

	public Integer getAssignedPermissionSelected() {
		return assignedPermissionSelected;
	}

	public void setAssignedPermissionSelected(Integer assignedPermissionSelected) {
		permissionSelected = null;
		this.assignedPermissionSelected = assignedPermissionSelected;
	}
	
	public String getRoleSelectedName() {
		return roleSelectedName;
	}

	public void setRoleSelectedName(String roleSelectedName) {
		this.roleSelectedName = roleSelectedName;
	}

	public void logout() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		String logoutUrl = ConfigurationService.getProperty(ConfigKey.LOGOUT_URL);
		if (logoutUrl == null) {
			throw new RuntimeException("Please define the property \""+ConfigKey.LOGOUT_URL+"\"");
		}
		FacesContext.getCurrentInstance().getExternalContext().redirect(logoutUrl);
	}

	public List<RoleEntity> getRolesWithoutRoleSelected() {
		rolesWithoutRoleSelected = new ArrayList<RoleEntity>();
		for(RoleEntity r : controller.loadRoleList()){
			if(!r.getId().equals(getRoleSelectedId()) && !r.getName().equals(RolePermissionContainer.ROLEPERMISSIONCONTAINER.getDisplayName())){
				rolesWithoutRoleSelected.add(r);
			}
		}
		return rolesWithoutRoleSelected;
	}

	public void setRolesWithoutRoleSelected(List<RoleEntity> rolesWithoutRoleSelected) {
		this.rolesWithoutRoleSelected = rolesWithoutRoleSelected;
	}

	public String getDisplaySelectedPermission() {
		return displaySelectedPermission;
	}

	public void setDisplaySelectedPermission(String displaySelectedPermission) {
		this.displaySelectedPermission = displaySelectedPermission;
	}

	public Integer getSelectedRoleIdToAssignPermission() {
		return selectedRoleIdToAssignPermission;
	}

	public void setSelectedRoleIdToAssignPermission(
			Integer selectedRoleIdToAssignPermission) {
		this.selectedRoleIdToAssignPermission = selectedRoleIdToAssignPermission;
	}
	
	public boolean isPermissionDeployable(){
		return controller.isPermissionDeployable(selectedDeployPermission);
	}

	public Integer getSelectedDeployPermission() {
		return selectedDeployPermission;
	}

	public void setSelectedDeployPermission(Integer selectedDeployPermission) {
		this.selectedDeployPermission = selectedDeployPermission;
	}

	public String getInfoPermission() {
		return infoPermission;
	}

	public void setInfoPermission(String infoPermission) {
		this.infoPermission = infoPermission;
	}

	public Integer getSelectedOldRole() {
		return selectedOldRole;
	}

	public void setSelectedOldRole(Integer selectedOldRole) {
		this.selectedOldRole = selectedOldRole;
	}
	
	public List<PermissionToRole> getPermissionsAndRoles(){
		if(selectedAgainRoleId != roleSelectedId){
			allPermissionAndRole = controller.getAllPermissionsAndRoles(roleSelectedId);
			selectedAgainRoleId=roleSelectedId;
		}
		return allPermissionAndRole;
	}

	public List<PermissionToRole> getAllPermissionAndRole() {
		return allPermissionAndRole;
	}

	public void setAllPermissionAndRole(List<PermissionToRole> allPermissionAndRole) {
		this.allPermissionAndRole = allPermissionAndRole;
	}

	public Integer getSelectedAgainRoleId() {
		return selectedAgainRoleId;
	}

	public void setSelectedAgainRoleId(Integer selectedAgainRoleId) {
		this.selectedAgainRoleId = selectedAgainRoleId;
	}
}
