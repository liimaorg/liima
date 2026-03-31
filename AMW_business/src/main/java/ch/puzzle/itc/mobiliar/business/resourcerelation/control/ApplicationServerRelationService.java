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
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ListApplicationsForAppServerUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ApplicationServerRelationService implements ListApplicationsForAppServerUseCase {

    @Inject
    ResourceLocator resourceLocator;

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = Action.READ)
    public List<ConsumedResourceRelationEntity> listApplications(Integer resourceId) 
            throws ResourceNotFoundException, ValidationException {
        
        if (resourceId == null) {
            throw new ValidationException("Resource ID must not be null");
        }

        ResourceEntity resource = resourceLocator.getResourceWithGroupAndRelatedResources(resourceId);
        
        if (resource == null) {
            throw new ResourceNotFoundException("Resource with ID " + resourceId + " not found");
        }

        if (!resource.getResourceType().isApplicationServerResourceType()) {
            throw new ValidationException("Resource is not an application server");
        }

        List<ConsumedResourceRelationEntity> applications = new ArrayList<>();
        
        for (ConsumedResourceRelationEntity relation : resource.getConsumedMasterRelations()) {
            if (DefaultResourceTypeDefinition.APPLICATION.name()
                    .equals(relation.getResourceRelationType().getResourceTypeB().getName())) {
                applications.add(relation);
            }
        }

        return applications;
    }
}
