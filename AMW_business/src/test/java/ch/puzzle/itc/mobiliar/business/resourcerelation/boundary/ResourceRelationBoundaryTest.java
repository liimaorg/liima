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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationRepository;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ResourceRelationBoundaryTest {

    @InjectMocks
    ResourceRelationBoundary resourceRelationBoundary;

    @Mock
    ResourceRelationRepository resourceRelationRepository;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldBeAddableAsProvidedResourceIfItsNotProvidedByAnyGroup() throws Exception {
        // given
        ResourceEntity master = new ResourceEntity();
        String slaveName = "slave";

        when(resourceRelationRepository.getResourceRelationOfOtherMasterResourceGroupsBySlaveResourceGroupName(master, slaveName)).thenReturn(Collections.EMPTY_LIST);

        // when // then
        assertTrue(resourceRelationBoundary.isAddableAsProvidedResourceToResourceGroup(master, slaveName));
    }

    @Test
    public void shouldBeAddableAsProvidedResourceIfItsProvidedBySameGroup() throws Exception {
        // given
        ResourceGroupEntity masterGroup = new ResourceGroupEntity();
        masterGroup.setName("master");
        ResourceEntity master = new ResourceEntity();
        master.setResourceGroup(masterGroup);
        master.setName(masterGroup.getName());
        String slaveName = "slave";

        ResourceEntity sameMasterGroup = new ResourceEntity();
        sameMasterGroup.setResourceGroup(masterGroup);
        sameMasterGroup.setName(masterGroup.getName());

        ProvidedResourceRelationEntity provided = new ProvidedResourceRelationEntity();
        provided.setMasterResource(sameMasterGroup);
        List<ProvidedResourceRelationEntity> list = new ArrayList<>();
        list.add(provided);

        when(resourceRelationRepository.getResourceRelationOfOtherMasterResourceGroupsBySlaveResourceGroupName(master, slaveName)).thenReturn(list);

        // when // then
        assertTrue(resourceRelationBoundary.isAddableAsProvidedResourceToResourceGroup(master, slaveName));
    }

    @Test
    public void shouldNotBeAddableAsProvidedResourceIfItsProvidedByAnotherGroup() throws Exception {
        // given
        ResourceGroupEntity masterGroup = new ResourceGroupEntity();
        masterGroup.setName("master");
        ResourceEntity master = new ResourceEntity();
        master.setResourceGroup(masterGroup);
        master.setName(masterGroup.getName());
        String slaveName = "slave";

        ResourceGroupEntity anotherMasterGroup = new ResourceGroupEntity();
        anotherMasterGroup.setName("anotherMaster");
        ResourceEntity anotherMaster = new ResourceEntity();
        anotherMaster.setResourceGroup(anotherMasterGroup);
        anotherMaster.setName(anotherMasterGroup.getName());

        ProvidedResourceRelationEntity provided = new ProvidedResourceRelationEntity();
        provided.setMasterResource(anotherMaster);
        List<ProvidedResourceRelationEntity> list = new ArrayList<>();
        list.add(provided);

        when(resourceRelationRepository.getResourceRelationOfOtherMasterResourceGroupsBySlaveResourceGroupName(master, slaveName)).thenReturn(list);

        // when // then
        assertFalse(resourceRelationBoundary.isAddableAsProvidedResourceToResourceGroup(master, slaveName));
    }

}