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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

public class ApplistScreenDomainServiceTest {


    @InjectMocks
    ApplistScreenDomainService applistScreenDomainService;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Ensure, that there is an empty list resulting when asking with an empty myamw-filter (which means "my amw filter on, but no favorite resources available")
     * @throws Exception
     */
    @Test
    public void testGetApplicationServersWithEmptyMyAMWFilter() throws Exception {
        List<ResourceEntity> applicationServers = applistScreenDomainService
                  .getAppServerResourcesWithApplications("*", 42, Collections.<Integer>emptyList(), true);
        Assert.assertTrue(applicationServers.isEmpty());
    }

    /**
     * Ensure, that there is an empty list resulting when asking with an empty myamw-filter (which means "my amw filter on, but no favorite resources available")
     * @throws Exception
     */
    @Test
    public void testGetApplicationServerResourcesWithNull() throws Exception {
        List<ResourceEntity> applicationServers = applistScreenDomainService
                  .getApplicationServerResources("*", 42, Collections.<Integer>emptyList());
        Assert.assertTrue(applicationServers.isEmpty());
    }
}