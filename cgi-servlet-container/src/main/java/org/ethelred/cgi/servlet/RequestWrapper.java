package org.ethelred.cgi.servlet;

import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.ethelred.cgi.CgiParam;
import org.ethelred.cgi.CgiRequest;
import org.ethelred.cgi.ParamName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Assume that this object is only used from a single request thread.
 *
 * @author edward3h
 * @since 2021-02-10
 */
public class RequestWrapper implements HttpServletRequest
{
    private final Logger LOGGER = LoggerFactory.getLogger(RequestWrapper.class);
    private final CgiRequest cgiRequest;

    private final Map<String, Cookie> nameToCookie;
    private final HeaderHelper headerHelper;
    private final ServletContext servletContext;
    private final Map<String, Object> attributes = new HashMap<>();
    private Charset charset = StandardCharsets.ISO_8859_1; // this is the default
    private boolean charsetIsDefault = true;
    private static final Pattern CHARSET_PARAMETER = Pattern.compile("\\bcharset=([^ ;]+)");

    private boolean inputOpened = false;

    private Map<String, String[]> parameters;

    public RequestWrapper(CgiRequest cgiRequest, ServletContext servletContext)
    {
        this.cgiRequest = cgiRequest;
        this.headerHelper = new HeaderHelper(cgiRequest.getEnv());
        this.servletContext = servletContext;
        this.nameToCookie = this.headerHelper.getOptionalHeader("Cookie")
                .map(this::parseCookies)
                .orElse(Collections.emptyMap());

        cgiRequest.getOptionalParam(CgiParam.CONTENT_TYPE)
                .ifPresent(h -> {
                    Matcher m = CHARSET_PARAMETER.matcher(h);
                    if (m.find())
                    {
                        try
                        {
                            charset = Charset.forName(m.group(1));
                            charsetIsDefault = false;
                        }
                        catch (Exception e)
                        {
                            LOGGER.error("Error reading charset from " + h, e);
                        }
                    }
                });
    }

    private Map<String, Cookie> parseCookies(String cookie)
    {
        var nettyCookie = ServerCookieDecoder.LAX.decode(cookie);
        return nettyCookie.stream()
                .map(this::nettyCookieToServletCookie)
                .collect(Collectors.toMap(Cookie::getName, Function.identity()));
//        return Collections.emptyMap();
    }

    private Cookie nettyCookieToServletCookie(io.netty.handler.codec.http.cookie.Cookie cookie)
    {
        var r = new Cookie(cookie.name(), cookie.value());
        r.setMaxAge(Math.toIntExact(cookie.maxAge()));
        r.setDomain(cookie.domain());
        r.setPath(cookie.path());
        r.setHttpOnly(cookie.isHttpOnly());
        r.setSecure(cookie.isSecure());
        return r;
    }

    /**
     * parameter parsing is lazy to allow for alternate ways of reading the request body
     */
    private void _parseParameters()
    {

    }

    @Override
    public String getAuthType()
    {
        return cgiRequest.getParam(CgiParam.AUTH_TYPE);
    }

    @Override
    public Cookie[] getCookies()
    {
        return nameToCookie.values().toArray(new Cookie[0]);
    }

    @Override
    public long getDateHeader(String name)
    {
        return headerHelper.getOptionalHeader(name)
                .map(h -> DateFormatter.parseHttpDate(h).getTime())
                .orElse(-1L);
//        return -1;
    }

    @Override
    public String getHeader(String name)
    {
        return headerHelper.getOptionalHeader(name).orElse(null);
    }

    // CGI rewrites headers appearing multiple times into a comma separated value. (Cookies are semi colon separated)
    @Override
    public Enumeration<String> getHeaders(String name)
    {
        return headerHelper.getHeaders(name);
    }

    // CGI converts headers into environment and then they can't be distinguished from other environment variables
    @Override
    public Enumeration<String> getHeaderNames()
    {
        return headerHelper.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name)
    {
        return headerHelper.getOptionalHeader(name)
                .map(Integer::parseInt)
                .orElse(-1);
    }

