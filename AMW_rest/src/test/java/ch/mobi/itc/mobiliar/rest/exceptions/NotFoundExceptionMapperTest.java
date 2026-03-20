package ch.mobi.itc.mobiliar.rest.exceptions;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotFoundExceptionMapperTest {

    @Test
    void toResponse_returnsNotFoundResponse() {
        NotFoundExceptionMapper mapper = new NotFoundExceptionMapper();
        NotFoundException exception = new NotFoundException("Resource not found");

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Resource not found", ((ExceptionDto) response.getEntity()).getMessage());
    }
}