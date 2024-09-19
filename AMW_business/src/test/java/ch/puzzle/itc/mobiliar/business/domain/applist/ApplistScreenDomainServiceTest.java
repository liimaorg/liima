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
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class ApplistScreenDomainServiceTest {

    @Mock
    ApplistScreenDomainServiceQueries applistScreenDomainServiceQueries;

    @InjectMocks
    ApplistScreenDomainService applistScreenDomainService;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetApplicationServers() throws Exception {
        Tuple<List<ResourceEntity>, Long> applicationServers = applistScreenDomainService
                  .getAppServerResourcesWithApplications(0,  42, "*", true);
        Assert.assertTrue(applicationServers.getA().isEmpty());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetApplicationServerResources() throws Exception {
        Tuple<List<ResourceEntity>, Long> applicationServers = applistScreenDomainService
                  .getApplicationServerResources(0,  42, "*");
        Assert.assertTrue(applicationServers.getA().isEmpty());
    }
}
