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

package ch.puzzle.itc.mobiliar.presentation.appserver;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.puzzle.itc.mobiliar.business.appserverrelation.boundary.AppServerRelation;
import ch.puzzle.itc.mobiliar.business.appserverrelation.control.AppServerRelationPath;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

@RunWith(MockitoJUnitRunner.class)
public class AppServerRelationsViewTest {

	@Mock
	private AppServerRelation serviceMock;

	@Mock
	private ReleaseMgmtService releaseServiceMock;

    @Mock
    private ResourceEntity appServerMock;

    @InjectMocks
	private AppServerRelationsView view;


    @Test
    public void setAppServerIdWhenServerIsNullShouldResetServerParameter(){
        // given
        Integer serverId = null;

        // when
        view.setAppServerId(serverId);

        // then
        assertTrue("Path should be reset when no serverId is available", view.getPaths().isEmpty());
        assertTrue("Releases should be reset when no serverId is available", view.getReleases().isEmpty());
        assertNull("Server should be set to null when no serverId is available", view.getAppServer());

    }

    @Test
    public void setAppServerIdWhenServerIsNullShouldSetAppServerIdNull(){
        // given
        Integer serverId = null;

        // when
        view.setAppServerId(serverId);

        // then
        assertNull("AppServerId should be set to null when no serverId is available", view.getAppServerId());

    }

    @Test
    public void setAppServerIdWhenServerIsPreviouslySetNullShouldSetAppServerId(){
        // given
        Integer serverId = 10;
        assertNull(view.getAppServerId());

        when(serviceMock.getAppServer(serverId)).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));

        // when
        view.setAppServerId(serverId);

        // then
        assertEquals("AppServerId should be set", serverId, view.getAppServerId());

    }

    @Test
    public void setAppServerIdWhenServerIsPreviouslySetShouldSetAppServerId(){
        // given
        Integer previousServerId = 10;
        Integer newServerId = 20;

        when(serviceMock.getAppServer(anyInt())).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));

        view.setAppServerId(previousServerId);
        assertEquals("AppServerId should be set", previousServerId, view.getAppServerId());

        // when
        view.setAppServerId(newServerId);

        // then
        assertEquals("AppServerId should be set", newServerId, view.getAppServerId());

    }

    @Test
    public void setAppServerIdWhenServerIsPreviouslySetShouldSetSameAppServerId(){
        // given
        Integer previousServerId = 10;
        Integer newServerId = 10;
        assertEquals("ServerId's should be the same", previousServerId, newServerId);

        when(serviceMock.getAppServer(anyInt())).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));

        view.setAppServerId(previousServerId);
        assertEquals("AppServerId should be set", previousServerId, view.getAppServerId());

        // when
        view.setAppServerId(newServerId);

        // then
        assertEquals("AppServerId should be set", newServerId, view.getAppServerId());

    }

    private ReleaseEntity createRelease(Integer id) {
       ReleaseEntity release = new ReleaseEntity();
        release.setId(id);
        return release;
    }


    @Test
    public void setAppServerIdWhenServerIsPreviouslySetShouldNotLoadDataWhenSameId(){
        // given
        Integer serverId = 10;

        when(serviceMock.getAppServer(serverId)).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));

        view.setAppServerId(serverId);
        assertEquals("AppServerId should be set", serverId, view.getAppServerId());

        // when
        view.setAppServerId(serverId);

        // then load data only the first time. Do not load data when id is the same
       verify(serviceMock, times(1)).getAppServer(serverId);
        verify(appServerMock, times(1)).getRelease();

    }

    @Test
    public void setAppServerIdShouldLoadAppserverWhenDifferentId(){
        // given
        Integer serverId = 10;
        assertNull(view.getAppServerId());

        when(serviceMock.getAppServer(serverId)).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));

        // when
        view.setAppServerId(serverId);

        // then
        assertEquals("AppServer should be set", appServerMock, view.getAppServer());

    }

    @Test
    public void setAppServerIdShouldLoadReleasesWhenDifferentId(){
        // given
        Integer serverId = 10;
        assertNull(view.getAppServerId());
        assertNull(view.getReleases());

        List<ReleaseEntity> releases = new ArrayList<>();
        releases.add(createRelease(1));

        when(serviceMock.getAppServer(serverId)).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));
        when(releaseServiceMock.getDeployableReleasesForResourceGroup(any(ResourceGroupEntity.class))).thenReturn(releases);

        // when
        view.setAppServerId(serverId);

        // then
        assertEquals("Releases should be set", releases.size(), view.getReleases().size());

    }

    @Test
    public void setAppServerIdShouldLoadAppServerRelationsFromLiveDBWhenDifferentId(){
        // given
        Integer serverId = 10;
        assertNull(view.getAppServerId());
        assertNull(view.getPaths());

        List<AppServerRelationPath> paths = new ArrayList<>();
        paths.add(mock(AppServerRelationPath.class));

        when(serviceMock.getAppServer(serverId)).thenReturn(appServerMock);
        when(appServerMock.getRelease()).thenReturn(createRelease(1));
        when(serviceMock.getAppServerRelationsFromLiveDB(anyInt(), any(ReleaseEntity.class))).thenReturn(paths);

        // when
        view.setAppServerId(serverId);

        // then
        assertEquals("Releases should be set", paths.size(), view.getPaths().size());

    }


}