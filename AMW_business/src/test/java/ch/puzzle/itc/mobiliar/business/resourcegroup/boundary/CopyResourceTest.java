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

package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceGroupEntityBuilder;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.integration.entity.util.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class CopyResourceTest {

    @InjectMocks
    CopyResource copyResource;

    @Mock
    CommonDomainService commonDomainService;

    @Mock
    CopyResourceDomainService copyResourceDomainService;

    @Mock
    PermissionBoundary permissionBoundary;

    @Mock
    ResourceLocator resourceLocatorMock;



    @Test
    public void shouldNotAllowCopyFromResourceOfDifferentType() throws Exception {
        //given
        String targetResourceName = "target";
        Integer targetResourceId = 2;
        ResourceEntity targetResource = ResourceEntityBuilder.createResourceEntity(targetResourceName, targetResourceId);
        ResourceTypeEntity asType = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;
        targetResource.setResourceType(asType);

        String originResourceName = "origin";
        Integer originResourceId = 1;
        ResourceEntity originResource = ResourceEntityBuilder.createResourceEntity(originResourceName, originResourceId);
        ResourceTypeEntity appType = ResourceTypeEntityBuilder.APPLICATION_TYPE;
        originResource.setResourceType(appType);

        when(commonDomainService.getResourceEntityById(targetResourceId)).thenReturn(targetResource);
        when(commonDomainService.getResourceEntityById(originResourceId)).thenReturn(originResource);

        // when // then
        assertThrows(AMWException.class, () -> {
            copyResource.doCopyResource(targetResourceId, originResourceId);
        });
    }

    @Test
    public void shouldThrowNotAuthorizedExceptionWhenPermissionIsDenied() throws AMWException {
        // given
        ResourceEntity originResourceEntity = mock(ResourceEntity.class);
        ResourceEntity targetResourceEntity = mock(ResourceEntity.class);
        ResourceTypeEntity resourceType = mock(ResourceTypeEntity.class);
        when(targetResourceEntity.getResourceType()).thenReturn(resourceType);
        when(originResourceEntity.getResourceType()).thenReturn(resourceType);

        // when
        assertThrows(NotAuthorizedException.class, () -> {
            copyResource.doCopyResource(targetResourceEntity, originResourceEntity);
        });

        // then
        verify(copyResourceDomainService, never()).copyFromOriginToTargetResource(originResourceEntity, targetResourceEntity);
    }

    @Test
    public void shouldInvokePermissionServiceAndCopyResourceDomainServiceWithCorrectParams() throws Exception {
        //given
        String targetResourceName = "target";
        Integer targetResourceId = 2;
        ResourceEntity targetResource = ResourceEntityBuilder.createResourceEntity(targetResourceName, targetResourceId);
        ResourceTypeEntity asType = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;
        targetResource.setResourceType(asType);
        Set<ResourceEntity> set = new HashSet<>();
        set.add(targetResource);
        ResourceGroupEntity targetGroup = new ResourceGroupEntityBuilder().buildResourceGroupEntity(targetResourceName, set, false);
        targetResource.setResourceGroup(targetGroup);

        String originResourceName = "origin";
        Integer originResourceId = 1;
        ResourceEntity originResource = ResourceEntityBuilder.createResourceEntity(originResourceName, originResourceId);
        originResource.setResourceType(asType);

        when(commonDomainService.getResourceEntityById(targetResourceId)).thenReturn(targetResource);
        when(commonDomainService.getResourceEntityById(originResourceId)).thenReturn(originResource);
        when(permissionBoundary.canCopyFromSpecificResource(originResource, targetGroup)).thenReturn(true);

        // when
        copyResource.doCopyResource(targetResourceId, originResourceId);

        // then
        verify(permissionBoundary, times(1)).canCopyFromSpecificResource(originResource, targetGroup);
        verify(copyResourceDomainService, times(1)).copyFromOriginToTargetResource(originResource, targetResource);
    }

    @Test
    public void shouldNotAllowCopyFromWhenTargetNotFound() throws ValidationException {
        // given
        when(resourceLocatorMock.getResourceByGroupNameAndRelease(anyString(), anyString())).thenReturn(null);

        // when
        Throwable exception = assertThrows(ResourceNotFoundException.class, () -> {
            copyResource.doCopyResource("targetResourceGroupName", "targetReleaseName", "originResourceGroupName", "originReleaseName");
        });

        // then
        assertEquals(exception.getMessage(), "Target Resource not found");
    }

    @Test
    public void shouldNotAllowCopyFromWhenOriginNotFound() throws ValidationException {
        // given
        ResourceEntity targetResourceEntity = mock(ResourceEntity.class);
        when(resourceLocatorMock.getResourceByGroupNameAndRelease("targetResourceGroupName", "targetReleaseName")).thenReturn(targetResourceEntity);
        when(resourceLocatorMock.getResourceByGroupNameAndRelease("originResourceGroupName", "originReleaseName")).thenReturn(null);

        // when
        Throwable exception = assertThrows(ResourceNotFoundException.class, () -> {
            copyResource.doCopyResource("targetResourceGroupName", "targetReleaseName", "originResourceGroupName", "originReleaseName");
        });

        // then
        assertEquals(exception.getMessage(), "Origin Resource not found");
    }

}
