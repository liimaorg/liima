/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2026 by Puzzle ITC
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

package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import java.util.List;

public interface GetRelationPropertiesUseCase {

    /**
     * Returns properties for a specific resource relation in a given context.
     *
     * @param resourceId the resource ID
     * @param relationId       the relation ID (resRelId)
     * @param contextId        the context ID
     * @return list of properties for the relation
     * @throws ResourceNotFoundException if the resource or relation is not found
     */
    List<ResourceEditProperty> getPropertiesForRelation(Integer resourceId, Integer relationId, Integer contextId)
            throws ResourceNotFoundException;

    /**
     * Returns properties that differ from the parent context for a given resource and property.
     *
     * @param relationId       the relation ID
     * @param property         the property to check
     * @param relevantContexts the child contexts to compare
     * @return list of differing properties
     */
    List<PropertyEditingService.DifferingProperty> getPropertyOverviewForRelation(Integer relationId, ResourceEditProperty property, List<ContextEntity> relevantContexts)
            throws ResourceNotFoundException;
}
