package org.ethelred.cgi.standalone;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StandaloneCgiServer implements CgiServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneCgiServer.class);

    private final static int FCGI_PORT = 29000;
    private Callback callback = Callback.ignore();
    private JettyServer jettyServer;
    private FcgiServer fcgiServer;

    private Future<?> fcgiServerShutdown;

    @Override
    public void init(Callback callback, Options options) {
        LOGGER.debug("init");
        var listenPort = options.get("server.port", 8080);
        Path files = options.get("static.files");
        this.callback = callback;
        this.jettyServer = new JettyServer(listenPort, FCGI_PORT, files);
        this.fcgiServer = new FcgiServer(FCGI_PORT);
    }


    @Override
    public void start(CgiHandler handler) {
        try {
            LOGGER.debug("start {}", handler);
            fcgiServer.start(handler);
            jettyServer.start();
        } catch (Exception e) {
            LOGGER.error("Error in start", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            LOGGER.debug("shutdown");
            jettyServer.shutdown();
            fcgiServerShutdown = fcgiServer.shutdown();
        } catch (Exception e) {
            LOGGER.error("Error in shutdown", e);
        }
    }

    @Override
    public boolean isSingleRequest() {
        return false;
    }

    @Override
    public void waitForCompletion(long timeout, TimeUnit unit) {
        LOGGER.debug("waitForCompletion");
        if (fcgiServerShutdown != null) {
            try {
                fcgiServerShutdown.get(timeout, unit);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error("Error in waitForCompletion", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return fcgiServerShutdown == null && jettyServer.isRunning();
    }
}
