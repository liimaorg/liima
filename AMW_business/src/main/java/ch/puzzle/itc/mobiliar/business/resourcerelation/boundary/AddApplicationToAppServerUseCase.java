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

import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

/**
 * Use case for adding an application to an application server
 */
public interface AddApplicationToAppServerUseCase {
    
    /**
     * Add an application relation to an application server
     * 
     * @param command the command containing app server resource ID and application resource group ID
     * @throws ResourceNotFoundException if the resource or application group is not found
     * @throws ValidationException if the resource is not an application server or validation fails
     * @throws ElementAlreadyExistsException if the relation already exists
     */
    void addApplication(AddApplicationCommand command) 
            throws ResourceNotFoundException, ValidationException, ElementAlreadyExistsException;
}
