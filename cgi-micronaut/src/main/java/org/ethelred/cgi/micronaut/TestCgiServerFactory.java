package org.ethelred.cgi.micronaut;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.standalone.StandaloneCgiServer;

import javax.inject.Singleton;

@Factory
public class TestCgiServerFactory {
    @Singleton
    @Requires(env = "test")
    public CgiServer cgiServer() {
        return new StandaloneCgiServer();
    }
}
