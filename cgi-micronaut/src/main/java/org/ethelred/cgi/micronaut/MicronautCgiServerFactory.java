package org.ethelred.cgi.micronaut;

import io.micronaut.context.annotation.*;

import javax.inject.Singleton;

import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.graal.CgiServerFactory;

/**
 *
 * @author edward
 */
@Factory
public class MicronautCgiServerFactory {
    @Primary
    @Singleton
    @Requires(notEnv = "test")
    @Requires(property = "cgi.server", notEquals = "standalone")
    public CgiServer cgiServer() {
        return new CgiServerFactory().get();
    }

}
