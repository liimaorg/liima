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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class RelationImportService {

    @Inject
    private Logger log;

    @Inject
    private ResourceRelationService relationService;


    public void deleteConsumedPortRelations(List<ConsumedResourceRelationEntity> toBeDeleted) throws ResourceNotFoundException, ElementAlreadyExistsException {
        for (ConsumedResourceRelationEntity consumedMasterRelation : toBeDeleted) {
            relationService.removeRelation(consumedMasterRelation);
        }
    }

    public void deleteProvidedPortRelations(ResourceEntity application, List<ProvidedResourceRelationEntity> toBeDeleted) {
        for (ProvidedResourceRelationEntity providedMasterRelation : toBeDeleted) {
            application.removeRelation(providedMasterRelation);
            relationService.deleteRelation(providedMasterRelation);
        }
    }

    public <T extends AbstractResourceRelationEntity> T getMatchingResource(String slaveResourceName, Set<T> masterRelations) {
        if (masterRelations != null) {
            for (AbstractResourceRelationEntity masterRelation : masterRelations) {
                if (masterRelation.getSlaveResource().getName().equalsIgnoreCase(slaveResourceName)) {
                    return (T) masterRelation;
                }
            }
        }
        return null;
    }

    public void addRelation(ResourceEntity application, ResourceEntity relatedResource, Integer releaseId, boolean provided) {
        try {
            relationService.doAddResourceRelationForSpecificRelease(application.getId(), relatedResource.getResourceGroup().getId(), provided, null, null, releaseId);
            log.info((provided ? "Provided " : "Consumed ") + "port relation between " + application.getName() + " and " + relatedResource.getName() + "successfully created");
        } catch (AMWException e) {
            // TODO message human readable
            log.warning((provided ? "Provided " : "Consumed ") + "port relation creation failed! Reason: " + e.getMessage());
            throw new AMWRuntimeException("CODE: " + (provided ? "Provided " : "Consumed ") + "port relation creation failed", e);
        }
    }

}
