package ch.mobi.itc.mobiliar.rest.exceptions;


import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ValidationExceptionMapperTest {

    @Test
    public void shouldMapValidationException() {
        // given
        ValidationExceptionMapper mapper = new ValidationExceptionMapper();

        // when
        Response response = mapper.toResponse(new ValidationException("validation failed"));

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("validation failed", ((ExceptionDto) response.getEntity()).getMessage());
        assertNull(((ExceptionDto) response.getEntity()).getDetail());
    }
}