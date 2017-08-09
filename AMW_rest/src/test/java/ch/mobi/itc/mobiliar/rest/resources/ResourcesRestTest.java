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

import ch.mobi.itc.mobiliar.rest.dtos.*;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    CopyResource copyResourceMock;

    @Mock
    ServerView serverViewMock;

    @Mock
    ResourceRelationsRest resourceRelationsMock;

    @Mock
    ResourcePropertiesRest resourcePropertiesMock;

    @Mock
    ResourceTemplatesRest resourceTemplatesRestMock;

    @Mock
    ReleaseLocator releaseLocatorMock;

    @Mock
    DeploymentBoundary deploymentBoundaryMock;

    @Before
    public void configure() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getResourcesWhenTypeIsNullAndNoRessourceGroupsShouldReturnEmptyResult() {
        // given
        String typeName = null;
        List<ResourceGroupEntity> resourceGroupEntities = new ArrayList<>();

        when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

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

        when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

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

        when(resourceGroupLocatorMock.getResourceGroups()).thenReturn(resourceGroupEntities);

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

        when(resourceGroupLocatorMock.getGroupsForType(typeName, Collections.EMPTY_LIST, true, true)).thenReturn(resourceGroupEntities);

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

        when(resourceGroupLocatorMock.getGroupsForType(typeName, Collections.EMPTY_LIST, true, true)).thenReturn(resourceGroupEntities);

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
        when(serverViewMock.getServers(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(list);
        
        // when
        BatchJobInventoryDTO result = rest.getBatchJobInventar(env, type, null, null, null, null, null);

        // then
        assertTrue(result != null);

    }

    @Test
    public void shouldNotAllowCreationOfNewResourcesWithoutRelease() {
        // given
        ResourceReleaseDTO resourceReleaseDTO = new ResourceReleaseDTO();
        resourceReleaseDTO.setName("Test");

        // when
        Response response = rest.addResource(resourceReleaseDTO);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void shouldReturnExpectedLocationHeaderOnSuccessfullResourceCreation() throws ResourceTypeNotFoundException, ResourceNotFoundException, ElementAlreadyExistsException {
        // given
        ResourceTypeEntity resType = new ResourceTypeEntity();
        resType.setName("APP");
        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resGroup.setName("Test");
        resGroup.setResourceType(resType);
        ReleaseEntity release = new ReleaseEntity();
        release.setName("TestRelease");
        release.setId(1);

        ResourceReleaseDTO resourceReleaseDTO = new ResourceReleaseDTO();
        resourceReleaseDTO.setName(resGroup.getName());
        resourceReleaseDTO.setType(resType.getName());
        resourceReleaseDTO.setReleaseName(release.getName());

        ResourceEntity  resEnt = new ResourceEntity();
        resEnt.setResourceGroup(resGroup);
        resEnt.setName(resGroup.getName());
        Resource resource = Resource.createByResource(ForeignableOwner.getSystemOwner(), resEnt, resType, new ContextEntity());
        Mockito.when(resourceBoundaryMock.createNewResourceByName(ForeignableOwner.getSystemOwner(), resGroup.getName(), resType.getName(), release.getName())).thenReturn(resource);

        // when
        Response response = rest.addResource(resourceReleaseDTO);

        // then
        assertEquals(CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getMetadata().get("Location").contains("/resources/" + resGroup.getName()));
    }

    @Test
    public void shouldNotAllowCreationOfResourceReleaseOfAResourcesWithEmptyName() {
        // given
        ResourceReleaseDTO resourceReleaseDTO = new ResourceReleaseDTO();
        resourceReleaseDTO.setName("");

        // when
        Response response = rest.addNewResourceRelease(resourceReleaseDTO, "TestRelease");

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void shouldNotAllowCreationOfResourceReleaseOfAResourcesWithoutOriginRelease() {
        // given
        ResourceReleaseDTO resourceReleaseDTO = new ResourceReleaseDTO();
        resourceReleaseDTO.setName("TestResource");

        // when
        Response response = rest.addNewResourceRelease(resourceReleaseDTO, "TestRelease");

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void shouldReturnExpectedLocationHeaderOnSuccessfullResourceReleaseCreation() throws AMWException, ForeignableOwnerViolationException {
        // given
        ResourceReleaseDTO resourceReleaseDTO = new ResourceReleaseDTO();
        resourceReleaseDTO.setName("TestApp");
        resourceReleaseDTO.setType("APP");
        resourceReleaseDTO.setReleaseName("TestRelease");
        String targetRelease = "AnotherRelease";

        CopyResourceResult copyResourceResult = new CopyResourceResult(resourceReleaseDTO.getName());
        Mockito.when(copyResourceMock.doCreateResourceRelease(resourceReleaseDTO.getName(), targetRelease,
                resourceReleaseDTO.getReleaseName(), ForeignableOwner.getSystemOwner())).thenReturn(copyResourceResult);

        // when
        Response response = rest.addNewResourceRelease(resourceReleaseDTO, targetRelease);

        // then
        assertEquals(CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getMetadata().get("Location").contains("/resources/" + resourceReleaseDTO.getName() + "/" +targetRelease));
    }

    @Test
    public void shouldReturnBadRequestIfReleaseCreationFailed() throws AMWException, ForeignableOwnerViolationException {
        // given
        ResourceReleaseDTO resourceReleaseDTO = new ResourceReleaseDTO();
        resourceReleaseDTO.setName("TestApp");
        resourceReleaseDTO.setType("APP");
        resourceReleaseDTO.setReleaseName("TestRelease");
        String targetRelease = "AnotherRelease";

        CopyResourceResult copyResourceResult = new CopyResourceResult(resourceReleaseDTO.getName());
        copyResourceResult.getExceptions().add("bogus");
        Mockito.when(copyResourceMock.doCreateResourceRelease(resourceReleaseDTO.getName(), targetRelease,
                resourceReleaseDTO.getReleaseName(), ForeignableOwner.getSystemOwner())).thenReturn(copyResourceResult);

        // when
        Response response = rest.addNewResourceRelease(resourceReleaseDTO, targetRelease);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeResourcesWithRightArgumentsOnGetClosestPastRelease() throws ValidationException {
        // given
        String resourceGroupName = "TEST";
        String releaseName = "RL-17.10";
        String env = "V";
        String resourceTypeName = "APPLICATION";
        ReleaseEntity closestRelease = new ReleaseEntity();
        closestRelease.setName("RL-16.10");
        when(resourceLocatorMock.getExactOrClosestPastReleaseByGroupNameAndRelease(resourceGroupName,releaseName)).thenReturn(closestRelease);

        // when
        rest.getExactOrClosestPastRelease(resourceGroupName, releaseName, env, resourceTypeName);

        // then
        verify(resourceRelationsMock).getResourceRelations(resourceGroupName, closestRelease.getName(), resourceTypeName);
        verify(resourcePropertiesMock).getResourceProperties(resourceGroupName, closestRelease.getName(), env);
        verify(resourceTemplatesRestMock).getResourceTemplates(resourceGroupName, closestRelease.getName(), "");
    }
  
    @Test
    public void shouldInvokeBoundaryWithRightArgumentsOnGetApplicationsWithVersionForRelease() throws ValidationException {
        // given
        Integer resourceGroupId = 8;
        Integer releaseId = 9;
        ResourceEntity appServer = new ResourceEntity();
        List<Integer> contextIds = Arrays.asList(1,2);
        ReleaseEntity release = new ReleaseEntity();
        release.setName("TestRelease");
        when(resourceLocatorMock.getExactOrClosestPastReleaseByGroupIdAndReleaseId(resourceGroupId, releaseId)).thenReturn(appServer);
        when(releaseLocatorMock.getReleaseById(releaseId)).thenReturn(release);

        // when
        rest.getApplicationsWithVersionForRelease(resourceGroupId, releaseId, contextIds);

        // then
        verify(deploymentBoundaryMock, times(1)).getVersions(appServer, contextIds, release);
    }

    @Test
    public void shouldNotAllowCopyFromResourceOfDifferentTypes() throws ValidationException, ForeignableOwnerViolationException, AMWException {
        // given
        String originResourceGroupName = "Origin";
        String originReleaseName = "From";
        String targetResourceGroupName = "Target";
        String targetReleaseName = "To";

        ResourceGroupEntity originResourceGroup = new ResourceGroupEntity();
        originResourceGroup.setName("Origin");
        ResourceEntity origin = new ResourceEntity();
        origin.setResourceGroup(originResourceGroup);
        origin.setName(originResourceGroupName);
        origin.setId(1);
        ResourceTypeEntity asType = new ResourceTypeEntity();
        asType.setName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
        origin.setResourceType(asType);

        ResourceGroupEntity targetResourceGroup = new ResourceGroupEntity();
        originResourceGroup.setName("Target");
        ResourceEntity target = new ResourceEntity();
        target.setResourceGroup(targetResourceGroup);
        target.setName(targetResourceGroupName);
        target.setId(2);
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName(DefaultResourceTypeDefinition.APPLICATION.name());
        target.setResourceType(appType);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(targetResourceGroupName, targetReleaseName)).thenReturn(target);
        when(resourceLocatorMock.getResourceByGroupNameAndRelease(originResourceGroupName, originReleaseName)).thenReturn(origin);
        when(copyResourceMock.doCopyResource(target.getId(), origin.getId(), ForeignableOwner.getSystemOwner())).thenThrow(new AMWException("Target and origin Resource are not of the same ResourceType"));

        // when
        Response response = rest.copyFromResource(targetResourceGroupName, targetReleaseName, originResourceGroupName, originReleaseName);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void shouldNotAllowCopyFromResourceOfNodeType() throws ValidationException {
        // given
        String originResourceGroupName = "Origin";
        String originReleaseName = "From";
        String targetResourceGroupName = "Target";
        String targetReleaseName = "To";

        ResourceGroupEntity originResourceGroup = new ResourceGroupEntity();
        originResourceGroup.setName("Origin");
        ResourceEntity origin = new ResourceEntity();
        origin.setResourceGroup(originResourceGroup);
        origin.setName(originResourceGroupName);
        ResourceTypeEntity nodeType = new ResourceTypeEntity();
        nodeType.setName(DefaultResourceTypeDefinition.NODE.name());
        origin.setResourceType(nodeType);

        ResourceGroupEntity targetResourceGroup = new ResourceGroupEntity();
        originResourceGroup.setName("Target");
        ResourceEntity target = new ResourceEntity();
        target.setResourceGroup(targetResourceGroup);
        target.setName(targetResourceGroupName);
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName(DefaultResourceTypeDefinition.NODE.name());
        target.setResourceType(nodeType);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(targetResourceGroupName, targetReleaseName)).thenReturn(target);
        when(resourceLocatorMock.getResourceByGroupNameAndRelease(originResourceGroupName, originReleaseName)).thenReturn(origin);

        // when
        Response response = rest.copyFromResource(targetResourceGroupName, targetReleaseName, originResourceGroupName, originReleaseName);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

}