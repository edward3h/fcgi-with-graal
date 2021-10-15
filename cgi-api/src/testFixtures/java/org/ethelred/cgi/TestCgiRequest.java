package org.ethelred.cgi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestCgiRequest implements CgiRequest {
    private final static Map<String, String> DEFAULT_ENV =
            Map.ofEntries(
                    Map.entry("FCGI_ROLE", "RESPONDER"),
                    Map.entry("UNIQUE_ID", "TODO"),
                    Map.entry("SCRIPT_URL", "/"),
                    Map.entry("SCRIPT_URI", "http://fcgi.example.com/"),
                    Map.entry("HTTP_HOST", "fcgi.example.com"),
                    Map.entry("HTTP_USER_AGENT", "TestCgiRequest user-agent"),
                    Map.entry("HTTP_ACCEPT", "*/*"),
                    Map.entry("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"),
                    Map.entry("SERVER_SIGNATURE", ""),
                    Map.entry("SERVER_SOFTWARE", "Apache"),
                    Map.entry("SERVER_NAME", "fcgi.example.com"),
                    Map.entry("SERVER_ADDR", "127.0.0.1"),
                    Map.entry("SERVER_PORT", "80"),
                    Map.entry("REMOTE_ADDR", "10.0.0.1"),
                    Map.entry("DOCUMENT_ROOT", "/TODO"),
                    Map.entry("REQUEST_SCHEME", "http"),
                    Map.entry("CONTEXT_PREFIX", ""),
                    Map.entry("CONTEXT_DOCUMENT_ROOT", "/TODO"),
                    Map.entry("SERVER_ADMIN", "webmaster@fcgi.example.com"),
                    Map.entry("SCRIPT_FILENAME", "/TODO/test_cgi_request.fcgi"),
                    Map.entry("REMOTE_PORT", "12345"),
                    Map.entry("GATEWAY_INTERFACE", "CGI/1.1"),
                    Map.entry("SERVER_PROTOCOL", "HTTP/1.1"),
                    Map.entry("REQUEST_METHOD", "GET"),
//                    Map.entry("QUERY_STRING", "hello=world"),
                    Map.entry("REQUEST_URI", "/TODO/test_cgi_request.fcgi"),
                    Map.entry("SCRIPT_NAME", "/TODO/test_cgi_request.fcgi"),
                    Map.entry("HTTP_CONNECTION", "close"),
                    Map.entry("CONTENT_LENGTH", "0")
            );

    private final Map<String, String> env = new HashMap<>(DEFAULT_ENV);
    private String body;
    private boolean readBody = false;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public TestCgiRequest() {

    }

    public TestCgiRequest(Map<String, String> env) {
        this(env, null);
    }

    public TestCgiRequest(Map<String, String> env, String body) {
        this.env.putAll(env);
        this.body = body;
    }

    // fluent API support...
    public TestCgiRequest param(ParamName name, String value) {
        this.env.put(name.getName(), value);
        return this;
    }

    public TestCgiRequest param(String name, String value) {
        this.env.put(name, value);
        return this;
    }

    public TestCgiRequest body(String body) {
        this.body = body;
        return this;
    }

    // groovy support...
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public Map<String, String> getEnv() {
        return Collections.unmodifiableMap(env);
    }

    @Override
    public synchronized InputStream getBody() {
        if (readBody) {
            throw new IllegalStateException("trying to get body that has already been read");
        }
        readBody = true;
        return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public OutputStream getOutput() {
        return output;
    }

    public String getOutputAsString() {
        return output.toString();
    }
}
