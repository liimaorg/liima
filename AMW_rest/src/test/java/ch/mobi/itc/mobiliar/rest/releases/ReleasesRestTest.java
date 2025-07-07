package ch.mobi.itc.mobiliar.rest.releases;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.common.exception.ConcurrentModificationException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

public class ReleasesRestTest {

    @InjectMocks
    private ReleasesRest releasesRest;
    @Mock
    private ReleaseLocator releaseLocator;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

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

    @Test(expected = NotFoundException.class)
    public void updateRelease_shouldThrowNotFoundException_whenReleaseNotFound() throws Exception {
        Integer id = 42;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenThrow(new NotFoundException("not found"));

        releasesRest.updateRelease(id, request);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateRelease_shouldThrowConcurrentModificationException_whenUpdateThrows() throws Exception {
        Integer id = 42;
        ReleaseEntity request = new ReleaseEntity();
        when(releaseLocator.getReleaseById(id)).thenReturn(new ReleaseEntity());
        when(releaseLocator.update(any(ReleaseEntity.class)))
                .thenThrow(new ConcurrentModificationException("concurrent"));

        releasesRest.updateRelease(id, request);
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
}