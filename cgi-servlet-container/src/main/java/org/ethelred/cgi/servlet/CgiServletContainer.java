package org.ethelred.cgi.servlet;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiRequest;
import org.ethelred.cgi.CgiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * TODO
 *
 * @author eharman
 * @since 2021-02-09
 */
public class CgiServletContainer implements CgiHandler
{
    private final Logger logger = LoggerFactory.getLogger(CgiServletContainer.class);
    private final CgiServer server;
    private HttpServlet servlet;
    private final ServletContext servletContext;

    public CgiServletContainer(CgiServer server)
    {
        this.server = server;
        server.init(CgiServer.Callback.ignore());
        servletContext = new CgiServletContext();
    }

    public void setServlet(HttpServlet servlet)
    {
        this.servlet = servlet;
    }

    public void start()
    {
        if (servlet == null) {
            throw new IllegalStateException("No servlet has been set up.");
        }
        try
        {
            servlet.init(new CgiServletConfig(this));
        }
        catch (ServletException e)
        {
            throw new RuntimeException(e);
        }
        server.start(this);
    }

    @Override
    public void handleRequest(CgiRequest cgiRequest)
    {
        var request = new RequestWrapper(cgiRequest, servletContext);
        var response = new ResponseWrapper(cgiRequest, request);
        try
        {
            servlet.service(request, response);
        }
        catch (ServletException | IOException e)
        {
            try
            {
                response.sendError(500, e.getMessage());
            }
            catch (IOException ioException)
            {
                // already failing, log and give up
                logger.error("Failed to send error response", e);
                server.shutdown();
            }
        }


    }

    private class CgiServletContext implements ServletContext
    {
        @Override
        public String getContextPath()
        {
            throw new UnsupportedOperationException("CgiServletContext.getContextPath");
        }

        @Override
        public ServletContext getContext(String uripath)
        {
            throw new UnsupportedOperationException("CgiServletContext.getContext");
        }

        @Override
        public int getMajorVersion()
        {
            throw new UnsupportedOperationException("CgiServletContext.getMajorVersion");
        }

        @Override
        public int getMinorVersion()
        {
            throw new UnsupportedOperationException("CgiServletContext.getMinorVersion");
        }

        @Override
        public int getEffectiveMajorVersion()
        {
            throw new UnsupportedOperationException("CgiServletContext.getEffectiveMajorVersion");
        }

        @Override
        public int getEffectiveMinorVersion()
        {
            throw new UnsupportedOperationException("CgiServletContext.getEffectiveMinorVersion");
        }

        @Override
        public String getMimeType(String file)
        {
            throw new UnsupportedOperationException("CgiServletContext.getMimeType");
        }

        @Override
        public Set<String> getResourcePaths(String path)
        {
            throw new UnsupportedOperationException("CgiServletContext.getResourcePaths");
        }

        @Override
        public URL getResource(String path) throws MalformedURLException
        {
            throw new UnsupportedOperationException("CgiServletContext.getResource");
        }

        @Override
        public InputStream getResourceAsStream(String path)
        {
            throw new UnsupportedOperationException("CgiServletContext.getResourceAsStream");
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path)
        {
            throw new UnsupportedOperationException("CgiServletContext.getRequestDispatcher");
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name)
        {
            throw new UnsupportedOperationException("CgiServletContext.getNamedDispatcher");
        }

        @Override
        public Servlet getServlet(String name) throws ServletException
        {
            throw new UnsupportedOperationException("CgiServletContext.getServlet");
        }

        @Override
        public Enumeration<Servlet> getServlets()
        {
            throw new UnsupportedOperationException("CgiServletContext.getServlets");
        }

        @Override
        public Enumeration<String> getServletNames()
        {
            throw new UnsupportedOperationException("CgiServletContext.getServletNames");
        }

        @Override
        public void log(String msg)
        {
            throw new UnsupportedOperationException("CgiServletContext.log");
        }

        @Override
        public void log(Exception exception, String msg)
        {
            throw new UnsupportedOperationException("CgiServletContext.log");
        }

