package org.ethelred.cgi.standalone;

import org.eclipse.jetty.fcgi.server.proxy.FastCGIProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.Map;

import static org.eclipse.jetty.fcgi.server.proxy.FastCGIProxyServlet.*;

public class JettyServer {
    private final Server server;

    public JettyServer(int fcgiPort) {
        this.server = new Server();
        var connector = new ServerConnector(server, 1, 1);
        connector.setPort(8080);
//        connector.setHost("localhost");
        server.addConnector(connector);
        var servletContextHandler = new ServletContextHandler();
        var servletHolder = servletContextHandler.addServlet(FastCGIProxyServlet.class, "/*");
        servletHolder.setInitParameters(Map.of(
                "proxyTo", "http://localhost:" + fcgiPort,
                SCRIPT_ROOT_INIT_PARAM, "/virtual",
                SCRIPT_PATTERN_INIT_PARAM, "^()(.*)$"
        ));
        server.setHandler(servletContextHandler);
    }

    public void start() throws Exception {
        server.start();
    }

    public void shutdown() throws Exception {
        server.stop();
    }

    public boolean isRunning() {
        return server.isRunning();
    }
}
