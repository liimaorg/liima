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

package ch.puzzle.itc.mobiliar.business.softlinkRelation.entity;

import static org.junit.jupiter.api.Assertions.*;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.SoftlinkRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

/**
 * Tests {@link SoftlinkRelationEntity}
 */
public class SoftlinkRelationEntityTest {

    @Test
    public void getCopy_COPY() throws AMWException{
        // given
        String origSoftlinkRef = "origSoftlinkRef";
        ResourceEntity origCpiResource = (new ResourceEntityBuilder()).withName("origCpiResource").build();
        SoftlinkRelationEntity origin = (new SoftlinkRelationEntityBuilder()).withId(1).withOwner(ForeignableOwner.MAIA).withCpiResource(origCpiResource).withSoftlinkRef
                (origSoftlinkRef).build();

        String targetSoftlinkRef = "targetSoftlinkRef";
        ResourceEntity targetCpiResource = (new ResourceEntityBuilder()).withName("targetCpiResource").build();
        SoftlinkRelationEntity target = (new SoftlinkRelationEntityBuilder()).withId(2).withOwner(ForeignableOwner.AMW).withCpiResource(targetCpiResource).withSoftlinkRef
                (targetSoftlinkRef).build();

        CopyUnit copyUnit = new CopyUnit(origCpiResource, targetCpiResource, CopyResourceDomainService.CopyMode.COPY, ForeignableOwner.AMW);

        // when
        SoftlinkRelationEntity copy = origin.getCopy(target, copyUnit);

        // then
        assertNotNull(copy);
        assertEquals(origSoftlinkRef, copy.getSoftlinkRef());
        assertEquals(targetCpiResource, copy.getCpiResource());
        assertNotEquals(origin.getOwner(), copy.getOwner());
        assertNotEquals(origin.getId(), copy.getId());
    }


    @Test
    public void getCopy_RELEASE() throws AMWException {
        // given
        String origSoftlinkRef = "origSoftlinkRef";
        ResourceEntity origCpiResource = (new ResourceEntityBuilder()).withName("origCpiResource").build();
        SoftlinkRelationEntity origin = (new SoftlinkRelationEntityBuilder()).withId(1).withOwner(ForeignableOwner.MAIA).withCpiResource(origCpiResource).withSoftlinkRef
                (origSoftlinkRef).build();

        ResourceEntity targetCpiResource = (new ResourceEntityBuilder()).withName("targetResource").build();

        CopyUnit copyUnit = new CopyUnit(origCpiResource, targetCpiResource, CopyResourceDomainService.CopyMode.RELEASE, ForeignableOwner.AMW);

        // when
        SoftlinkRelationEntity copy = origin.getCopy(null, copyUnit);

        // then
        assertNotNull(copy);
        assertEquals(origSoftlinkRef, copy.getSoftlinkRef());
        assertEquals(targetCpiResource, copy.getCpiResource());
        assertEquals(origin.getOwner(), copy.getOwner());
        assertNotEquals(origin.getId(), copy.getId());
    }
}