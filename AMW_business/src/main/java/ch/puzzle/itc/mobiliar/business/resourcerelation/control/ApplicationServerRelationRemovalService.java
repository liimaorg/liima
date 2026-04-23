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
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RelationEditor;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RemoveApplicationCommand;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RemoveApplicationFromAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ApplicationServerRelationRemovalService implements RemoveApplicationFromAppServerUseCase {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    RelationEditor relationEditor;

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = Action.UPDATE)
    public void removeApplication(RemoveApplicationCommand command)
            throws ResourceNotFoundException, ValidationException {
        
        ResourceEntity appServer = resourceLocator.getResourceWithGroupAndRelatedResources(command.getAppServerResourceId());
        
        if (appServer == null) {
            throw new ResourceNotFoundException("Application server with ID " + command.getAppServerResourceId() + " not found");
        }

        if (!appServer.getResourceType().isApplicationServerResourceType()) {
            throw new ValidationException("Resource is not an application server");
        }

        // Verify the relation exists and belongs to this application server
        boolean relationFound = false;
        for (AbstractResourceRelationEntity relation : appServer.getConsumedMasterRelations()) {
            if (relation.getId().equals(command.getRelationId())) {
                if (DefaultResourceTypeDefinition.APPLICATION.name()
                        .equals(relation.getResourceRelationType().getResourceTypeB().getName())) {
                    relationFound = true;
                    break;
                }
            }
        }

        if (!relationFound) {
            throw new ResourceNotFoundException("Application relation with ID " + command.getRelationId() + " not found for this application server");
        }

        try {
            relationEditor.removeRelation(command.getRelationId());
        } catch (ElementAlreadyExistsException e) {
            throw new ValidationException("Failed to remove application relation: " + e.getMessage());
        }
    }
}
