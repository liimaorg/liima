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

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ResourceEntityTest {

    @Mock
    ResourceEntity resourceEntity;

    ResourceEntity slaveResourceEntity = ResourceFactory.createNewResource();
    ResourceEntity masterResource = ResourceFactory.createNewResource();
    ResourceRelationTypeEntity relationTypeEntity = new ResourceRelationTypeEntity();
    ProvidedResourceRelationEntity providedResourceRelationEntity = new ProvidedResourceRelationEntity();
    private ResourceEntityBuilder resourceEntityBuilder = new ResourceEntityBuilder();

    @BeforeEach
    public void setUp() {
        providedResourceRelationEntity.setResourceRelationType(relationTypeEntity);
        providedResourceRelationEntity.setMasterResource(masterResource);
        providedResourceRelationEntity.setSlaveResource(slaveResourceEntity);
    }

    @Test
    public void testAddProvidedResourceRelation() {
        assertEquals(relationTypeEntity, providedResourceRelationEntity.getResourceRelationType());
        assertEquals(slaveResourceEntity, providedResourceRelationEntity.getSlaveResource());
    }

    @Test
    public void testGetRelationById() {
        // given
        Mockito.when(resourceEntity.getRelationById(ArgumentMatchers.anyInt(), ArgumentMatchers.anySet()))
                .thenCallRealMethod();

        final int slaveResourceId = 21;

        final Set<ConsumedResourceRelationEntity> relations = new HashSet<ConsumedResourceRelationEntity>();
        final ConsumedResourceRelationEntity resourceRelation = Mockito.mock(ConsumedResourceRelationEntity.class);
        relations.add(resourceRelation);
        final ResourceEntity slaveResource = Mockito.mock(ResourceEntity.class);
        Mockito.when(slaveResource.getId()).thenReturn(slaveResourceId);
        Mockito.when(resourceRelation.getSlaveResource()).thenReturn(slaveResource);

        // when
        final AbstractResourceRelationEntity result = resourceEntity.getRelationById(slaveResourceId, relations);

        // then
        assertEquals(resourceRelation, result);
    }

    @Test
    public void testGetMasterRelation() {
        // given
        Mockito.when(
                resourceEntity.getMasterRelation(ArgumentMatchers.any(ResourceEntity.class), ArgumentMatchers.anySet()))
                .thenCallRealMethod();

        final int slaveResourceId = 21;
        final Set<ConsumedResourceRelationEntity> relations = new HashSet<ConsumedResourceRelationEntity>();
        final ConsumedResourceRelationEntity resourceRelation = Mockito.mock(ConsumedResourceRelationEntity.class);
        relations.add(resourceRelation);
        final ResourceEntity slaveResource = Mockito.mock(ResourceEntity.class);
        Mockito.when(slaveResource.getId()).thenReturn(slaveResourceId);
        Mockito.when(resourceRelation.getSlaveResource()).thenReturn(slaveResource);

        // when
        final AbstractResourceRelationEntity result = resourceEntity.getMasterRelation(slaveResource, relations);

        // then
        assertEquals(resourceRelation, result);
    }

    private ConsumedResourceRelationEntity createConsumedResourceRelation(final int masterResourceId,
            final int slaveResourceId) {
        final ConsumedResourceRelationEntity resourceRelation = Mockito.mock(ConsumedResourceRelationEntity.class);
        final ResourceEntity masterResource = Mockito.mock(ResourceEntity.class);
        Mockito.when(masterResource.getId()).thenReturn(masterResourceId);
        Mockito.when(resourceRelation.getMasterResource()).thenReturn(masterResource);
        return resourceRelation;
    }

    private ConsumedResourceRelationEntity prepareConsumedSlaveRelationTest() {
        Mockito.when(resourceEntity.getConsumedSlaveRelation(ArgumentMatchers.any(ResourceEntity.class)))
                .thenCallRealMethod();

        final ConsumedResourceRelationEntity consumedResourceRelationEntity = createConsumedResourceRelation(11, 12);

        final Set<ConsumedResourceRelationEntity> relations = new HashSet<ConsumedResourceRelationEntity>();
        relations.add(consumedResourceRelationEntity);

        Mockito.when(resourceEntity.getConsumedSlaveRelations()).thenReturn(relations);

        return consumedResourceRelationEntity;
    }

    @Test
    public void testGetConsumedSlaveRelation() {
        ConsumedResourceRelationEntity consumedResourceRelationEntity = prepareConsumedSlaveRelationTest();

        ResourceEntity queryEntity = ResourceFactory.createNewResource("testName");
        queryEntity.setId(11);

        assertEquals(consumedResourceRelationEntity, resourceEntity.getConsumedSlaveRelation(queryEntity));
    }

    @Test
    public void testGetConsumedSlaveRelationWithoutName() {
        prepareConsumedSlaveRelationTest();

        final ResourceEntity queryEntity = ResourceFactory.createNewResource();
        queryEntity.setId(11);

        assertNull(resourceEntity.getConsumedSlaveRelation(queryEntity));
    }

    @Test
    public void testGetConsumedSlaveRelationWithAppserverDisplayName() {
        prepareConsumedSlaveRelationTest();

        ResourceEntity queryEntity = ResourceFactory
                .createNewResource(ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName());
        queryEntity.setId(11);

        assertNull(resourceEntity.getConsumedSlaveRelation(queryEntity));
    }

    @Test
    public void testGetConsumedSlaveRelationWithWrongResource() {
        prepareConsumedSlaveRelationTest();

        final ResourceEntity queryEntity = ResourceFactory
                .createNewResource(ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName());
        queryEntity.setId(12);

        assertNull(resourceEntity.getConsumedSlaveRelation(queryEntity));
    }

    @Test
    public void copyResourceEntity_resources_should_be_same_type_COPY() throws AMWException {
        copyResourceEntity_resources_should_be_same_type(CopyResourceDomainService.CopyMode.COPY);
    }

    @Test
    public void copyResourceEntity_resources_should_be_same_type_RELEASE() throws AMWException {
        copyResourceEntity_resources_should_be_same_type(CopyResourceDomainService.CopyMode.RELEASE);
    }

    private void copyResourceEntity_resources_should_be_same_type(CopyResourceDomainService.CopyMode copying)
            throws AMWException {
        // given
        ResourceEntity originResource = resourceEntityBuilder.buildAppServerEntity("origin", null, null, true);
        ResourceEntity targetResource = resourceEntityBuilder.buildApplicationEntity(
                "appTargetResource", null, null, true);
        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, copying);

        // when
        originResource.getCopy(targetResource, copyUnit);

        // then
        assertFalse(copyUnit.getResult().isSuccess());
        Set<CopyResourceResult.CopyFailure> failures = copyUnit.getResult().getExceptionEnums();
        assertTrue(failures.contains(CopyResourceResult.CopyFailure.RESOURCETYPE_DIFF));
    }

}