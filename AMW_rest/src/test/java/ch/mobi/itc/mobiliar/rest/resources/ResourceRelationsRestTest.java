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
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RelationEditor;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceRelationsRestTest {

    @InjectMocks
    private ResourceRelationsRest rest;

    @Mock
    private RelationEditor relationEditorMock;

    @Mock
    private ResourceLocator resourceLocatorMock;

    @Before
    public void configure() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnBadRequestIfSlaveResourceGroupNameIsNull() {
        Response response = rest.addRelation("Master", "TestRelease", null, "consumed");
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnBadRequestIfResourceTypeIsNull() {
        Response response = rest.addRelation("Master", "TestRelease", "Slave", null);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnBadRequestIfResourceTypeIsInvalid() {
        Response response = rest.addRelation("Master", "TestRelease", "Slave", "Test");
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeRelationEditorWithRightArgumentsForConsumedRelations() throws AMWException, ValidationException {
        // given
        String resourceGroupName = "Master";
        String releaseName = "TestRelease";
        String relationType = "consumed";
        String slaveResourceGroupName = "Slave";

        when(relationEditorMock.isValidResourceRelationType(relationType)).thenReturn(true);

        // when
        Response response = rest.addRelation(resourceGroupName, releaseName, slaveResourceGroupName, relationType);

        // then
        verify(relationEditorMock, times(1)).addResourceRelationForSpecificRelease(resourceGroupName, slaveResourceGroupName, false, null, relationType, releaseName, ForeignableOwner.getSystemOwner());
        assertEquals(CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeResourceRelationBoundaryAndRelationEditorWithRightArgumentsForProvidedRelations() throws AMWException, ValidationException {
        // given
        String resourceGroupName = "Master";
        String releaseName = "TestRelease";
        String relationType =  "PROVIDED";
        String slaveResourceGroupName = "Slave";

        when(relationEditorMock.isValidResourceRelationType(relationType)).thenReturn(true);

        // when
        Response response = rest.addRelation(resourceGroupName, releaseName, slaveResourceGroupName, relationType);

        // then
        verify(relationEditorMock, times(1)).addResourceRelationForSpecificRelease(resourceGroupName, slaveResourceGroupName, true, null, relationType, releaseName, ForeignableOwner.getSystemOwner());
        assertEquals(CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeResourceRelationBoundaryAndRelationEditorWithRightArgumentsForProvidedRelationsAndFail() throws AMWException, ValidationException {
        // given
        String resourceGroupName = "Master";
        String releaseName = "TestRelease";
        String relationType =  "PROVIDED";
        String slaveResourceGroupName = "Slave";

        when(relationEditorMock.isValidResourceRelationType(relationType)).thenReturn(true);
        doThrow(new ValidationException("Resource is already provided by another ResourceGroup")).when(relationEditorMock)
                .addResourceRelationForSpecificRelease(resourceGroupName, slaveResourceGroupName, true, null, relationType, releaseName, ForeignableOwner.getSystemOwner());

        // when
        Response response = rest.addRelation(resourceGroupName, releaseName, slaveResourceGroupName, relationType);

        // then
        verify(relationEditorMock, times(1)).addResourceRelationForSpecificRelease(resourceGroupName, slaveResourceGroupName, true, null, relationType, releaseName, ForeignableOwner.getSystemOwner());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldNotAttemptToRemoveRelationWithInvalidResourceRelationType() throws ValidationException {
        // given
        String relationType = "InValid";

        when(relationEditorMock.isValidResourceRelationType(relationType)).thenReturn(false);

        // when
        Response response = rest.removeRelation("Master", "TestRelease", "Slave", relationType);

        // then
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnNotFoundOnRemoveRelationWithNonMatchingRelationName() throws ValidationException {
        // given
        String relationType = "CONSUMED";
        ResourceEntity resourceWithoutRelations = new ResourceEntity();

        when(relationEditorMock.isValidResourceRelationType(relationType)).thenReturn(true);
        when(resourceLocatorMock.getResourceByNameAndReleaseWithConsumedRelations(anyString(), anyString())).thenReturn(resourceWithoutRelations);

        // when
        Response response = rest.removeRelation("Master", "TestRelease", "Slave", "CONSUMED");

        // then
        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
    }

}
