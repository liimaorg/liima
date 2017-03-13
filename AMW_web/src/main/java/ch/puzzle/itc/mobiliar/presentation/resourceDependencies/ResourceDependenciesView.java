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

package ch.puzzle.itc.mobiliar.presentation.resourceDependencies;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.NamedIdentifiable;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.presentation.resourceDependencies.events.SelectedResourceEvent;

/**
 * This View is a View Backing Bean but in request scope
 */
@Named
@RequestScoped
public class ResourceDependenciesView {


    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private ResourceTypeLocator resourceTypeLocator;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Getter
    @Setter
    private Integer contextIdViewParam;

    @Getter
    @Setter
    private Integer relationIdViewParam;

    @Getter
    private Integer resourceTypeIdViewParam;

    @Getter
    private NamedIdentifiable resourceOrType;

    @Inject
    Event<SelectedResourceEvent> selectedResourceEvent;

    @Inject
    Event<ResourceTypeEntity> resourceTypeEvent;

    @Getter
    private Integer resourceIdViewParam;



    public void setResourceIdViewParam(Integer resourceIdViewParam){
        if(this.resourceIdViewParam==null || !this.resourceIdViewParam.equals(resourceIdViewParam)){
            this.resourceIdViewParam = resourceIdViewParam;
            this.resourceOrType = resourceLocator.getResourceWithGroupAndRelatedResources(resourceIdViewParam);
            this.resourceTypeIdViewParam = null;
            selectedResourceEvent.fire(new SelectedResourceEvent((ResourceEntity)resourceOrType)); // TODO fire event to be observed by dataprovider for resourceRelations backing bean; to be verified that there is no conflict with event from EditResourceView!!!
        }
    }

    public void setResourceTypeIdViewParam(Integer resourceTypeIdViewParam){
        if(this.resourceTypeIdViewParam==null || !this.resourceTypeIdViewParam.equals(resourceTypeIdViewParam)){
            this.resourceTypeIdViewParam = resourceTypeIdViewParam;
            this.resourceOrType = resourceTypeLocator.getResourceType(resourceTypeIdViewParam);
            this.resourceIdViewParam = null;
            resourceTypeEvent.fire((ResourceTypeEntity)resourceOrType);
        }
    }

    public String getResourceName(){
        return resourceOrType != null ? resourceOrType.getName() : "";
    }

    public boolean hasPermissionToEditResource(){
        return permissionBoundary.hasPermission(Permission.EDIT_RES);
    }

    public boolean isEditResource() {
        return resourceIdViewParam != null;
    }

}
