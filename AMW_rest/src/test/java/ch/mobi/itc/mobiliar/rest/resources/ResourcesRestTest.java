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

package ch.mobi.itc.mobiliar.rest.resources;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.mobi.itc.mobiliar.rest.dtos.BatchJobInventoryDTO;
import ch.mobi.itc.mobiliar.rest.dtos.BatchResourceDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;

public class ResourcesRestTest {

    @InjectMocks
    ResourcesRest rest;

    @Mock
    ResourceGroupLocator resourceGroupLocatorMock;

    @Mock
    ResourceLocator resourceLocatorMock;
    
    @Mock
    ServerView serverViewMock;

    @Before
    public void configure() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getResourcesWhenTypeIsNullAndNoRessourceGroupsShouldReturnEmptyResult() {
        // given
        String typeName = null;
        List<ResourceGroupEntity> resourceGroupEntities = new ArrayList<>();

        Mockito.when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

        // when
        List<ResourceDTO> resourcesResult = rest.getResources(typeName);

        // then
        Assert.assertTrue(resourcesResult.isEmpty());
    }

    @Test
    public void getResourcesWhenTypeIsNullShouldReturnAllGroups() {
        // given
        String typeName = null;
        List<ResourceGroupEntity> resourceGroupEntities = new ArrayList<>();
        resourceGroupEntities.add(createResourceGroupEntity("name", "type"));
        resourceGroupEntities.add(createResourceGroupEntity("name2", "type2"));

        Mockito.when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

        // when
        List<ResourceDTO> resourcesResult = rest.getResources(typeName);

        // then
        Assert.assertFalse(resourcesResult.isEmpty());
        Assert.assertEquals(resourceGroupEntities.size(), resourcesResult.size());
    }

    @Test
    public void getResourcesWhenTypeIsNullAndFoundResourceGroupTypeIsNullShouldReturnTheGroup() {
        // given
        String typeName = null;
        List<ResourceGroupEntity> resourceGroupEntities = new ArrayList<>();
        String groupName = "name";
        resourceGroupEntities.add(createResourceGroupEntity(groupName, null));

        Mockito.when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

        // when
        List<ResourceDTO> resourcesResult = rest.getResources(typeName);

        // then
        Assert.assertFalse(resourcesResult.isEmpty());
        Assert.assertEquals(resourceGroupEntities.get(0).getName(), resourcesResult.get(0).getName());
        Assert.assertEquals(groupName, resourcesResult.get(0).getName());
        Assert.assertNull(resourcesResult.get(0).getType());
    }

    @Test
    public void getResourcesForTypeWhereTypeExistsShouldReturnGroup() {
        // given
        String typeName = "type";
        String groupName = "name";
        List<ResourceGroupEntity> resourceGroupEntities = new ArrayList<>();
        resourceGroupEntities.add(createResourceGroupEntity(groupName, typeName));
        resourceGroupEntities.add(createResourceGroupEntity("name2", "type2"));

        Mockito.when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

        // when
        List<ResourceDTO> resourcesResult = rest.getResources(typeName);

        // then
        Assert.assertEquals(1, resourcesResult.size());
        Assert.assertEquals(groupName, resourcesResult.get(0).getName());
        Assert.assertEquals(typeName, resourcesResult.get(0).getType());
    }

    @Test
    public void getResourcesForTypeWhereTypeDoesNotExistsShouldReturnEmptyResult() {
        // given
        String typeName = "type";
        String groupName = "name";
        List<ResourceGroupEntity> resourceGroupEntities = new ArrayList<>();
        resourceGroupEntities.add(createResourceGroupEntity("otherName", "otherType"));
        resourceGroupEntities.add(createResourceGroupEntity("otherName2", "otherType2"));

        Mockito.when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

        // when
        List<ResourceDTO> resourcesResult = rest.getResources(typeName);

        // then
        Assert.assertTrue(resourcesResult.isEmpty());
    }

    private ResourceGroupEntity createResourceGroupEntity(String name, String type) {
        ResourceGroupEntity resourceGroupEntity = new ResourceGroupEntity();
        resourceGroupEntity.setName(name);
        if (type != null) {
            ResourceTypeEntity resourceType = new ResourceTypeEntity();
            resourceType.setName(type);
            resourceGroupEntity.setResourceType(resourceType);
        }
        return resourceGroupEntity;
    }

    @Test
    public void getBatchJobInventoryError() {
        // given
        String env = null;
        Integer resource = null;
        String appFilter = null;
        String jobFilter = null;
        String relFilter = null;
        String dbFilter = null;
        String wsFilter = null;
        // when
        try {
            // BatchJobInventoryDTO dto =
            rest.getBatchJobInventar(env, resource, appFilter, jobFilter, relFilter, dbFilter, wsFilter);
            Assert.fail("Muss Exception werfen.");
        } catch (ValidationException e) {
            // then expect this
            Assert.assertTrue(e.getMessage().equals("Der Parameter 'resource' muss numerisch sein"));
        }
    }

    @Test
    public void getBatchJobResources() throws ValidationException {
        // given
        BatchResourceDTO d = Mockito.mock(BatchResourceDTO.class);
        List<BatchResourceDTO> list = new ArrayList<>();
        list.add(d);
        String app = "app";

        // when
        List<BatchResourceDTO> result = rest.getBatchJobResources(app);

        // then
        Assert.assertTrue(result != null && result.size() == 0);

    }

    @Test
    public void getBatchJobInventoryEmpty() throws ValidationException {
        // given
        String env = "V";
        Integer type = 2305;
        List<ServerTuple> list = new ArrayList<>();
        Mockito.when(serverViewMock.getServers(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.eq(true))).thenReturn(list);
        
        // when
        BatchJobInventoryDTO result = rest.getBatchJobInventar(env, type, null, null, null, null, null);

        // then
        Assert.assertTrue(result != null);

    }

}