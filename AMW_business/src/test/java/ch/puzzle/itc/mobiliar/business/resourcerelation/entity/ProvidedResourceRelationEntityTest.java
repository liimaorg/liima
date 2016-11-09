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

package ch.puzzle.itc.mobiliar.business.resourcerelation.entity;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainServiceTestHelper;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.utils.CopyHelper;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;


public class ProvidedResourceRelationEntityTest {

    private ProvidedResourceRelationEntity providedResourceRelationEntity;
    private ResourceRelationEntityBuilder relationEntityBuilder = new ResourceRelationEntityBuilder();

    @Test
    public void defaultConstructorShouldSetDefaultSystemOwner(){

        // when
        providedResourceRelationEntity = new ProvidedResourceRelationEntity();

        // then
        assertNotNull(providedResourceRelationEntity.getOwner());
        assertEquals(ForeignableOwner.getSystemOwner(), providedResourceRelationEntity.getOwner());
    }

    @Test
    public void constructorWithOwnerShouldSetOwner(){
        // given
        ForeignableOwner owner = ForeignableOwner.MAIA;

        // when
        providedResourceRelationEntity = new ProvidedResourceRelationEntity(owner);

        // then
        assertEquals(owner, providedResourceRelationEntity.getOwner());
    }

    @Test(expected = NullPointerException.class)
    public void constructorWithNullShouldThrowException(){
        // given
        ForeignableOwner owner = null;

        // when
        providedResourceRelationEntity = new ProvidedResourceRelationEntity(owner);
    }

    @Test
    public void test_getCopy() throws AMWException {
        Map<CopyResourceDomainService.CopyMode, Set<ForeignableOwner>> validModeOwnerCombinationsMap = CopyHelper
                .getValidModeOwnerCombinationsMap();
        for (CopyResourceDomainService.CopyMode copyMode : validModeOwnerCombinationsMap.keySet()) {
            for (ForeignableOwner actingOwner : validModeOwnerCombinationsMap.get(copyMode)) {
                shouldCopyMasterRelations_emptyTarget(copyMode, actingOwner);
                shouldCopyMasterRelations_existingTarget(copyMode, actingOwner);
                shouldCopySlaveRelations_emptyTarget(copyMode, actingOwner);
                shouldCopySlaveRelations_existingTarget(copyMode, actingOwner);
            }
        }
    }

    private void shouldCopyMasterRelations_emptyTarget(CopyResourceDomainService.CopyMode mode,
                                                       ForeignableOwner actingOwner) throws AMWException {
        // given
        ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();
        ResourceEntity targetMaster = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
                null, null, true);

        String identifier = "fooBar";
        ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
                "webservice", null);
        ProvidedResourceRelationEntity origin = relationEntityBuilder.buildProvidedResRelEntity(originMaster, originSlave, identifier, 1);
        CopyUnit copyUnit = new CopyUnit(originMaster, targetMaster, mode, actingOwner);

        // when
        ProvidedResourceRelationEntity copy = origin.getCopy(null, copyUnit);

        // then
        assertTrue(copyUnit.getResult().isSuccess());
        assertEquals(targetMaster, copy.getMasterResource());
        assertEquals(originSlave, copy.getSlaveResource());
        assertNotEquals(origin.getId(), copy.getId());
    }

    private void shouldCopyMasterRelations_existingTarget(CopyResourceDomainService.CopyMode mode,
                                                          ForeignableOwner actingOwner) throws AMWException {
        // given
        ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();
        ResourceEntity targetMaster = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
                null, null, true);

        String identifier = "fooBar";
        ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
                "webservice", null);
        ProvidedResourceRelationEntity origin = relationEntityBuilder.buildProvidedResRelEntity(
                originMaster, originSlave, identifier, 1);

        ResourceEntity targetSlave = new ResourceEntityBuilder().mockResourceEntity("wsTarget", null,
                "webservice", null);
        ProvidedResourceRelationEntity target = relationEntityBuilder.buildProvidedResRelEntity(
                targetMaster, targetSlave, identifier, 2);

        CopyUnit copyUnit = new CopyUnit(originMaster, targetMaster, mode, actingOwner);

        // when
        ProvidedResourceRelationEntity copy = origin.getCopy(target, copyUnit);

        // then
        assertTrue(copyUnit.getResult().isSuccess());
        assertEquals(targetMaster, copy.getMasterResource());
        // Predecessor Mode the target Slaveresource is copied
        if(mode.equals(CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR)){
            assertEquals(target.getSlaveResource(), copy.getSlaveResource());
        }else{
            assertEquals(originSlave, copy.getSlaveResource());
        }

        assertNotEquals(origin.getId(), copy.getId());
    }

    private void shouldCopySlaveRelations_emptyTarget(CopyResourceDomainService.CopyMode mode,
                                                      ForeignableOwner actingOwner) throws AMWException {
        // given
        ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();

        String identifier = "fooBar";
        ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
                "webservice", null);
        ProvidedResourceRelationEntity origin = relationEntityBuilder.buildProvidedResRelEntity(
                originMaster, originSlave, identifier, 1);

        ResourceEntity targetSlave = new ResourceEntityBuilder().buildResourceEntity("wsTarget", null,
                "webservice", null, true);

        CopyUnit copyUnit = new CopyUnit(originSlave, targetSlave, mode, actingOwner);

        // when
        ProvidedResourceRelationEntity copy = origin.getCopy(null, copyUnit);

        // then
        assertTrue(copyUnit.getResult().isSuccess());
        if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
            assertEquals(originMaster, copy.getMasterResource());
            assertEquals(targetSlave, copy.getSlaveResource());
            assertNotEquals(origin.getId(), copy.getId());
        }
        else {
            assertNull(copy);
        }
    }

    private void shouldCopySlaveRelations_existingTarget(CopyResourceDomainService.CopyMode mode,
                                                         ForeignableOwner actingOwner) throws AMWException {
        // given
        ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();
        ResourceEntity targetMaster = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
                null, null, true);

        String identifier = "foo";
        ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
                "webservice", null);
        ProvidedResourceRelationEntity origin = relationEntityBuilder.buildProvidedResRelEntity(
                originMaster, originSlave, identifier, 1);

        ResourceEntity targetSlave = new ResourceEntityBuilder().buildResourceEntity("wsTarget", null,
                "webservice", null, true);
        ProvidedResourceRelationEntity target = relationEntityBuilder.buildProvidedResRelEntity(
                targetMaster, targetSlave, identifier, 2);

        CopyUnit copyUnit = new CopyUnit(originSlave, targetSlave, mode, actingOwner);

        // when
        ProvidedResourceRelationEntity copy = origin.getCopy(target, copyUnit);

        // then
        assertTrue(copyUnit.getResult().isSuccess());
        if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
            assertEquals(originMaster, copy.getMasterResource());
            assertEquals(targetSlave, copy.getSlaveResource());
            assertNotEquals(origin.getId(), copy.getId());
        }
        else {
            assertNull(copy);
        }
    }
}