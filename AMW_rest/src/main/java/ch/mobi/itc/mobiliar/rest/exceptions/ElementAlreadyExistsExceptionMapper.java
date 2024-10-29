package ch.mobi.itc.mobiliar.rest.exceptions;

import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ElementAlreadyExistsExceptionMapper implements ExceptionMapper<ElementAlreadyExistsException> {
    @Override
    public Response toResponse(ElementAlreadyExistsException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ExceptionDto(exception)).build();
    }
}