    @Override
    public String getMethod()
    {
        return cgiRequest.getParam(CgiParam.REQUEST_METHOD);
    }

    @Override
    public String getPathInfo()
    {
        return cgiRequest.getParam(CgiParam.PATH_INFO);
    }

    @Override
    public String getPathTranslated()
    {
        return cgiRequest.getParam(CgiParam.PATH_TRANSLATED);
    }

    @Override
    public String getContextPath()
    {
        return cgiRequest.getOptionalParam(CgiParam.SCRIPT_NAME).orElse("");
    }

    @Override
    public String getQueryString()
    {
        return cgiRequest.getParam(CgiParam.QUERY_STRING);
    }

    @Override
    public String getRemoteUser()
    {
        return cgiRequest.getParam(CgiParam.REMOTE_USER);
    }

    @Override
    public boolean isUserInRole(String role)
    {
        return false;
    }

    @Override
    public Principal getUserPrincipal()
    {
        String user = getRemoteUser();
        return user == null ? null : new ShittyPrincipal(user);
    }

    @Override
    public String getRequestedSessionId()
    {
        throw new UnsupportedOperationException("RequestWrapper.getRequestedSessionId");
    }

    @Override
    public String getRequestURI()
    {
        return cgiRequest.getRequiredParam(ParamName.of("SCRIPT_URL")); // determined by experiment on my setup, not from spec
    }

    @Override
    public StringBuffer getRequestURL()
    {
        return new StringBuffer(cgiRequest.getRequiredParam(ParamName.of("SCRIPT_URI")));  // determined by experiment on my setup, not from spec
    }

    @Override
    public String getServletPath()
    {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create)
    {
        throw new UnsupportedOperationException("RequestWrapper.getSession");
    }

    @Override
    public HttpSession getSession()
    {
        throw new UnsupportedOperationException("RequestWrapper.getSession");
    }

    @Override
    public String changeSessionId()
    {
        throw new UnsupportedOperationException("RequestWrapper.changeSessionId");
    }

