package org.ethelred.cgi.micronaut;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.servlet.http.DefaultServletExchange;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiRequest;

/**
 *
 * @author edward
 */
@Singleton
public class MicronautCgiHandler extends ServletHttpHandler<CgiRequest, CgiRequest> implements CgiHandler {
    private final CookieEncoder cookieEncoder = new CookieEncoder();
    private final CookieDecoder cookieDecoder;

    public MicronautCgiHandler(ApplicationContext applicationContext) {
        super(applicationContext);
        cookieDecoder = new CookieDecoder(applicationContext.getConversionService());
    }

    @Override
    protected ServletExchange<CgiRequest, CgiRequest> createExchange(CgiRequest request, CgiRequest response) {
        return new DefaultServletExchange<>(
                new RequestWrapper(request, getApplicationContext().getConversionService(), cookieDecoder, getMediaTypeCodecRegistry()),
                new ResponseWrapper(request, getApplicationContext(), cookieEncoder)
        );
    }

    @Override
    public void handleRequest(CgiRequest cgiRequest) {
        service(cgiRequest, cgiRequest);
    }

}
