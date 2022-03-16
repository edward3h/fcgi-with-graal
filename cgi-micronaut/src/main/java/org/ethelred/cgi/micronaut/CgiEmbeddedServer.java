package org.ethelred.cgi.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.Options;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Singleton
public class CgiEmbeddedServer implements EmbeddedServer {
    private final ApplicationContext applicationContext;
    private final ApplicationConfiguration applicationConfiguration;
    private final HttpServerConfiguration httpServerConfiguration;
    private final CgiServer cgiServer;
    private final CgiHandler cgiHandler;

    public CgiEmbeddedServer(ApplicationContext applicationContext,
                             ApplicationConfiguration applicationConfiguration,
                             HttpServerConfiguration httpServerConfiguration,
                             CgiServer cgiServer,
                             CgiHandler cgiHandler) {
        this.applicationContext = applicationContext;
        this.applicationConfiguration = applicationConfiguration;
        this.httpServerConfiguration = httpServerConfiguration;
        this.cgiServer = cgiServer;
        this.cgiHandler = cgiHandler;
    }

    @Nonnull
    @Override
    public EmbeddedServer start() {
        try {
            var options = Options.of("server.port", httpServerConfiguration.getPort().orElse(8080));
            cgiServer.init(this::_onCompleted, options);
            if (!applicationContext.isRunning()) {
                applicationContext.start();
            }
            new Thread(() -> cgiServer.start(cgiHandler)).start();
            applicationContext.publishEvent(new ServerStartupEvent(this));
        }
        catch (Exception e)
        {
            throw new HttpServerException("Error starting server " + e.getMessage(), e);
        }
        return this;
    }

    private void _onCompleted() {
        try {
            if (applicationContext.isRunning()) {
                applicationContext.stop();
            }
            applicationContext.publishEvent(new ServerShutdownEvent(this));
        } catch (Exception e) {
            throw new HttpServerException("Error stopping server " + e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    public EmbeddedServer stop() {
        if (isRunning()) {
            try {
                cgiServer.shutdown();
            } catch (Exception e) {
                throw new HttpServerException("Error stopping server " + e.getMessage(), e);
            }
        }
        return this;
    }

    @Override
    public int getPort() {
        return httpServerConfiguration.getPort().orElse(8080);
    }

    @Override
    public String getHost() {
        return httpServerConfiguration.getHost().orElse("localhost");
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public URL getURL() {
        try {
            return getURI().toURL();
        } catch (MalformedURLException e) {
            throw new HttpServerException(e.getMessage(), e);
        }
    }

    @Override
    public URI getURI() {
        return URI.create(String.format("%s://%s:%d/", getScheme(), getHost(), getPort()));
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    @Override
    public boolean isRunning() {
        return cgiServer.isRunning();
    }
}
