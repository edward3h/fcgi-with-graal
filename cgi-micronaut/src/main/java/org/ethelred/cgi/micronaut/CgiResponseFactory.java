package org.ethelred.cgi.micronaut;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.server.exceptions.InternalServerException;

public class CgiResponseFactory implements HttpResponseFactory {
    @Override
    public <T> MutableHttpResponse<T> ok(T body) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof RequestWrapper) {
            final MutableHttpResponse response = ((RequestWrapper) req).getResponse();
            return response.status(HttpStatus.OK).body(body);
        } else {
                throw new InternalServerException("Current request not found in context.");
        }
    }

    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof RequestWrapper) {
            final MutableHttpResponse response = ((RequestWrapper) req).getResponse();
            return response.status(status, reason);
        } else {
            throw new InternalServerException("Current request not found in context.");
        }
    }

    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof RequestWrapper) {
            final MutableHttpResponse response = ((RequestWrapper) req).getResponse();
            return response.status(status).body(body);
        } else {
            throw new InternalServerException("Current request not found in context.");
        }
    }
}
