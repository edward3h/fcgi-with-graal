package org.ethelred.cgi.micronaut;

import io.micronaut.context.annotation.Factory;
import javax.inject.Singleton;

import io.micronaut.context.annotation.Requires;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.graal.CgiServerFactory;

/**
 *
 * @author edward
 */
@Factory
public class MicronautCgiServerFactory {
    @Singleton
    @Requires(notEnv = "test")
    public CgiServer cgiServer() {
        return new CgiServerFactory().get();
    }

}
