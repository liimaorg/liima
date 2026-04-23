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

package ch.puzzle.itc.mobiliar.business.resourcerelation.control;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.AddApplicationCommand;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.AddApplicationToAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RelationEditor;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class ApplicationServerRelationAdditionService implements AddApplicationToAppServerUseCase {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    RelationEditor relationEditor;

    @Inject
    EntityManager entityManager;

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = Action.UPDATE)
    public void addApplication(AddApplicationCommand command) 
            throws ResourceNotFoundException, ValidationException, ElementAlreadyExistsException {

        ResourceEntity appServer = resourceLocator.getResourceWithGroupAndRelatedResources(
                command.getAppServerResourceId());

        if (appServer == null) {
            throw new ResourceNotFoundException("Application server with ID " 
                    + command.getAppServerResourceId() + " not found");
        }

        if (!appServer.getResourceType().isApplicationServerResourceType()) {
            throw new ValidationException("Resource is not an application server");
        }

        ResourceGroupEntity applicationGroup = entityManager.find(
                ResourceGroupEntity.class, command.getApplicationResourceGroupId());

        if (applicationGroup == null) {
            throw new ResourceNotFoundException("Application resource group with ID " 
                    + command.getApplicationResourceGroupId() + " not found");
        }

        if (!DefaultResourceTypeDefinition.APPLICATION.name()
                .equals(applicationGroup.getResourceType().getName())) {
            throw new ValidationException("Resource group is not an application");
        }

        // Add the relation (consumed, not provided)
        relationEditor.addRelation(
                command.getAppServerResourceId(), 
                command.getApplicationResourceGroupId(), 
                false, 
                null);
    }
}
