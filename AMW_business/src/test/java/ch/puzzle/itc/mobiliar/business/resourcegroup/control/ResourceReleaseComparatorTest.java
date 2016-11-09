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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import static org.junit.Assert.*;

import java.util.Date;

import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

public class ResourceReleaseComparatorTest {
    private static final ReleaseEntity MAIN_RELEASE_A = ReleaseEntityBuilder.createMainReleaseEntity("Beta", 10, null);
    private static final ReleaseEntity MAIN_RELEASE_B = ReleaseEntityBuilder.createMainReleaseEntity("Beta", 20, new Date(20_000_000));
    private static final ReleaseEntity MINOR_RELEASE_B_1 = ReleaseEntityBuilder.createMinorReleaseEntity("MinorBeta1", 21, new Date(20_000_000));
    private static final ReleaseEntity MAIN_RELEASE_C = ReleaseEntityBuilder.createMainReleaseEntity("Gamma", 30, new Date(30_000_000));

    private static final String APP_GROUP_NAME = "appname";
    private static final ResourceEntity RESOURCE_MAIN_RELEASE_A = new ResourceEntityBuilder().withName(APP_GROUP_NAME).withRelease(MAIN_RELEASE_A).build();
    private static final ResourceEntity RESOURCE_MAIN_RELEASE_B = new ResourceEntityBuilder().withName(APP_GROUP_NAME).withRelease(MAIN_RELEASE_B).build();
    private static final ResourceEntity RESOURCE_MINOR_RELEASE_B_1 = new ResourceEntityBuilder().withName(APP_GROUP_NAME).withRelease(MINOR_RELEASE_B_1).build();
    private static final ResourceEntity RESOURCE_MAIN_RELEASE_C = new ResourceEntityBuilder().withName(APP_GROUP_NAME).withRelease(MAIN_RELEASE_C).build();

    private ResourceReleaseComparator comparator;

    @Before
    public void setUp(){
        comparator = new ResourceReleaseComparator();
    }

    @Test
    public void compareResourceReleaseBeforeShouldReturnBefore(){
        // given
        assertTrue(MAIN_RELEASE_B.getInstallationInProductionAt().before(MAIN_RELEASE_C.getInstallationInProductionAt()));

        // when
        int compare = comparator.compare(RESOURCE_MAIN_RELEASE_B, RESOURCE_MAIN_RELEASE_C);

        // then
        assertEquals(-1, compare);
    }


    @Test
    public void compareResourceReleaseWithoutDateShouldReturnEquals(){
        // given
        assertTrue(MAIN_RELEASE_B.getInstallationInProductionAt().equals(MINOR_RELEASE_B_1.getInstallationInProductionAt()));

        // when
        int compare = comparator.compare(RESOURCE_MAIN_RELEASE_B, RESOURCE_MINOR_RELEASE_B_1);

        // then
        assertEquals(0, compare);
    }

    @Test
    public void compareResourceReleaseWithSameDateShouldReturnEquals(){
        // given
        assertNull(MAIN_RELEASE_A.getInstallationInProductionAt());

        // when
        int compare = comparator.compare(RESOURCE_MAIN_RELEASE_A, RESOURCE_MAIN_RELEASE_A);

        // then
        assertEquals(0, compare);
    }

    @Test
    public void compareResourceReleaseWhereFirstHasDateAndSecondHasNullDateShouldReturnBefore(){
        // given
        assertNull(MAIN_RELEASE_A.getInstallationInProductionAt());
        assertNotNull(MAIN_RELEASE_B.getInstallationInProductionAt());

        // when
        int compare = comparator.compare(RESOURCE_MAIN_RELEASE_B, RESOURCE_MAIN_RELEASE_A);

        // then
        assertEquals(-1, compare);
    }

    @Test
    public void compareResourceReleaseWhereSecondHasDateAndFirstHasNullDateShouldReturnAfter(){
        // given
        assertNull(MAIN_RELEASE_A.getInstallationInProductionAt());
        assertNotNull(MAIN_RELEASE_B.getInstallationInProductionAt());

        // when
        int compare = comparator.compare(RESOURCE_MAIN_RELEASE_A, RESOURCE_MAIN_RELEASE_B);

        // then
        assertEquals(1, compare);
    }

}