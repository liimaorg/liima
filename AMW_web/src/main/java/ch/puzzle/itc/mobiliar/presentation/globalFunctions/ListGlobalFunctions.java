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

package ch.puzzle.itc.mobiliar.presentation.globalFunctions;

import ch.puzzle.itc.mobiliar.business.globalfunction.boundary.GlobalFunctionsBoundary;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@CompositeBackingBean
public class ListGlobalFunctions implements Serializable {

    @Inject
    GlobalFunctionsBoundary functionsBoundary;

    @Inject
    PermissionBoundary permissionBoundary;

    private List<GlobalFunctionEntity> allGlobalFunctions;

    @Getter
    private Integer selectedFunctionIdToBeRemoved;

    public List<GlobalFunctionEntity> getAllGlobalFunctions() {
        if (this.allGlobalFunctions == null) {
            refreshList();
        }
        return this.allGlobalFunctions;
    }

    public void setSelectedFunctionIdToBeRemoved(Integer functionId){
          this.selectedFunctionIdToBeRemoved = functionId;
    }

    public List<GlobalFunctionEntity> loadAllFunctions() {
        return functionsBoundary.getAllGlobalFunctions();
    }

    /**
     * Defines if the current user has the rights to manage global functions
     */
    public boolean canManage() {
        return permissionBoundary.hasPermission(Permission.MANAGE_GLOBAL_FUNCTIONS);
    }
    
    public boolean canView() {
    	return permissionBoundary.hasPermission(Permission.VIEW_GLOBAL_FUNCTIONS);
    }

    public void deleteFunction() throws NotFoundException {
        if (selectedFunctionIdToBeRemoved != null) {
            functionsBoundary.deleteGlobalFunction(selectedFunctionIdToBeRemoved);
            selectedFunctionIdToBeRemoved = null;
            refreshList();
            GlobalMessageAppender.addSuccessMessage("Successfully deleted function");
        } else {
            GlobalMessageAppender.addErrorMessage("No function selected to be removed");
        }
    }

    private void refreshList() {
        this.allGlobalFunctions = loadAllFunctions();
    }

}
