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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.puzzle.itc.mobiliar.business.domain.applist.ApplistScreenDomainService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.usersettings.control.UserSettingsService;
import ch.puzzle.itc.mobiliar.business.usersettings.entity.UserSettingsEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public class ResourceRelationsTest {

    @Mock
    ResourceDependencyResolverService dependencyResolverService;

    @Mock
    ApplistScreenDomainService applistScreenDomainService;

    @Mock
    UserSettingsService userSettingsService;

    @Mock
    PermissionService permissionService;

    @Spy
    @InjectMocks
    ResourceRelations service;

    ReleaseEntity release;
    ResourceGroupEntity asGrp;
    ResourceEntity as;
    ResourceEntity app;
    ResourceGroupEntity appGrp;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        release = Mockito.mock(ReleaseEntity.class);
        as = Mockito.mock(ResourceEntity.class);
        asGrp = Mockito.mock(ResourceGroupEntity.class);
        app = Mockito.mock(ResourceEntity.class);
        appGrp = Mockito.mock(ResourceGroupEntity.class);
        Mockito.when(as.getResourceGroup()).thenReturn(asGrp);
        Mockito.when(app.getResourceGroup()).thenReturn(appGrp);
        Mockito.when(dependencyResolverService
                  .getResourceEntityForRelease(Mockito.any(ResourceGroupEntity.class),
                            Mockito.any(ReleaseEntity.class))).thenReturn(app);

    }

    @Test
    public void testGetAppServersWithApplications() throws Exception {
        ResourceEntity as2 = Mockito.mock(ResourceEntity.class);
        Mockito.when(as2.getResourceGroup()).thenReturn(asGrp);

        ResourceEntity as3 = Mockito.mock(ResourceEntity.class);
        Mockito.when(as3.getResourceGroup()).thenReturn(asGrp);

        ResourceEntity as4 = Mockito.mock(ResourceEntity.class);
        Mockito.when(as4.getResourceGroup()).thenReturn(asGrp);

        UserSettingsEntity userSettings = Mockito.mock(UserSettingsEntity.class);
        Mockito.when(userSettingsService.getUserSettings(Mockito.anyString())).thenReturn(userSettings);
        List<ResourceEntity> aslist = Arrays.asList(as,as2,as3,as4);
        Mockito.when(applistScreenDomainService.getAppServerResourcesWithApplications(Mockito.isNull())).thenReturn(aslist);
        service.getAppServersWithApplications(null, release);
        Mockito.verify(service).filterAppServersByRelease(release, aslist);
    }

    @Test
    public void testFilterAppServersByRelease() throws Exception {
        //given
        List<ResourceEntity> applicationServers = Arrays.asList(as);
        Mockito.when(dependencyResolverService
                  .getResourceEntityForRelease(Mockito.any(ResourceGroupEntity.class),
                            Mockito.any(ReleaseEntity.class))).thenReturn(as);

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(service).filterApplicationsByRelease(Mockito.any(ReleaseEntity.class),
                  Mockito.any(ResourceEntity.class), Mockito.any(ResourceWithRelations.class));

        //when
        List<ResourceWithRelations> resources = service.filterAppServersByRelease(release, applicationServers);

        //then
        assertEquals(1, resources.size());
        assertEquals(0, resources.get(0).getRelatedResources().size());
        assertEquals(as, resources.get(0).getResource());
    }

    @Test
    public void testFilterApplicationsByRelease() throws Exception {
        ResourceWithRelations resourceWithRelations = doTestFilterApplicationsByRelease(5);

        //then
        assertEquals(1, resourceWithRelations.getRelatedResources().size());
        assertEquals(resourceWithRelations.getRelatedResources().get(0), app);
    }

    private ResourceWithRelations doTestFilterApplicationsByRelease(int appId)
              throws Exception {
        //given
        Mockito.when(as.getConsumedRelatedResourcesByResourceType(Mockito.any(
                  DefaultResourceTypeDefinition.class))).thenReturn(Arrays.asList(app));

        ResourceWithRelations resourceWithRelations = new ResourceWithRelations(as);
        Mockito.when(dependencyResolverService
                  .getResourceEntityForRelease(Mockito.any(ResourceGroupEntity.class),
                            Mockito.any(ReleaseEntity.class))).thenReturn(app);

        Mockito.when(appGrp.getId()).thenReturn(appId);

        //when
        service.filterApplicationsByRelease(release, as, resourceWithRelations);
        return resourceWithRelations;
    }
}
