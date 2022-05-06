package org.ethelred.cgi.standalone;

import org.eclipse.jetty.fcgi.server.proxy.FastCGIProxyServlet;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.server.ResourceService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.eclipse.jetty.fcgi.server.proxy.FastCGIProxyServlet.*;

public class JettyServer {
    private final Server server;

    public JettyServer(int listenPort, int fcgiPort, Path resourceBase) {
        this.server = new Server();
        var connector = new ServerConnector(server, 1, 1);
        connector.setPort(listenPort);
//        connector.setHost("localhost");
        server.addConnector(connector);
        var container = new HandlerList();
        var resources = new ResourceHandler(new MyResourceService());
        if (resourceBase != null) {
            resources.setBaseResource(Resource.newResource(resourceBase));
            resources.setDirAllowed(false);
        }
        container.addHandler(resources);
        var servletContextHandler = new ServletContextHandler();
        var servletHolder = servletContextHandler.addServlet(FastCGIProxyServlet.class, "/*");
        servletHolder.setInitParameters(Map.of(
                "proxyTo", "http://localhost:" + fcgiPort,
                SCRIPT_ROOT_INIT_PARAM, "/virtual",
                SCRIPT_PATTERN_INIT_PARAM, "^()(.*)$"
        ));
        container.addHandler(servletContextHandler);
        server.setHandler(container);
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

    private static class MyResourceService extends ResourceService {
        @Override
        public void setContentFactory(HttpContent.ContentFactory contentFactory) {
            super.setContentFactory(new MyContentFactory(contentFactory));
        }

        @Override
        protected void notFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
            // no-op
        }
    }

    /**
     * overridden content factory allows Jetty to serve up existing static files while passing anything else
     * on to the FCGI handler.
     */
    private static class MyContentFactory implements HttpContent.ContentFactory {

        private final HttpContent.ContentFactory delegate;

        public MyContentFactory(HttpContent.ContentFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public HttpContent getContent(String path, int maxBuffer) throws IOException {
            var content = delegate.getContent(path, maxBuffer);
            if (content == null || content.getResource() == null || content.getResource().isDirectory()) {
                return null;
            }
            return content;
        }
    }
}