        @Override
        public void log(String message, Throwable throwable)
        {
            throw new UnsupportedOperationException("CgiServletContext.log");
        }

        @Override
        public String getRealPath(String path)
        {
            throw new UnsupportedOperationException("CgiServletContext.getRealPath");
        }

        @Override
        public String getServerInfo()
        {
            throw new UnsupportedOperationException("CgiServletContext.getServerInfo");
        }

        @Override
        public String getInitParameter(String name)
        {
            throw new UnsupportedOperationException("CgiServletContext.getInitParameter");
        }

        @Override
        public Enumeration<String> getInitParameterNames()
        {
            throw new UnsupportedOperationException("CgiServletContext.getInitParameterNames");
        }

        @Override
        public boolean setInitParameter(String name, String value)
        {
            throw new UnsupportedOperationException("CgiServletContext.setInitParameter");
        }

        @Override
        public Object getAttribute(String name)
        {
            throw new UnsupportedOperationException("CgiServletContext.getAttribute");
        }

        @Override
        public Enumeration<String> getAttributeNames()
        {
            throw new UnsupportedOperationException("CgiServletContext.getAttributeNames");
        }

        @Override
        public void setAttribute(String name, Object object)
        {
            throw new UnsupportedOperationException("CgiServletContext.setAttribute");
        }

        @Override
        public void removeAttribute(String name)
        {
            throw new UnsupportedOperationException("CgiServletContext.removeAttribute");
        }

        @Override
        public String getServletContextName()
        {
            throw new UnsupportedOperationException("CgiServletContext.getServletContextName");
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className)
        {
            throw new UnsupportedOperationException("CgiServletContext.addServlet");
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet)
        {
            throw new UnsupportedOperationException("CgiServletContext.addServlet");
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass)
        {
            throw new UnsupportedOperationException("CgiServletContext.addServlet");
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
        {
            throw new UnsupportedOperationException("CgiServletContext.createServlet");
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName)
        {
            throw new UnsupportedOperationException("CgiServletContext.getServletRegistration");
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations()
        {
            throw new UnsupportedOperationException("CgiServletContext.getServletRegistrations");
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, String className)
        {
            throw new UnsupportedOperationException("CgiServletContext.addFilter");
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
        {
            throw new UnsupportedOperationException("CgiServletContext.addFilter");
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
        {
            throw new UnsupportedOperationException("CgiServletContext.addFilter");
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
        {
            throw new UnsupportedOperationException("CgiServletContext.createFilter");
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName)
        {
            throw new UnsupportedOperationException("CgiServletContext.getFilterRegistration");
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations()
        {
            throw new UnsupportedOperationException("CgiServletContext.getFilterRegistrations");
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig()
        {
            throw new UnsupportedOperationException("CgiServletContext.getSessionCookieConfig");
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
        {
            throw new UnsupportedOperationException("CgiServletContext.setSessionTrackingModes");
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
        {
            throw new UnsupportedOperationException("CgiServletContext.getDefaultSessionTrackingModes");
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
        {
            throw new UnsupportedOperationException("CgiServletContext.getEffectiveSessionTrackingModes");
        }

        @Override
        public void addListener(String className)
        {
            throw new UnsupportedOperationException("CgiServletContext.addListener");
        }

        @Override
        public <T extends EventListener> void addListener(T t)
        {
            throw new UnsupportedOperationException("CgiServletContext.addListener");
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass)
        {
            throw new UnsupportedOperationException("CgiServletContext.addListener");
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
        {
            throw new UnsupportedOperationException("CgiServletContext.createListener");
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor()
        {
            throw new UnsupportedOperationException("CgiServletContext.getJspConfigDescriptor");
        }

        @Override
        public ClassLoader getClassLoader()
        {
            throw new UnsupportedOperationException("CgiServletContext.getClassLoader");
        }

        @Override
        public void declareRoles(String... roleNames)
        {
            throw new UnsupportedOperationException("CgiServletContext.declareRoles");
        }

        @Override
        public String getVirtualServerName()
        {
            throw new UnsupportedOperationException("CgiServletContext.getVirtualServerName");
        }
    }
}
