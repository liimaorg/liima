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

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ResourceRelationMapper {

    @Inject
    ResourceRelationService resourceRelationService;

    public ResourceEditRelation toResourceEditRelation(Integer relationId) throws ResourceNotFoundException {
        AbstractResourceRelationEntity relation = resourceRelationService.getResourceRelation(relationId);
        if (relation == null) {
            throw new ResourceNotFoundException("Relation with id " + relationId + " not found");
        }
        return toResourceEditRelation(relation);
    }

    public ResourceEditRelation toResourceEditRelation(AbstractResourceRelationEntity relation) throws ResourceNotFoundException {
        ResourceEditRelation.Mode mode;
        if (relation instanceof ConsumedResourceRelationEntity) {
            mode = ResourceEditRelation.Mode.CONSUMED;
        } else if (relation instanceof ProvidedResourceRelationEntity) {
            mode = ResourceEditRelation.Mode.PROVIDED;
        } else {
            throw new ResourceNotFoundException("Relation with id " + relation.getId() + " is not a consumed or provided relation");
        }

        return new ResourceEditRelation(
                relation.getId(),
                relation.getSlaveResource().getId(),
                relation.buildIdentifer(),
                relation.getSlaveResource().getName(),
                relation.getSlaveResource().getResourceGroup().getId(),
                relation.getSlaveResource().getRelease().getId(),
                relation.getSlaveResource().getRelease().getName(),
                relation.getResourceRelationType().getResourceTypeB().getId(),
                relation.getResourceRelationType().getResourceTypeB().getName(),
                relation.getResourceRelationType().getResourceTypeA().getName(),
                relation.getResourceRelationType().getId(),
                relation.getResourceRelationType().getIdentifier(),
                mode.name(),
                relation.getSlaveResource().getRelease().getInstallationInProductionAt());
    }
}
