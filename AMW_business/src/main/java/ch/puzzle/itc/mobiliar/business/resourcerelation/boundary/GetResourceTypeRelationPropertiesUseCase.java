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

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import java.util.List;

public interface GetResourceTypeRelationPropertiesUseCase {

    /**
     * Returns properties for a specific resource type relation.
     *
     * @param resourceTypeId the master resource type ID
     * @param relTypeId      the resource relation type ID (resRelTypeId)
     * @param contextId      the context ID
     * @return list of properties for the type relation
     * @throws NotFoundException if the resource type or relation type is not found
     */
    List<ResourceEditProperty> getPropertiesForTypeRelation(Integer resourceTypeId, Integer relTypeId, Integer contextId)
            throws NotFoundException;
}
