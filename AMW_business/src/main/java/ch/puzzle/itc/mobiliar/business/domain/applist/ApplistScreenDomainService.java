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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

/**
 * The ScreenDomainService for Applist Screens
 */
@Stateless
public class ApplistScreenDomainService {

    @Inject
    private ApplistScreenDomainServiceQueries queries;

    Tuple<List<ResourceEntity>, Long> getApplicationServerResources(Integer startIndex, Integer maxResults, String filter) {
        return queries.getAppServersWithApps(startIndex, maxResults, filter);
    }


    public Tuple<List<ResourceEntity>, Long> getAppServerResourcesWithApplications(Integer startIndex, Integer maxResults, String filter, boolean withAppServerContainer) {
        Tuple<List<ResourceEntity>, Long>  result = getApplicationServerResources(startIndex, maxResults, filter);
        List<ResourceEntity> appServerList = result.getA();
        for (ResourceEntity as : appServerList) {
            if (as.getName().equals(ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName())) {
                if (!withAppServerContainer || as.getConsumedMasterRelations().isEmpty()) {
                    appServerList.remove(as);
                    break;
                }
                ;
            }
        }
        return new Tuple<List<ResourceEntity>, Long>(appServerList, result.getB());
    }

}
