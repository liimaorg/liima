package ch.mobi.itc.mobiliar.rest.releases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.common.exception.ConcurrentModificationException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
public class ReleasesRestTest {

    @InjectMocks
    private ReleasesRest releasesRest;
    @Mock
    private ReleaseLocator releaseLocator;

    @Test
    public void updateRelease_shouldReturnOK_whenUpdateSucceeds() throws Exception {
        Integer id = 42;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenReturn(new ReleaseEntity());
        when(releaseLocator.update(any(ReleaseEntity.class))).thenReturn(true);

        Response response = releasesRest.updateRelease(id, request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateRelease_shouldReturnBadRequest_whenUpdateFails() throws Exception {
        Integer id = 42;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenReturn(new ReleaseEntity());
        when(releaseLocator.update(any(ReleaseEntity.class))).thenReturn(false);

        Response response = releasesRest.updateRelease(id, request);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateRelease_shouldThrowNotFoundException_whenReleaseNotFound() throws Exception {
        Integer id = 42;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> releasesRest.updateRelease(id, request));
    }

    @Test
    public void updateRelease_shouldThrowConcurrentModificationException_whenUpdateThrows() throws Exception {
        Integer id = 42;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenReturn(new ReleaseEntity());
        when(releaseLocator.update(any(ReleaseEntity.class)))
                .thenThrow(new ConcurrentModificationException("concurrent"));

        assertThrows(ConcurrentModificationException.class, () -> releasesRest.updateRelease(id, request));
    }

    @Test
    public void updateRelease_shouldSetIdFromParam() throws Exception {
        Integer id = 123;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenReturn(new ReleaseEntity());
        when(releaseLocator.update(any(ReleaseEntity.class))).thenReturn(true);

        releasesRest.updateRelease(id, request);

        assertEquals(id, request.getId());
    }

    @Test
    public void addRelease_shouldReturnConflict_whenReleaseWithNameExists() {
        ReleaseEntity request = new ReleaseEntity();
        String name = "existingRelease";
        request.setName(name);
        // Simulate that a release with the same name exists
        when(releaseLocator.getReleaseByName(name)).thenReturn(new ReleaseEntity());

        Response response = releasesRest.addRelease(request);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ExceptionDto exceptionDto = (ExceptionDto) response.getEntity();

        assertEquals(409, response.getStatus());
        assertEquals("Release with name existingRelease already exists", exceptionDto.getMessage());
    }

    @Test
    public void addRelease_shouldReturnBadRequest_whenIdIsNotNull() {
        ReleaseEntity request = new ReleaseEntity();
        request.setId(1); // Id is not null

        Response response = releasesRest.addRelease(request);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ExceptionDto exceptionDto = (ExceptionDto) response.getEntity();
        assertEquals("Id must be null", exceptionDto.getMessage());
    }

    @Test
    public void addRelease_shouldReturnCreated_whenReleaseIsNewAndIdIsNull() {
        ReleaseEntity request = new ReleaseEntity();
        request.setName("newRelease");
        request.setId(null);
        when(releaseLocator.getReleaseByName("newRelease")).thenReturn(null);

        Response response = releasesRest.addRelease(request);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(request, response.getEntity());
        // Verify that create was called
        org.mockito.Mockito.verify(releaseLocator).create(request);
    }

}
