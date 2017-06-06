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

package ch.puzzle.itc.mobiliar.maiafederationservice.boundary;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.Message;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyImportService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceImportService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.RelationImportService;
import ch.puzzle.itc.mobiliar.maiafederationservice.entity.ResourceHelper;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MaiaAmwFederationServiceRemovalHandler {

    @Inject
    private ResourceRepository resourceRepository;
    @Inject
    private Logger log;
    @Inject
    private ResourceImportService resourceImportService;

    @Inject
    private PropertyImportService propertyImportService;

    @Inject
    private RelationImportService relationImportService;

    public ResourceHelper handleRemoval(String appName) {
        log.info("Request for removing application " + appName);

        ResourceHelper resourceHelper = new ResourceHelper();
        resourceHelper.setAppName(appName);
        resourceHelper.setProcessingState(ProcessingState.FAILED);

        List<ResourceEntity> resources = resourceRepository.getResourcesByGroupNameWithRelations(appName);
        if (resources == null || resources.isEmpty()) {
            resourceHelper.addMessage(new Message(MessageSeverity.ERROR, "removal failed, application " + appName + " not found"));
        } else {

            if(isOneResourceMaiaOwned(resources)){
                for (ResourceEntity resource : resources) {
                    resourceRepository.remove(resource);
                }
                resourceRepository.removeResourceGroup(resources.get(0).getResourceGroup());
                resourceHelper.setProcessingState(ProcessingState.OK);
                resourceHelper.addMessage(new Message(MessageSeverity.INFO, "removed application " + appName));
            }else{
                resourceHelper.setProcessingState(ProcessingState.FAILED);
                resourceHelper.addMessage(new Message(MessageSeverity.WARNING, "application to remove does not belong to maia: " + appName));
            }

        }
        return resourceHelper;
    }

    private boolean isOneResourceMaiaOwned(List<ResourceEntity> resources){
        for (ResourceEntity resource : resources) {
            if (ForeignableOwner.MAIA.isSameOwner(resource.getOwner())) {
                return true;
            }
        }
        return false;
    }
}
