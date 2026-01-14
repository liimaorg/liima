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

package ch.puzzle.itc.mobiliar.presentation.resourcesedit;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ApplicationServer;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.util.NameChecker;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceException;

/**
 * Controller for creating new resources
 *
 * @author cweber
 */
@Named
@RequestScoped
public class CreateResourceController {

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private PermissionBoundary permissionBoundary;

    /**
     * @param newResourceName
     * @param resourceType
     * @param release
     * @return @return true if application was successful created, false otherwise
     * @throws AMWException
     */
    public boolean createResource(String newResourceName, ResourceTypeEntity resourceType
                                  , ReleaseEntity release) throws AMWException {
        boolean isSuccessful = false;
        String errorMessage = null;
        try {
            if (newResourceName == null) {
                errorMessage = "Could not read name for new resource.";
            } else if (newResourceName.isEmpty()) {
                errorMessage = "The name for the resource must not be empty.";
            } else if (resourceType == null) {
                errorMessage = "No resourcetype for new resource selected.";
            } else if (!NameChecker.isNameValid(newResourceName)) {
                errorMessage = NameChecker.getErrorTextForResourceType(
                        (resourceType != null) ? resourceType.getName() : null, newResourceName);
            } else if (release == null) {
                errorMessage = "The release for the resource must not be empty.";
            } else {
                try {
                    Resource r = resourceBoundary.createNewResourceByName(newResourceName,
                                resourceType.getId(), release.getId());
                    if (r != null) {
                        String message = "Resource " + newResourceName + " successfully created";
                        GlobalMessageAppender.addSuccessMessage(message);
                        try {
                            permissionBoundary.createAutoAssignedRestrictions(r.getEntity());
                        } catch (AMWException e) {
                            GlobalMessageAppender.addSuccessMessage("Failed to add resource permissions");
                        }
                        isSuccessful = true;
                    }
                } catch (EJBException e) {
                    errorMessage = handleEJBException(newResourceName, e);
                }
            }

        } catch (ResourceTypeNotFoundException e) {
            errorMessage = "Could not find resourcetype.";
        } catch (ElementAlreadyExistsException e) {
            errorMessage = "A resource with the name \"" + e.getExistingObjectName()
                    + "\" already exists for release " + release.getName();
        }
        if (errorMessage != null) {
            GlobalMessageAppender.addErrorMessage(errorMessage);
        }
        return isSuccessful;
    }

    private String handleEJBException(String newResourceName, EJBException e) {
        String errorMessage;
        if (e.getCause() instanceof NotAuthorizedException || e.getCause() instanceof NotAuthorizedException) {
            errorMessage = e.getCause().getMessage();
        } else if (e.getCause() instanceof PersistenceException) {
            errorMessage = "A resource with group name \"" + newResourceName + "\" already exist and can not be created!";
        } else {
            throw e;
        }
        return errorMessage;
    }

    /**
     * @param appName
     * @param releaseForApp
     * @param releaseForAs
     * @return true if application was successful created, false otherwise
     * @throws AMWException
     */
    public boolean createAppAndAppServer(String appName, Integer appServerGroup, ReleaseEntity releaseForApp, Integer releaseForAs) throws AMWException {
        Application app = null;
        boolean isSuccessful = false;
        String message;

        if (StringUtils.isEmpty(appName)) {
            message = "The name for Application must not be empty";
            GlobalMessageAppender.addErrorMessage(message);
        } else if (!NameChecker.isNameValid(appName)) {
            message = "Invalid application name \"" + appName
                    + "\". The name must not contain empty space or dots.";
            GlobalMessageAppender.addErrorMessage(message);
        } else if (releaseForApp == null) {
            message = "The release for the resource must not be empty.";
            GlobalMessageAppender.addErrorMessage(message);
        } else if (appServerGroup != null && releaseForAs == null) {
            GlobalMessageAppender.addErrorMessage("The release for the application server must not be empty.");
        } else {
            try {
                if (appServerGroup == null) {
                    // create App for special AS "Applications without application server"
                    app = resourceBoundary.createNewApplicationWithoutAppServerByName(appName, releaseForApp.getId(), false);
                    if (app != null) {
                        message = "Application " + appName + " without Application Server successfully created.";
                        GlobalMessageAppender.addSuccessMessage(message);
                    }
                } else {
                    // create App for AS
                    app = resourceBoundary.createNewUniqueApplicationForAppServer(appName, appServerGroup, releaseForApp.getId(), releaseForAs);
                    if (app != null) {
                        message = "Application " + appName + " successfully created.";
                        GlobalMessageAppender.addSuccessMessage(message);
                    }
                }

                if (app != null) {
                    try {
                        permissionBoundary.createAutoAssignedRestrictions(app.getEntity());
                    } catch (AMWException e) {
                        GlobalMessageAppender.addSuccessMessage("Failed to add resource permissions");
                    }
                    isSuccessful = true;
                }

            } catch (ElementAlreadyExistsException e) {
                ElementAlreadyExistsException ex = e;
                String errorMessage = "";
                if (ex.getExistingObjectClass() == Application.class) {
                    errorMessage = "An application with the name " + ex.getExistingObjectName()
                            + " already exists.";
                } else if (ex.getExistingObjectClass() == ApplicationServer.class) {
                    errorMessage = "An application server with the name " + ex.getExistingObjectName()
                            + " already exists.";
                } else {
                    errorMessage = "An element with the name " + ex.getExistingObjectName()
                            + " already exists.";
                }
                GlobalMessageAppender.addErrorMessage(errorMessage);
            } catch (EJBException e) {
                GlobalMessageAppender.addErrorMessage(handleEJBException(appName, e));
            } catch (ResourceTypeNotFoundException e) {
                GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
            }
        }
        return isSuccessful;
    }

}
