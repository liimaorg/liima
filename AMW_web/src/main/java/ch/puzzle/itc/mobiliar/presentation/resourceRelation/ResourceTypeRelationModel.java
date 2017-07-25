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

import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import lombok.Getter;

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

	@Inject
	PermissionService permissionService;

	@Getter
	private boolean allowedToAddTypeRelations = false;
	
	public void loadResourceTypeRelations(@Observes ResourceTypeEntity resourceType){
		allowedToAddTypeRelations = permissionService.hasPermissionToAddRelatedResourceType(resourceType);
		canShowRestypes = permissionService.hasPermission(Permission.RESOURCETYPE, Action.READ, resourceType);
		resourceTypeRelations = new ArrayList<>();
	}
	
}
