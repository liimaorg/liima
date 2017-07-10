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

package ch.puzzle.itc.mobiliar.presentation.resourceRelation;

import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.presentation.common.context.SessionContext;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class ResourceTypeRelationModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	Event<ResourceEditRelation> changeResourceEditRelationEvent;	
	
	@Getter
	private List<ResourceEditRelation> resourceTypeRelations;

	@Getter
	private boolean canShowRestypes; 
	
	private boolean allowedToRemoveRelations = false;
	
	@Getter
	private ResourceEditRelation currentResourceTypeRelation;
	
	@Inject
	SessionContext sessionContext;
	
	@Inject
	PermissionService permissionService;
	
	@Inject
	PropertyEditor editor;

    @Getter
    private boolean allowedToAddRelations = false;
	

	@PostConstruct
	public void postConstruct(){
		canShowRestypes = permissionService.hasPermission(Permission.REL_RESTYPE_PANEL_LIST);
        allowedToAddRelations = permissionService.hasPermissionToAddRelation(null, true, sessionContext.getCurrentContext()) || permissionService.hasPermissionToAddRelation(null, false, sessionContext.getCurrentContext());
		allowedToRemoveRelations = permissionService.hasPermissionToDeleteRelation(null, sessionContext.getCurrentContext());
	}	
	
	public void loadResourceTypeRelations(@Observes ResourceTypeEntity resourceType){
	    //TODO resourcetyperelations
//		if(resourceType!=null){
//			resourceTypeRelations = editor.getRelationsForResourceType(resourceType);
//		}
//		else{
			resourceTypeRelations = new ArrayList<ResourceEditRelation>();
//		}
//		setCurrentResourceTypeRelation(resourceTypeRelations==null || resourceTypeRelations.isEmpty() ? null : resourceTypeRelations.get(0));
	}
		
	public boolean isAllowedToRemoveRelation(){
		return currentResourceTypeRelation!=null && sessionContext.getIsGlobal() && allowedToRemoveRelations;
	}

	public boolean isActiveRelation(ResourceEditRelation rel){
		if(currentResourceTypeRelation!=null){
			return currentResourceTypeRelation.equals(rel);
		}
		return false;
	}
	
	/**
	 * Called by JSF - fires a ResourceEditRelation-Event if the current relation changes.
	 * 
	 * @param currentResourceTypeRelation
	 */
	public void setCurrentResourceTypeRelation(ResourceEditRelation currentResourceTypeRelation){
		if(this.currentResourceTypeRelation==null || !this.currentResourceTypeRelation.equals(currentResourceTypeRelation)){
			this.currentResourceTypeRelation = currentResourceTypeRelation;
			changeResourceEditRelationEvent.fire(currentResourceTypeRelation);			
		}
	}
	
	
	
}
