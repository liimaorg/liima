package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.CopyFromCandidateDTO;
import ch.mobi.itc.mobiliar.rest.dtos.CopyFromResourceRequestDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyFromResourcesRestTest {

    @InjectMocks
    CopyFromResourcesRest rest;

    @Mock
    GetResourceUseCase getResourceUseCaseMock;

    @Mock
    GetCandidatesToCopyFromResourceUseCase getCandidatesToCopyFromResourceUseCaseMock;

    @Mock
    CopyFromResourceUseCase copyFromResourceUseCaseMock;

    @Mock
    PermissionBoundary permissionBoundaryMock;

    @Test
    public void shouldThrowResourceNotFoundExceptionWhenGetCopyFromCandidatesForNonExistingResource() throws ResourceNotFoundException {
        // given
        when(getResourceUseCaseMock.getResourceById(any(ResourceIdCommand.class)))
                .thenThrow(new ResourceNotFoundException("Resource not found"));

        // when/then
        assertThrows(ResourceNotFoundException.class, () -> rest.getCopyFromCandidates(999));
    }

    @Test
    public void shouldThrowNotAuthorizedExceptionWhenNoCopyPermission() throws ResourceNotFoundException {
        // given
        ResourceEntity resource = mock(ResourceEntity.class);
        when(getResourceUseCaseMock.getResourceById(any(ResourceIdCommand.class))).thenReturn(resource);
        doThrow(new NotAuthorizedException("Permission denied"))
                .when(permissionBoundaryMock).assertPermission(any());

        // when/then
        assertThrows(NotAuthorizedException.class, () -> rest.getCopyFromCandidates(1));
    }

    @Test
    public void shouldReturnCandidatesWhenGetCopyFromCandidatesSucceeds() throws ResourceNotFoundException {
        // given
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);
        
        when(getResourceUseCaseMock.getResourceById(any(ResourceIdCommand.class))).thenReturn(resource);
        doNothing().when(permissionBoundaryMock).assertPermission(any());
        
        ResourceGroup group = mock(ResourceGroup.class);
        when(group.getId()).thenReturn(2);
        when(group.getName()).thenReturn("TestGroup");
        LinkedHashMap<String, Integer> releaseMap = new LinkedHashMap<>();
        releaseMap.put("v1.0", 100);
        when(group.getReleaseToResourceMap()).thenReturn(releaseMap);
        
        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(group);
        when(getCandidatesToCopyFromResourceUseCaseMock.getCandidates(resource)).thenReturn(groups);

        // when
        Response response = rest.getCopyFromCandidates(1);

        // then
        assertThat(response.getStatus(), is(OK.getStatusCode()));
        @SuppressWarnings("unchecked")
        List<CopyFromCandidateDTO> candidates = (List<CopyFromCandidateDTO>) response.getEntity();
        assertNotNull(candidates);
        assertEquals(1, candidates.size());
        assertEquals("TestGroup", candidates.get(0).getGroupName());
    }

    @Test
    public void shouldReturnOkOnSuccessfulCopyFromResource() throws AMWException {
        // given
        CopyFromResourceRequestDTO request = new CopyFromResourceRequestDTO(1, 2);
        doNothing().when(copyFromResourceUseCaseMock).copyFromResource(any(CopyFromResourceCommand.class));

        // when
        Response response = rest.copyFromResource(request);

        // then
        assertThat(response.getStatus(), is(OK.getStatusCode()));
        verify(copyFromResourceUseCaseMock).copyFromResource(any(CopyFromResourceCommand.class));
    }

    @Test
    public void shouldThrowExceptionWhenCopyFromResourceFails() throws AMWException {
        // given
        CopyFromResourceRequestDTO request = new CopyFromResourceRequestDTO(1, 2);
        doThrow(new IllegalStateException("Copy from resource failed"))
                .when(copyFromResourceUseCaseMock).copyFromResource(any(CopyFromResourceCommand.class));

        // when/then
        assertThrows(IllegalStateException.class, () -> rest.copyFromResource(request));
    }

    @Test
    public void shouldThrowAMWExceptionWhenCopyFromResourceThrowsException() throws AMWException {
        // given
        CopyFromResourceRequestDTO request = new CopyFromResourceRequestDTO(1, 2);
        doThrow(new AMWException("Permission Denied"))
                .when(copyFromResourceUseCaseMock).copyFromResource(any(CopyFromResourceCommand.class));

        // when/then
        AMWException exception = assertThrows(AMWException.class, () -> rest.copyFromResource(request));
        assertEquals("Permission Denied", exception.getMessage());
    }

}