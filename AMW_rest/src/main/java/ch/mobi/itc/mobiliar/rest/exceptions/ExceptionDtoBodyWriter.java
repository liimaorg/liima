package ch.mobi.itc.mobiliar.rest.exceptions;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(TEXT_HTML)
public class ExceptionDtoBodyWriter implements MessageBodyWriter<ExceptionDto> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (ExceptionDto.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    @Override
    public long getSize(ExceptionDto exceptionDto, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        //used for contentLenght header, -1 = calculate it
        return -1;
    }

    @Override
    public void writeTo(ExceptionDto exceptionDto, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        PrintWriter pw = new PrintWriter(entityStream);

        httpHeaders.add(HttpHeaders.CONTENT_ENCODING, "utf-8");
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "text/html");

        pw.append("<!DOCTYPE html>\n<html>");
        pw.append("<head><title>Error</title></head><body>");
        pw.append(exceptionDto.getMessage());
        if (exceptionDto.getDetail() != null) {
            pw.append("<br/>").append(exceptionDto.getDetail());
        }
        pw.append("</body></html>");
        pw.flush();
    }
}
