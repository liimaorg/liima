package ch.mobi.itc.mobiliar.rest.exceptions;

import ch.puzzle.itc.mobiliar.common.exception.PropertyDescriptorNotDeletableException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PropertyDescriptorNotDeletableExceptionMapper implements ExceptionMapper<PropertyDescriptorNotDeletableException> {
    @Override
    public Response toResponse(PropertyDescriptorNotDeletableException exception) {
        String additionalInfo = "If you force the deletion, all those property values will be deleted as well";
        String errorMessage = String.format("%s. %s", exception.getMessage(), additionalInfo);
        return Response.status(Response.Status.CONFLICT)
                .entity(new ExceptionDto(errorMessage))
                .build();
    }
}
