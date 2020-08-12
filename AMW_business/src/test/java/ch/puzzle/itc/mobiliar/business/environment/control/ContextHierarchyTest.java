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

package ch.puzzle.itc.mobiliar.business.environment.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContextHierarchyTest {

    @InjectMocks
    ContextHierarchy contextHierarchy;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetContextWithParentIds() throws Exception {
        ContextEntity env = mock(ContextEntity.class);
        ContextEntity dom = mock(ContextEntity.class);
        ContextEntity glob = mock(ContextEntity.class);
        when(env.getParent()).thenReturn(dom);
        when(env.getId()).thenReturn(2000);
        when(dom.getParent()).thenReturn(glob);
        when(dom.getId()).thenReturn(1002);
        when(glob.getParent()).thenReturn(null);
        when(glob.getId()).thenReturn(3000);

        List<Integer> result = contextHierarchy.getContextWithParentIds(env);

        assertEquals(3, result.size());
        assertEquals(Arrays.asList(3000, 1002, 2000), result);

    }
}