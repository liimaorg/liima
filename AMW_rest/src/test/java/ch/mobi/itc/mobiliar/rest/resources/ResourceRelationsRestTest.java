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

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RelationEditor;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceRelationsRestTest {

    @InjectMocks
    ResourceRelationsRest rest;

    @Mock
    RelationEditor relationEditorMock;

    @Before
    public void configure() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnBadRequestIfSlaveResourceGroupNameIsNull() throws AMWException, ForeignableOwnerViolationException {
        // given
        rest.resourceGroupName = "Master";
        rest.releaseName = "TestRelease";

        // when
        Response response = rest.addRelation(null);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnBadRequestIfResourceTypeIsNull() throws AMWException, ForeignableOwnerViolationException {
        // given
        rest.resourceGroupName = "Master";
        rest.releaseName = "TestRelease";
        rest.resourceType = null;

        // when
        Response response = rest.addRelation("Slave");

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnBadRequestIfResourceTypeIsInvalid() throws AMWException, ForeignableOwnerViolationException {
        // given
        rest.resourceGroupName = "Master";
        rest.releaseName = "TestRelease";
        rest.resourceType = "Test";

        // when
        Response response = rest.addRelation("Slave");

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeRelationEditorWithRightArgumentsForConsumedRelations() throws AMWException, ForeignableOwnerViolationException, ValidationException {
        // given
        rest.resourceGroupName = "Master";
        rest.releaseName = "TestRelease";
        rest.resourceType = "consumed";
        String slaveResourceGroupName = "Slave";

        // when
        Response response = rest.addRelation(slaveResourceGroupName);

        // then
        verify(relationEditorMock, times(1)).addResourceRelationForSpecificRelease(rest.resourceGroupName, slaveResourceGroupName, false, null, rest.resourceType, rest.releaseName, ForeignableOwner.getSystemOwner());
        assertEquals(CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeResourceRelationBoundaryAndRelationEditorWithRightArgumentsForProvidedRelations() throws AMWException, ForeignableOwnerViolationException, ValidationException {
        // given
        rest.resourceGroupName = "Master";
        rest.releaseName = "TestRelease";
        rest.resourceType = "PROVIDED";
        String slaveResourceGroupName = "Slave";

        // when
        Response response = rest.addRelation(slaveResourceGroupName);

        // then
        verify(relationEditorMock, times(1)).addResourceRelationForSpecificRelease(rest.resourceGroupName, slaveResourceGroupName, true, null, rest.resourceType, rest.releaseName, ForeignableOwner.getSystemOwner());
        assertEquals(CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeResourceRelationBoundaryAndRelationEditorWithRightArgumentsForProvidedRelationsAndFail() throws AMWException, ForeignableOwnerViolationException, ValidationException {
        // given
        rest.resourceGroupName = "Master";
        rest.releaseName = "TestRelease";
        rest.resourceType = "PROVIDED";
        String slaveResourceGroupName = "Slave";

        doThrow(new ValidationException("Resource is already provided by another ResourceGroup")).when(relationEditorMock)
                .addResourceRelationForSpecificRelease(rest.resourceGroupName, slaveResourceGroupName, true, null, rest.resourceType, rest.releaseName, ForeignableOwner.getSystemOwner());

        // when
        Response response = rest.addRelation(slaveResourceGroupName);

        // then
        verify(relationEditorMock, times(1)).addResourceRelationForSpecificRelease(rest.resourceGroupName, slaveResourceGroupName, true, null, rest.resourceType, rest.releaseName, ForeignableOwner.getSystemOwner());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

}
