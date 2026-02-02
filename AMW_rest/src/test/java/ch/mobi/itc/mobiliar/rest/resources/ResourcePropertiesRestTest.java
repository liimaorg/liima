package ch.mobi.itc.mobiliar.rest.resources;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class ResourcePropertiesRestTest {

    @InjectMocks
    ResourcePropertiesRest rest;

    @Mock
    ResourceLocator resourceLocator;

    @Test
    public void shouldReturnNotFoundStatusIfNoPropertiesHaveBeenFound() throws ValidationException {
        // given
        int notFound = Response.Status.NOT_FOUND.getStatusCode();

        // when
        Response response = rest.getResourceProperties("Test", "Test", "test");

        // then
        assertThat(response.getStatus(), is(notFound));
    }

}
