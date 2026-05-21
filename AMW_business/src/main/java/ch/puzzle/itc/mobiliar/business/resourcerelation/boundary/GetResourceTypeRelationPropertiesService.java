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

import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@Stateless
public class GetResourceTypeRelationPropertiesService implements GetResourceTypeRelationPropertiesUseCase {

    @Inject
    ResourceTypeLocator resourceTypeLocator;

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    EntityManager entityManager;

    @Override
    public List<ResourceEditProperty> getPropertiesForTypeRelation(Integer resourceTypeId, Integer relTypeId, Integer contextId)
            throws NotFoundException {
        ResourceTypeEntity resourceType = resourceTypeLocator.getResourceType(resourceTypeId);
        if (resourceType == null) {
            throw new NotFoundException("ResourceType with id " + resourceTypeId + " not found");
        }
        ResourceRelationTypeEntity relationType = entityManager.find(ResourceRelationTypeEntity.class, relTypeId);
        if (relationType == null) {
            throw new NotFoundException("ResourceRelationType with id " + relTypeId + " not found");
        }
        ResourceEditRelation relation = new ResourceEditRelation(
                null, null, null, null, null, null, null, null,
                null, null, relTypeId, null,
                ResourceEditRelation.Mode.TYPE.name(), null);
        return propertyEditor.getPropertiesForRelatedResourceType(relation, contextId);
    }
}
