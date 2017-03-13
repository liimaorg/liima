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

package ch.puzzle.itc.mobiliar.presentation.propertyTagEdit;

import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyTagEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@RequestScoped
public class EditPropertyTagView {

    @Inject
    PropertyTagEditor propertyTagEditor;

    @Inject
    PermissionBoundary permissionBoundary;

    @Getter
    @Setter
    private String propertyTagName;

    private List<PropertyTagEntity> propertyTags = new ArrayList<>();

    @PostConstruct
    public void init(){
        refresh();
    }

    public List<PropertyTagEntity> getAllPropertyTags() {
        return propertyTags;
    }

    public void deletePropertyTag(Integer propertyTagId) {
        propertyTagEditor.deletePropertyTag(propertyTagId);
        refresh();
    }

    public void addPropertyTag() {
        propertyTagEditor.addGlobalPropertyTag(propertyTagName);
        refresh();
    }

    private void refresh() {
        this.propertyTags = propertyTagEditor.getAllGlobalPropertyTags();
        this.propertyTagName = null;
    }

    public boolean isCanManageGlobalTags(){
        return permissionBoundary.hasPermission(Permission.MANAGE_GLOBAL_TAGS);
    }
}
