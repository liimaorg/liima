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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Use case for listing applications consumed by an application server
 */
public interface ListApplicationsForAppServerUseCase {
    
    /**
     * Get all applications for a given application server resource
     * 
     * @param resourceId the application server resource ID
     * @return list of consumed application relations
     * @throws ResourceNotFoundException if the resource is not found
     * @throws ValidationException if the resource ID is invalid
     */
    List<ConsumedResourceRelationEntity> listApplications(@NotNull Integer resourceId)
            throws ResourceNotFoundException, ValidationException;
}
