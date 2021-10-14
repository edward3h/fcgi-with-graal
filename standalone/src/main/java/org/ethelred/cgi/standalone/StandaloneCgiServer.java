package org.ethelred.cgi.standalone;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StandaloneCgiServer implements CgiServer {
    private final static int FCGI_PORT = 29000;
    private Callback callback = Callback.ignore();
    private JettyServer jettyServer;
    private FcgiServer fcgiServer;

    private Future<?> fcgiServerShutdown;

    @Override
    public void init(Callback callback) {
        this.callback = callback;
        this.jettyServer = new JettyServer(FCGI_PORT);
        this.fcgiServer = new FcgiServer(FCGI_PORT);
    }

    @Override
    public void start(CgiHandler handler) {
        try {
            fcgiServer.start(handler);
            jettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            jettyServer.shutdown();
            fcgiServerShutdown = fcgiServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSingleRequest() {
        return false;
    }

    @Override
    public void waitForCompletion(long timeout, TimeUnit unit) {
        if (fcgiServerShutdown != null) {
            try {
                fcgiServerShutdown.get(timeout, unit);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return fcgiServerShutdown == null && jettyServer.isRunning();
    }
}
