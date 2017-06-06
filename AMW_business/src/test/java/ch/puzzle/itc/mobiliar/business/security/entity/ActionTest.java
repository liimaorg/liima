/*
 * AMW - Automated Middleware actionows you to manage the configurations of
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

package ch.puzzle.itc.mobiliar.business.security.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class ActionTest {

    private Action action;
    
    @Test
    public void allCanEverything() throws Exception {
        action = Action.ALL;
        assertTrue(action.canCreate());
        assertTrue(action.canRead());
        assertTrue(action.canUpdate());
        assertTrue(action.canDelete());
    }

    @Test
    public void readCanOnlyCreate() throws Exception {
        action = Action.CREATE;
        assertTrue(action.canCreate());
        assertFalse(action.canRead());
        assertFalse(action.canUpdate());
        assertFalse(action.canDelete());
    }

    @Test
    public void readCanOnlyRead() throws Exception {
        action = Action.READ;
        assertFalse(action.canCreate());
        assertTrue(action.canRead());
        assertFalse(action.canUpdate());
        assertFalse(action.canDelete());
    }

    @Test
    public void updateCanOnlyUpdate() throws Exception {
        action = Action.UPDATE;
        assertFalse(action.canCreate());
        assertFalse(action.canRead());
        assertTrue(action.canUpdate());
        assertFalse(action.canDelete());
    }

    @Test
    public void deleteCanOnlyDelete() throws Exception {
        action = Action.DELETE;
        assertFalse(action.canCreate());
        assertFalse(action.canRead());
        assertFalse(action.canUpdate());
        assertTrue(action.canDelete());
    }

}