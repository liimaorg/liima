package ch.mobi.itc.mobiliar.rest.exceptions;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.stream.Collectors;

public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        final String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .distinct() // since we use CDI and JAX-RS, there is potential that the validation kicks in twice.
                            // calling distinct on the list of messages makes sure, that each message is included only once.
                .collect(Collectors.joining("\n"));
                return Response.status(Response.Status.BAD_REQUEST).entity(new ExceptionDto(message)).build();
    }
}