    @Override
    public boolean isRequestedSessionIdValid()
    {
        throw new UnsupportedOperationException("RequestWrapper.isRequestedSessionIdValid");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie()
    {
        throw new UnsupportedOperationException("RequestWrapper.isRequestedSessionIdFromCookie");
    }

    @Override
    public boolean isRequestedSessionIdFromURL()
    {
        throw new UnsupportedOperationException("RequestWrapper.isRequestedSessionIdFromURL");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl()
    {
        throw new UnsupportedOperationException("RequestWrapper.isRequestedSessionIdFromUrl");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
    {
        throw new UnsupportedOperationException("RequestWrapper.authenticate");
    }

    @Override
    public void login(String username, String password) throws ServletException
    {
        throw new UnsupportedOperationException("RequestWrapper.login");
    }

    @Override
    public void logout() throws ServletException
    {
        throw new UnsupportedOperationException("RequestWrapper.logout");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException
    {
        throw new UnsupportedOperationException("RequestWrapper.getParts");
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException
    {
        throw new UnsupportedOperationException("RequestWrapper.getPart");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException
    {
        throw new UnsupportedOperationException("RequestWrapper.upgrade");
    }

    @Override
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding()
    {
        if (charsetIsDefault)
        {
            return null;
        }
        return charset.name();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException
    {
        charset = Charset.forName(env);
        charsetIsDefault = false;
    }

    @Override
    public int getContentLength()
    {
        var longValue = getContentLengthLong();
        if (((int) longValue) != longValue)
        {
            return -1;
        }
        return (int) longValue;
    }

    @Override
    public long getContentLengthLong()
    {
        return cgiRequest.getOptionalParam(CgiParam.CONTENT_LENGTH)
                .map(Long::parseLong)
                .orElse(-1L);
    }

    @Override
    public String getContentType()
    {
        return cgiRequest.getParam(CgiParam.CONTENT_TYPE);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if (inputOpened)
        {
            throw new IllegalStateException("Input already read");
        }
        inputOpened = true;
        return new ServletInputStreamWrapper(cgiRequest.getBody());
    }

    @Override
    public String getParameter(String name)
    {
        _parseParameters();
        String[] values = getParameterValues(name);
        return values == null ? null : values[0];
    }

    @Override
    public Enumeration<String> getParameterNames()
    {
        _parseParameters();
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name)
    {
        _parseParameters();
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        return Map.copyOf(parameters);
    }

    @Override
    public String getProtocol()
    {
        return cgiRequest.getRequiredParam(CgiParam.SERVER_PROTOCOL);
    }

    @Override
    public String getScheme()
    {
        return cgiRequest.getParam(ParamName.of("REQUEST_SCHEME"));
    }

    @Override
    public String getServerName()
    {

        return cgiRequest.getRequiredParam(CgiParam.SERVER_NAME);
    }

    @Override
    public int getServerPort()
    {
        return Integer.parseInt(cgiRequest.getRequiredParam(CgiParam.SERVER_PORT));
    }

    @Override
    public BufferedReader getReader() throws IOException
    {        if (inputOpened)
    {
        throw new IllegalStateException("Input already read");
    }
    inputOpened = true;
    return new BufferedReader(new InputStreamReader(Objects.requireNonNull(cgiRequest.getBody()), charset));
    }

    @Override
    public String getRemoteAddr()
    {
        return cgiRequest.getParam(ParamName.of("REMOTE_ADDR"));
    }

    @Override
    public String getRemoteHost()
    {
        return cgiRequest.getOptionalParam(ParamName.of("REMOTE_HOST"))
                .orElse(getRemoteAddr());
    }

    @Override
    public void setAttribute(String name, Object o)
    {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale()
    {
        throw new UnsupportedOperationException("RequestWrapper.getLocale");
    }

    @Override
    public Enumeration<Locale> getLocales()
    {
        throw new UnsupportedOperationException("RequestWrapper.getLocales");
    }

    @Override
    public boolean isSecure()
    {
        return "https".equalsIgnoreCase(getScheme());
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        throw new UnsupportedOperationException("RequestWrapper.getRequestDispatcher");
    }

    @Override
    public String getRealPath(String path)
    {
        throw new UnsupportedOperationException("RequestWrapper.getRealPath");
    }

    @Override
    public int getRemotePort()
    {
        return cgiRequest.getOptionalParam(ParamName.of("REMOTE_PORT"))
                .map(Integer::parseInt)
                .orElse(-1);
    }

    @Override
    public String getLocalName()
    {
        throw new UnsupportedOperationException("RequestWrapper.getLocalName");
    }

    @Override
    public String getLocalAddr()
    {
        throw new UnsupportedOperationException("RequestWrapper.getLocalAddr");
    }

    @Override
    public int getLocalPort()
    {
        throw new UnsupportedOperationException("RequestWrapper.getLocalPort");
    }

    @Override
    public ServletContext getServletContext()
    {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException
    {
        throw new UnsupportedOperationException("RequestWrapper.startAsync");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
    {
        throw new UnsupportedOperationException("RequestWrapper.startAsync");
    }

    @Override
    public boolean isAsyncStarted()
    {
        throw new UnsupportedOperationException("RequestWrapper.isAsyncStarted");
    }

    @Override
    public boolean isAsyncSupported()
    {
        throw new UnsupportedOperationException("RequestWrapper.isAsyncSupported");
    }

    @Override
    public AsyncContext getAsyncContext()
    {
        throw new UnsupportedOperationException("RequestWrapper.getAsyncContext");
    }

    @Override
    public DispatcherType getDispatcherType()
    {
        return DispatcherType.REQUEST;
    }
}
