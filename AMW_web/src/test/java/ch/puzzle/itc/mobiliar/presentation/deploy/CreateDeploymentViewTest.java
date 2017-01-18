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

package ch.puzzle.itc.mobiliar.presentation.deploy;

import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateDeploymentViewTest {

    @Test
    public void shouldCreateNewDeploymentParameter() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        cdv.setSelectedKeyName("atest");
        cdv.createNewParameter();
        assertEquals(1, cdv.allSelectedDeploymentParameters.size());
    }

    @Test
    public void shouldNotCreateAnotherDeploymentParameterWithSameName() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        cdv.setSelectedKeyName("atest");
        cdv.createNewParameter();
        assertEquals(1, cdv.allSelectedDeploymentParameters.size());

        cdv.setSelectedKeyName("atest");
        cdv.createNewParameter();
        assertEquals(1, cdv.allSelectedDeploymentParameters.size());
    }

    @Test
    public void shouldNotCreateNewDeploymentParameterWithEmptyKey() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        cdv.setSelectedKeyName("");
        cdv.createNewParameter();
        assertEquals(0, cdv.allSelectedDeploymentParameters.size());
    }

    @Test
    public void shouldRemoveTheRightDeploymentParameter() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        cdv.setSelectedKeyName("atest");
        cdv.setDeployParameterValue("123");
        cdv.createNewParameter();
        assertEquals(1, cdv.allSelectedDeploymentParameters.size());

        cdv.setSelectedKeyName("btest");
        cdv.createNewParameter();
        assertEquals(2, cdv.allSelectedDeploymentParameters.size());

        cdv.removeParameter(cdv.allSelectedDeploymentParameters.get(0));
        assertEquals(1, cdv.allSelectedDeploymentParameters.size());
        assertEquals("btest", cdv.allSelectedDeploymentParameters.get(0).getKey());
    }

    @Test
    public void shouldReturnTheRightAvailableKeys() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        Key key = new Key("atest");
        cdv.getAllKeys().add(key);
        assertEquals(1, cdv.getAvailableKeys("").size());

        cdv.setSelectedKeyName("btest");
        cdv.createNewParameter();
        assertEquals(1, cdv.getAvailableKeys("").size());
        assertEquals("atest", cdv.getAvailableKeys("").get(0));
    }

    @Test
    public void shouldReturnNullIfNoReleaseIsSelected() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        assertNull(cdv.getSelectedRelease());
    }

    @Test
    public void shouldReturnFalseIfNoReleaseIsSelected() {

        CreateDeploymentView cdv = new CreateDeploymentView();
        assertFalse(cdv.isReleaseSelected());
    }

}