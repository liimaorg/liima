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
import java.util.Collections;
import java.util.List;

import ch.mobi.itc.mobiliar.rest.dtos.ReleaseDTO;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
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

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourcesRestTest {

    @InjectMocks
    ResourcesRest rest;

    @Mock
    ResourceGroupLocator resourceGroupLocatorMock;

    @Mock
    ResourceLocator resourceLocatorMock;

    @Mock
    ResourceBoundary resourceBoundaryMock;
    
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
        assertTrue(resourcesResult.isEmpty());
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

        Mockito.when(resourceGroupLocatorMock.getGroupsForType(typeName, Collections.EMPTY_LIST, true, true)).thenReturn(resourceGroupEntities);

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

        Mockito.when(resourceGroupLocatorMock.getGroupsForType(typeName, Collections.EMPTY_LIST, true, true)).thenReturn(resourceGroupEntities);

        // when
        List<ResourceDTO> resourcesResult = rest.getResources(typeName);

        // then
        assertTrue(resourcesResult.isEmpty());
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
            assertTrue(e.getMessage().equals("Der Parameter 'resource' muss numerisch sein"));
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
        assertTrue(result != null && result.size() == 0);

    }

    @Test
    public void getBatchJobInventoryEmpty() throws ValidationException {
        // given
        String env = "V";
        Integer type = 2305;
        List<ServerTuple> list = new ArrayList<>();
        Mockito.when(serverViewMock.getServers(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(list);
        
        // when
        BatchJobInventoryDTO result = rest.getBatchJobInventar(env, type, null, null, null, null, null);

        // then
        assertTrue(result != null);

    }

    @Test
    public void shouldNotAllowCreationOfNewResourcesWithExistingId() {
        // given
        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resGroup.setId(1);
        ResourceDTO resourceDTO = new ResourceDTO(resGroup, Collections.EMPTY_LIST);

        // when
        Response response = rest.addResource(resourceDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void shouldNotAllowCreationOfNewResourcesWithoutRelease() {
        // given
        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resGroup.setName("Test");
        ResourceDTO resourceDTO = new ResourceDTO(resGroup, Collections.EMPTY_LIST);

        // when
        Response response = rest.addResource(resourceDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void shouldReturnExpectedLocationHeaderOnSuccess() throws ResourceTypeNotFoundException, ResourceNotFoundException, ElementAlreadyExistsException {
        // given
        ResourceTypeEntity resType = new ResourceTypeEntity();
        resType.setName("APP");
        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resGroup.setName("Test");
        resGroup.setResourceType(resType);
        ReleaseEntity release = new ReleaseEntity();
        release.setName("TestRelease");
        release.setId(1);
        ReleaseDTO releaseDto = new ReleaseDTO(release);
        List<ReleaseDTO> releaseDtos = new ArrayList<>();
        releaseDtos.add(releaseDto);
        ResourceDTO resourceDTO = new ResourceDTO(resGroup, releaseDtos);

        ResourceEntity  resEnt = new ResourceEntity();
        resEnt.setResourceGroup(resGroup);
        resEnt.setName(resGroup.getName());
        Resource resource = Resource.createByResource(ForeignableOwner.getSystemOwner(), resEnt, resType, new ContextEntity());
        Mockito.when(resourceBoundaryMock.createNewResourceByName(ForeignableOwner.getSystemOwner(), resGroup.getName(), resType.getName(), release.getName())).thenReturn(resource);

        // when
        Response response = rest.addResource(resourceDTO);

        // then
        assertEquals(CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getMetadata().get("Location").contains("/resources/"+resGroup.getName()));
    }

}