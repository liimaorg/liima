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

package ch.puzzle.itc.mobiliar.business.utils;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceGroupEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests {@link CopyHelper}
 */
public class CopyHelperTest {

    ResourceGroupEntityBuilder resourceGroupEntityBuilder;

    @BeforeEach
    public void setUp(){
        resourceGroupEntityBuilder = new ResourceGroupEntityBuilder();
    }

    @Test
    public void copyForeignable_copy() throws AMWException{
        // given
        Foreignable origin = new PropertyDescriptorEntityBuilder().withOwner(ForeignableOwner.MAIA).build();
        origin.setExternalLink("originLink");
        origin.setExternalKey("originKey");

        Foreignable target = new PropertyDescriptorEntityBuilder().withOwner(ForeignableOwner.AMW).build();

        ForeignableOwner actingOwner = ForeignableOwner.AMW;
        CopyUnit copyUnit = new CopyUnit(null, new ResourceEntityBuilder().withName("targetResource").build(), CopyResourceDomainService.CopyMode.COPY, ForeignableOwner.AMW);

        // when
        CopyHelper.copyForeignable(target, origin, copyUnit);

        // then
        assertEquals(actingOwner, target.getOwner());
        assertNull(target.getExternalLink());
        assertNull(target.getExternalKey());
    }

    @Test
    @Disabled
    public void copyForeignable_release() throws AMWException{
        // TODO: cweber FIXME!
        // given
        Foreignable origin = new PropertyDescriptorEntityBuilder().withOwner(ForeignableOwner.MAIA).build();
        origin.setExternalLink("originLink");
        origin.setExternalKey("originKey");

        Foreignable target = new PropertyDescriptorEntityBuilder().withOwner(ForeignableOwner.AMW).build();

        ForeignableOwner actingOwner = ForeignableOwner.AMW;
        CopyUnit copyUnit = new CopyUnit(null, new ResourceEntityBuilder().withName("targetResource").build(), CopyResourceDomainService.CopyMode.COPY, actingOwner);

        // when
        CopyHelper.copyForeignable(target, origin, copyUnit);

        // then
        assertEquals(origin.getOwner(), target.getOwner());
        assertEquals(origin.getExternalKey(), target.getExternalKey());
        assertEquals(origin.getExternalLink(), target.getExternalLink());
    }

    @Test
    public void copyForeignable_resourceCopy() throws AMWException{
        // given
        ResourceGroupEntity originGroup = resourceGroupEntityBuilder.buildResourceGroupEntity("origin", null, true);
        ResourceEntity origin = new ResourceEntityBuilder().withOwner(ForeignableOwner.MAIA).forResourceGroup(originGroup).build();
        origin.setExternalLink("originLink");
        origin.setExternalKey("originKey");

        ResourceGroupEntity targetGroup = resourceGroupEntityBuilder.buildResourceGroupEntity("target", null, true);
        ResourceEntity target = new ResourceEntityBuilder().forResourceGroup(targetGroup).build();

        ForeignableOwner actingOwner = ForeignableOwner.AMW;
        CopyUnit copyUnit = new CopyUnit(origin, target, CopyResourceDomainService.CopyMode.COPY, actingOwner);

        // when
        CopyHelper.copyForeignable(target, origin, copyUnit);

        // then
        assertEquals(actingOwner, target.getOwner());
        assertNull(target.getExternalKey());
        assertNull(target.getExternalLink());
    }

    @Test
    public void copyForeignable_resourceRelease() throws AMWException{
        // given
        ResourceGroupEntity originGroup = resourceGroupEntityBuilder.buildResourceGroupEntity("origin", null, true);
        ResourceEntity origin = new ResourceEntityBuilder().withOwner(ForeignableOwner.MAIA).forResourceGroup(originGroup).build();
        origin.setExternalLink("originLink");
        origin.setExternalKey("originKey");

        ResourceGroupEntity targetGroup = resourceGroupEntityBuilder.buildResourceGroupEntity("target", null, true);
        ResourceEntity target = new ResourceEntityBuilder().forResourceGroup(targetGroup).build();

        ForeignableOwner actingOwner = ForeignableOwner.AMW;
        CopyUnit copyUnit = new CopyUnit(origin, target, CopyResourceDomainService.CopyMode.RELEASE, actingOwner);

        // when
        CopyHelper.copyForeignable(target, origin, copyUnit);

        // then
        assertEquals(actingOwner, target.getOwner());
        assertEquals(origin.getExternalKey(), target.getExternalKey());
        assertEquals(origin.getExternalLink(), target.getExternalLink());
    }

    @Test
    public void test_equalsWithNullCheck() {
        // given
        Integer nill = null;
        Integer nell = null;
        Integer one = 1;
        Integer eins = 1;
        Integer two = 2;

        // when then
        assertTrue(CopyHelper.equalsWithNullCheck(nill, nell));
        assertTrue(CopyHelper.equalsWithNullCheck(one, eins));
        assertFalse(CopyHelper.equalsWithNullCheck(one, null));
        assertFalse(CopyHelper.equalsWithNullCheck(one, two));
        assertFalse(CopyHelper.equalsWithNullCheck(null, two));
    }
}
