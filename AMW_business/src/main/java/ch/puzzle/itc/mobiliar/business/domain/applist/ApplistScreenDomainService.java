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

package ch.puzzle.itc.mobiliar.business.domain.applist;

import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;

/**
 * The ScreenDomainService for Applist Screens
 */
@Stateless
public class ApplistScreenDomainService {

    @Inject
    private ApplistScreenDomainServiceQueries queries;

    List<ResourceEntity> getApplicationServerResources(String filter, Integer maxResults, List<Integer> myAMW) {
        if (myAMW != null && myAMW.isEmpty()) {
            //there is a myAMW-filter, but it doesn't contain any values - so we don't have to ask the db - we know that there is an empty result
            return Collections.emptyList();
        }
        return queries.doFetchApplicationServersWithApplicationsOrderedByAppServerNameCaseInsensitive(filter, myAMW, maxResults);
    }


    public List<ResourceEntity> getAppServerResourcesWithApplications(String filter, Integer maxResults,
                                                                      List<Integer> myAmw, boolean withAppServerContainer) {
        List<ResourceEntity> appServerList = getApplicationServerResources(filter, maxResults, myAmw);
        for (ResourceEntity as : appServerList) {
            if (as.getName().equals(ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName())) {
                if (!withAppServerContainer || as.getConsumedMasterRelations().size() == 0) {
                    appServerList.remove(as);
                    break;
                }
            }
        }
        return appServerList;
    }

}
