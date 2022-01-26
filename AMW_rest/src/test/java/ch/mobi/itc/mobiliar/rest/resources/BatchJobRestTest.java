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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.mobi.itc.mobiliar.rest.dtos.BatchJobInventoryDTO;
import ch.mobi.itc.mobiliar.rest.dtos.BatchResourceDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public class BatchJobRestTest {

    @InjectMocks
    BatchJobRest rest;

    @Mock
    ServerView serverViewMock;

    @Mock
    private ResourceLocator resourceLocator;


    @Before
    public void configure() {
        MockitoAnnotations.openMocks(this);
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
    public void getBatchJobInventoryEmpty() {
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

}
