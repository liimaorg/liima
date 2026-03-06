package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopyFromResourcesRestTest {

    @InjectMocks
    CopyFromResourcesRest rest;

    @Mock
    CopyResource copyResourceMock;

    @Mock
    ResourceLocator resourceLocatorMock;

    @Mock
    PermissionBoundary permissionBoundaryMock;

    @Test
    public void shouldReturnNotFoundWhenGetCopyFromCandidatesForNonExistingResource() {
        // given
        when(resourceLocatorMock.getResourceById(999)).thenReturn(null);

        // when
        Response response = rest.getCopyFromCandidates(999);

        // then
        assertThat(response.getStatus(), is(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnForbiddenWhenNoCopyPermission() {
        // given
        ResourceEntity resource = mock(ResourceEntity.class);
        when(resourceLocatorMock.getResourceById(1)).thenReturn(resource);
        when(permissionBoundaryMock.canCopyFromResource(resource)).thenReturn(false);

        // when
        Response response = rest.getCopyFromCandidates(1);

        // then
        assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void shouldReturnOkOnSuccessfulCopyFromResourceById() throws AMWException {
        // given
        CopyResourceResult copyResourceResult = mock(CopyResourceResult.class);
        when(copyResourceResult.isSuccess()).thenReturn(true);
        when(copyResourceMock.doCopyResource(1, 2)).thenReturn(copyResourceResult);

        // when
        Response response = rest.copyFromResourceById(1, 2);

        // then
        assertThat(response.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestWhenCopyFromResourceByIdFails() throws AMWException {
        // given
        CopyResourceResult copyResourceResult = mock(CopyResourceResult.class);
        when(copyResourceResult.isSuccess()).thenReturn(false);
        when(copyResourceMock.doCopyResource(1, 2)).thenReturn(copyResourceResult);

        // when
        Response response = rest.copyFromResourceById(1, 2);

        // then
        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestWhenCopyFromResourceByIdThrowsException() throws AMWException {
        // given
        when(copyResourceMock.doCopyResource(1, 2)).thenThrow(new AMWException("Permission Denied"));

        // when
        Response response = rest.copyFromResourceById(1, 2);

        // then
        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
        assertThat(((ExceptionDto) response.getEntity()).getMessage(), is("Permission Denied"));
    }

}