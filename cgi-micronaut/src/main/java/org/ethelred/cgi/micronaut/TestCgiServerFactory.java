package org.ethelred.cgi.micronaut;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.standalone.StandaloneCgiServer;

import javax.inject.Singleton;

/**
 * This is the fallback case when the requirements on MicronautCgiServerFactory are not met.
 */
@Factory
public class TestCgiServerFactory {
    @Singleton
    public CgiServer cgiServer() {
        return new StandaloneCgiServer();
    }
}
