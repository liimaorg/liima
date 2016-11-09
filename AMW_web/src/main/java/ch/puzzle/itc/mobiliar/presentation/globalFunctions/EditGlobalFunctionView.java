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

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.globalfunction.boundary.GlobalFunctionsBoundary;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.Permissions;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;


@ViewBackingBean
public class EditGlobalFunctionView implements Serializable{

    @Inject
    GlobalFunctionsBoundary functionsBoundary;

    @Inject
    Permissions permissionBoundary;

    @Getter
    private Integer functionIdViewParam;

    @Getter
    @Setter
    private GlobalFunctionEntity globalFunction;

    @Getter
    private GlobalFunctionEntity compareGlobalFunction;

    @Getter
    private List<RevisionInformation> revisionInformations;

    @Getter
    private RevisionInformation compareRevision;


    @PostConstruct
    public void init() {
        if (functionIdViewParam == null) {
            this.globalFunction = new GlobalFunctionEntity();
        }
    }

    /**
     * This is a viewParameter and is called by JSF!
     */
    public void setFunctionIdViewParam(Integer param) {
        if (param != null){
            this.functionIdViewParam = param;
            this.globalFunction = functionsBoundary.getFunctionById(functionIdViewParam);
            refreshRevisionInformation(functionIdViewParam);
        }
    }

    private void refreshRevisionInformation(Integer funId){
        revisionInformations = Lists.reverse(functionsBoundary.getFunctionRevisions(funId));
    }

    /**
     * Returns the id of the function to which we want compare the actual function
     */
    public Integer getCompareGlobalFunctionId() {
        return compareGlobalFunction != null ? compareGlobalFunction.getId() : null;
    }

    /**
     * Whether or not we are comparing two revisions of a function
     */
    public boolean isCompareMode() {
        return compareGlobalFunction != null;
    }

    /**
     * Returns the id of the revision to which we want compare the actual function
     */
    public Integer getCompareRevisionId() {
        return compareRevision != null ? compareRevision.getRevision().intValue() : null;
    }

    /**
     * Sets the revision id to which we want compare the actual function
     */
    public void setCompareRevisionId(Integer compareRevisionId) {
        if (compareRevisionId != null && compareRevisionId > 0) {
            for (RevisionInformation r : revisionInformations) {
                if (r.getRevision().intValue() == compareRevisionId) {
                    compareRevision = r;
                    compareGlobalFunction = functionsBoundary.getFunctionByIdAndRevision(functionIdViewParam, compareRevisionId);
                    return;
                }
            }
        }
        compareGlobalFunction = null;
        compareRevision = null;
    }

    /**
     * Returns true if the current global function has not been persisted yet
     */
    public boolean isNewFunction() {
        return globalFunction == null || globalFunction.getId() == null;
    }

    /**
     * Defines if the current user has the rights to manage global functions
     */
    public boolean canManage() {
        return permissionBoundary.hasPermission(Permission.MANAGE_GLOBAL_FUNCTIONS);
    }

    /**
     * Save function
     */
	public void saveFunction() {

		try {

			if (functionsBoundary.saveGlobalFunction(globalFunction)) {
				GlobalMessageAppender.addSuccessMessage("Function " + globalFunction.getName()
						+ " successfully saved");
				refreshRevisionInformation(globalFunction.getId());
			}
			else {
				GlobalMessageAppender.addErrorMessage("A function with the same name "
						+ globalFunction.getName() + " already exists");
			}
		}
		catch (AMWException e) {
			GlobalMessageAppender.addErrorMessage(e.getMessage());
		}
	}

}
