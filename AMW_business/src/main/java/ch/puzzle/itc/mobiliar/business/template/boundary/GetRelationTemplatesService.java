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

package ch.puzzle.itc.mobiliar.business.template.boundary;


import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.template.control.TemplatesScreenDomainService;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Stateless
public class GetRelationTemplatesService implements GetRelationTemplatesUseCase {

    @Inject
    ResourceRelationService resourceRelationService;

    @Inject
    TemplatesScreenDomainService templateService;

    @Inject
    ResourceTypeLocator resourceTypeLocator;

    @Inject
    EntityManager entityManager;

    @Override
    public List<TemplateDescriptorEntity> getTemplatesForResourceRelation(Integer relationId)
            throws ResourceNotFoundException {
        ResourceEditRelation resourceEditRelation = toResourceEditRelation(relationId);
        List<TemplateDescriptorEntity> relationTemplates = templateService.getGlobalTemplatesForResourceRelation(resourceEditRelation);
        relationTemplates.forEach(template -> template.setSourceType(TemplateDescriptorEntity.TemplateSourceType.RESOURCE_RELATION));
        List<TemplateDescriptorEntity> relationTypeTemplates = templateService.getGlobalTemplatesForResourceRelationType(resourceEditRelation);
        relationTypeTemplates.forEach(template -> template.setSourceType(TemplateDescriptorEntity.TemplateSourceType.RESOURCE_RELATION_TYPE));

        return Stream.concat(relationTemplates.stream(), relationTypeTemplates.stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateDescriptorEntity> getTemplatesForResourceTypeRelation(Integer resourceTypeId, Integer relTypeId)
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

        List<TemplateDescriptorEntity> relationTemplates = templateService.getGlobalTemplatesForResourceRelationType(relation);
        relationTemplates.forEach(template -> template.setSourceType(TemplateDescriptorEntity.TemplateSourceType.RESOURCE_RELATION_TYPE));

        return relationTemplates;
    }

    //TODO remove duplicated code
    private ResourceEditRelation toResourceEditRelation(Integer relationId) throws ResourceNotFoundException {
        AbstractResourceRelationEntity relation = resourceRelationService.getResourceRelation(relationId);
        if (relation == null) {
            throw new ResourceNotFoundException("Relation with id " + relationId + " not found");
        }

        ResourceEditRelation.Mode mode;
        if (relation instanceof ConsumedResourceRelationEntity) {
            mode = ResourceEditRelation.Mode.CONSUMED;
        } else if (relation instanceof ProvidedResourceRelationEntity) {
            mode = ResourceEditRelation.Mode.PROVIDED;
        } else {
            throw new ResourceNotFoundException("Relation with id " + relationId + " is not a consumed or provided relation");
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
