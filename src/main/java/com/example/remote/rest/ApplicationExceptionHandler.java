package com.example.remote.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.example.remote.dto.ErrorResponse;

public class ApplicationExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException) exception;
            return Response.fromResponse(e.getResponse()).entity(new ErrorResponse(e)).build();
        } else {
            exception.printStackTrace();
            return Response.serverError().entity(new ErrorResponse(exception)).build();
        }
    }
}
