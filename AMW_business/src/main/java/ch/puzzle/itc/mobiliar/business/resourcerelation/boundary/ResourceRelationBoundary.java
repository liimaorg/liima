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

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationRepository;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;

import java.util.List;

@Stateless
public class ResourceRelationBoundary {

    @Inject
    ResourceRelationRepository resourceRelationRepository;

    /**
     * Checks if a Resource can be added as provided Resource
     * A Resource can only be provided by one ResourceGroup
     * @param masterResource
     * @param slaveResourceName
     * @return
     */
    public boolean isAddableAsProvidedResourceToResourceGroup(ResourceEntity masterResource, String slaveResourceName) {
        List<ProvidedResourceRelationEntity> resourceRelations = resourceRelationRepository.getResourceRelationOfOtherMasterResourceGroupsBySlaveResourceGroupName(masterResource, slaveResourceName);
        if (resourceRelations != null) {
            for (ProvidedResourceRelationEntity resourceRelation : resourceRelations) {
                if (!resourceRelation.getMasterResource().getName().equals(masterResource.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

}
