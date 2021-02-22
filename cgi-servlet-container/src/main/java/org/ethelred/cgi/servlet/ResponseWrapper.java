package org.ethelred.cgi.servlet;

import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.ethelred.cgi.CgiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ResponseWrapper implements HttpServletResponse
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseWrapper.class);

    private final CgiRequest cgiRequest;
    private final RequestWrapper request;
    private final List<Cookie> cookies = new ArrayList<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    private Charset charset;
    private String contentTypeWithoutCharset;
    private ServletOutputStream output;
    private PrintWriter writer;

    /*
    response states - initial -> wrote headers -> done
     */
    private enum ResponseState
    {
        INITIAL, WROTE_HEADERS
            {
                @Override
                void checkModifyHeaders()
                {
                    LOGGER.error("already wrote headers", new Exception("Trace"));
                    //throw new IllegalStateException("Already wrote headers");
                }
            }, COMMITTED
            {
                @Override
                void checkModifyHeaders()
                {
                    LOGGER.error("already wrote headers", new Exception("Trace"));
                    //throw new IllegalStateException("Already wrote headers");
                }

                @Override
                void checkDoOutput()
                {
                    LOGGER.error("already wrote output", new Exception("Trace"));
                    //throw new IllegalStateException("Already wrote output");
                }
            };

        void checkModifyHeaders()
        {
        }

        void checkDoOutput()
        {
        }
    }

    private ResponseState state = ResponseState.INITIAL;

    public ResponseWrapper(CgiRequest cgiRequest, RequestWrapper request)
    {
        this.cgiRequest = cgiRequest;
        this.request = request;
        headers.computeIfAbsent("X-Powered-By", k -> new ArrayList<>()).add("cgi-servlet-container");
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        state.checkModifyHeaders();
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String name)
    {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url)
    {
        // no URL rewriting here
        return url;
    }

    @Override
    public String encodeRedirectURL(String url)
    {
        return url;
    }

    @Override
    public String encodeUrl(String url)
    {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url)
    {
        return url;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        state.checkModifyHeaders();
        setIntHeader("Status", sc);
        _writeHeaders();
        Utils.renderErrorPage(getWriter(), sc, msg);
    }

    private static final String CONTENT_TYPE = "Content-Type";
    // https://tools.ietf.org/html/rfc3875#section-6
    private static final Set<String> CGI_REQUIRED_HEADERS = Set.of(CONTENT_TYPE, "Location", "Status");

    private void _writeHeaders() throws IOException
    {
        state.checkModifyHeaders();
        headers.put(CONTENT_TYPE, List.of(getContentType()));
        if (headers.keySet().stream().noneMatch(CGI_REQUIRED_HEADERS::contains))
        {
            throw new IllegalStateException("Must specify at least one of " + CGI_REQUIRED_HEADERS + " headers");
        }
        state = ResponseState.WROTE_HEADERS;
        var cookieStrings = ServerCookieEncoder.LAX.encode(
                cookies.stream()
                        .map(this::servletCookieToNettyCookie)
                        .collect(Collectors.toList())
        );
//        var cookieStrings = new ArrayList<String>();
        headers.put("Set-Cookie", List.of(String.join("; ", cookieStrings)));
        var w = new PrintWriter(cgiRequest.getOutput());
        headers.forEach((name, values) -> {
                    w.print(name);
                    w.print(": ");
                    w.println(String.join(", ", values));
                }
        );
        w.println();
    }

    private io.netty.handler.codec.http.cookie.Cookie servletCookieToNettyCookie(Cookie cookie)
    {
        var netty = new DefaultCookie(cookie.getName(), cookie.getValue());
        netty.setMaxAge(cookie.getMaxAge());
        netty.setDomain(cookie.getDomain());
        netty.setHttpOnly(cookie.isHttpOnly());
        netty.setPath(cookie.getPath());
        netty.setSecure(cookie.getSecure());
        return netty;
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        sendError(sc, null);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        state.checkModifyHeaders();
        setHeader("Location", location);
        _writeHeaders();
        // body is empty
        getWriter();
    }

    @Override
    public void setDateHeader(String name, long date)
    {
        var formattedDate = DateFormatter.format(new Date(date));
        setHeader(name, formattedDate);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        var formattedDate = DateFormatter.format(new Date(date));
        addHeader(name, formattedDate);
    }

    @Override
    public void setHeader(String name, String value)
    {
        state.checkModifyHeaders();
        var replaceList = new ArrayList<String>();
        replaceList.add(value);
        headers.put(name, replaceList);
    }

    @Override
    public void addHeader(String name, String value)
    {
        state.checkModifyHeaders();
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc)
    {
        setIntHeader("Status", sc);
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        setStatus(sc);
    }

    @Override
    public int getStatus()
    {
        var values = headers.get("Status");
        if (values == null || values.isEmpty())
        {
            return 200;
        }
        return Integer.parseInt(values.get(0));
    }

    @Override
    public String getHeader(String name)
    {
        var values = headers.get(name);
        if (values == null || values.isEmpty())
        {
            return null;
        }
        return values.get(0);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        var values = headers.get(name);
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return List.copyOf(values);
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return Set.copyOf(headers.keySet());
    }

    @Override
    public String getCharacterEncoding()
    {
        return Objects.requireNonNullElse(charset, StandardCharsets.ISO_8859_1).name();
    }

    @Override
    public String getContentType()
    {
        if (contentTypeWithoutCharset == null)
        {
            return null;
        }
        return contentTypeWithoutCharset + "; charset=" + getCharacterEncoding();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (output == null)
        {
            _writeHeaders();
            state.checkDoOutput();
            state = ResponseState.COMMITTED;
            output = new ServletOutputStreamWrapper(cgiRequest.getOutput());
        }
        return output;
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (writer == null)
        {
            if (charset == null)
            {
                charset = StandardCharsets.ISO_8859_1;
            }
            _writeHeaders();
            state.checkDoOutput();
            state = ResponseState.COMMITTED;
            writer = new PrintWriter(cgiRequest.getOutput(), false, charset);
        }
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset)
    {
        state.checkModifyHeaders();
        this.charset = Charset.forName(charset);
    }

    @Override
    public void setContentLength(int len)
    {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentLengthLong(long len)
    {
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void setContentType(String type)
    {
        state.checkModifyHeaders();
        var parts = type.split(";\\s*");
        if (parts.length > 1)
        {
            StringBuilder reassemble = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++)
            {
                var parameter = parts[i].split("=", 2);
                if (parameter.length > 1 && "charset".equalsIgnoreCase(parameter[0]))
                {
                    this.charset = Charset.forName(parameter[1]);
                } else
                {
                    reassemble.append("; ").append(parts[i]);
                }
            }
            contentTypeWithoutCharset = reassemble.toString();
        } else
        {
            contentTypeWithoutCharset = type;
        }
    }

    @Override
    public void setBufferSize(int size)
    {
        throw new UnsupportedOperationException("ResponseWrapper.setBufferSize");
    }

    @Override
    public int getBufferSize()
    {
        throw new UnsupportedOperationException("ResponseWrapper.getBufferSize");
    }

    @Override
    public void flushBuffer() throws IOException
    {
        throw new UnsupportedOperationException("ResponseWrapper.flushBuffer");
    }

    @Override
    public void resetBuffer()
    {
        throw new UnsupportedOperationException("ResponseWrapper.resetBuffer");
    }

    @Override
    public boolean isCommitted()
    {
        return state == ResponseState.COMMITTED;
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException("ResponseWrapper.reset");
    }

    @Override
    public void setLocale(Locale loc)
    {
        throw new UnsupportedOperationException("ResponseWrapper.setLocale");
    }

    @Override
    public Locale getLocale()
    {
        throw new UnsupportedOperationException("ResponseWrapper.getLocale");
    }
}
