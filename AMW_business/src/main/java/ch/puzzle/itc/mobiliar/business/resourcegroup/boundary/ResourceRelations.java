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

package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.domain.applist.ApplistScreenDomainService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.usersettings.control.UserSettingsService;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.UserSettingsEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class ResourceRelations {

    @Inject
    ApplistScreenDomainService applistScreenDomainService;
    @Inject
    UserSettingsService userSettingsService;
    @Inject
    PermissionService permissionService;
    @Inject
    ResourceDependencyResolverService dependencyResolverService;

    public List<ResourceWithRelations> getAppServersWithApplications(String filter, Integer maxResults, ReleaseEntity release) {
        UserSettingsEntity userSettings = userSettingsService.getUserSettings(permissionService.getCurrentUserName());
        List<Integer> myAMWFilter = null;
        List<ResourceEntity> appServersWithAllApplications = applistScreenDomainService.getAppServerResourcesWithApplications(filter, maxResults, myAMWFilter, true);
        return filterAppServersByRelease(release, appServersWithAllApplications, myAMWFilter);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    List<ResourceWithRelations> filterAppServersByRelease(ReleaseEntity release,
              List<ResourceEntity> appServersWithAllApplications, List<Integer> myAMWFilter) {
        // filter app server releases
        Set<Integer> idLookup = new HashSet<>();
        List<ResourceWithRelations> result = new ArrayList<>();
        for (ResourceEntity as : appServersWithAllApplications) {
            ResourceGroupEntity g = as.getResourceGroup();
            if(!idLookup.contains(as.getResourceGroup().getId())){
                ResourceEntity asForRelease = dependencyResolverService
                          .getResourceEntityForRelease(g, release);
                if(asForRelease!=null) {
                    idLookup.add(as.getResourceGroup().getId());

                    ResourceWithRelations asWithApps = new ResourceWithRelations(asForRelease);
                    filterApplicationsByRelease(release, asForRelease, asWithApps, myAMWFilter);
                    result.add(asWithApps);
                }
            }
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    void filterApplicationsByRelease(ReleaseEntity release, ResourceEntity as,
              ResourceWithRelations asWithApps, List<Integer> myAMWFilter) {
        List<ResourceEntity> list = as.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION);
    	
        for (ResourceEntity r : list) {
            if(myAMWFilter==null || myAMWFilter.contains(r.getResourceGroup().getId())) {
                ResourceEntity rel = dependencyResolverService
                          .getResourceEntityForRelease(r.getResourceGroup(), release);
                if (rel != null && !asWithApps.getRelatedResources().contains(rel)) {
                    asWithApps.getRelatedResources().add(rel);
                }
            }
        }
    }
}
