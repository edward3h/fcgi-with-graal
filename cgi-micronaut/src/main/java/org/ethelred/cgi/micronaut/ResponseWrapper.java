package org.ethelred.cgi.micronaut;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.ethelred.cgi.CgiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.servlet.http.ServletHttpResponse;

public class ResponseWrapper<B> implements ServletHttpResponse<CgiRequest, B> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseWrapper.class);

    private static final String CONTENT_TYPE = "Content-Type";
    // https://tools.ietf.org/html/rfc3875#section-6
    private static final Set<String> CGI_REQUIRED_HEADERS = Set.of(CONTENT_TYPE, "Location", "Status");

    private final CgiRequest cgiRequest;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final Map<String, Cookie> cookies = new HashMap<>();
    private final MutableHttpHeaders headers;
    private final Function<Cookie, String> cookieEncoder;

    private ResponseState state = ResponseState.INITIAL;
    private OutputStream output;
    private BufferedWriter writer;

    private Object body;
    private HttpStatus status = HttpStatus.OK;

    private final CountDownLatch commitLatch = new CountDownLatch(1);

    public void awaitCommit() {
        try {
            commitLatch.await();
        } catch (InterruptedException e) {
            // ignore
        }
    }

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

    public ResponseWrapper(CgiRequest cgiRequest, ApplicationContext applicationContext, Function<Cookie, String> cookieEncoder) {
        this.cgiRequest = cgiRequest;
        this.headers = new HeaderStateWrapper(new SimpleHttpHeaders(applicationContext.getConversionService()));
        this.cookieEncoder = cookieEncoder;
    }

    @Override
    public CgiRequest getNativeResponse() {
        return cgiRequest;
    }

    private void writeHeaders() {
        state.checkModifyHeaders();
        if (headers.names().stream().noneMatch(CGI_REQUIRED_HEADERS::contains) && status == HttpStatus.OK) {
            throw new IllegalStateException("Must specify at least one of " + CGI_REQUIRED_HEADERS);
        }
        state = ResponseState.WROTE_HEADERS;
        LOGGER.info("Writing headers", new Exception("Trace"));
        try (var w = new PrintWriter(cgiRequest.getOutput())) {
            w.print("Status: ");
            w.println(status.getCode());
            headers.forEach((name, values) -> {
                        w.print(name);
                        w.print(": ");
                        w.println(String.join(", ", values));
                    }
            );
            if (!cookies.isEmpty()) {
                cookies.values().stream()
                        .map(cookieEncoder)
                        .forEach(c -> {
                            w.print("Set-Cookie: ");
                            w.println(c);
                        });
            }
            w.println();
            commitLatch.countDown();
        }
    }

    private void initOutput() {
        if (output == null) {
            state.checkDoOutput();
            writeHeaders();
            state = ResponseState.COMMITTED;
            output = cgiRequest.getOutput();
            writer = new BufferedWriter(new OutputStreamWriter(output));
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        initOutput();
        return output;
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        initOutput();
        return writer;
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        LOGGER.info("Set cookie {}", cookie);
        state.checkModifyHeaders();
        cookies.put(cookie.getName(), cookie);
        return this;
    }

    @Override
    public <T> MutableHttpResponse<T> body(T body) {
        this.body = body;
        return (MutableHttpResponse<T>) this;
    }

    @Override
    public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
        LOGGER.info("set status {} [{}]", status, message, new Exception("Trace"));
        state.checkModifyHeaders();
        this.status = status;
        if (message != null) {
            sendMessage(message);
        }
        return this;
    }

    private void sendMessage(CharSequence message) {
        try (var w = getWriter()) {
            w.write(message.toString());
        } catch (IOException e) {
            // not a lot we can do here
            LOGGER.error("Failed to write message", e);
        }
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Nonnull
    @Override
    public MutableHttpHeaders getHeaders() {
        return headers;
    }

    @Nonnull
    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    @Override
    public Optional<B> getBody() {
        return (Optional<B>) Optional.ofNullable(body);
    }

    /**
     * ensure the output is written if it hasn't already been
     */
    public void commit() {
        switch (state) {
            case COMMITTED -> LOGGER.info("Already committed.");
            case WROTE_HEADERS -> {
                LOGGER.info("Already wrote headers.");
                _commitOutput();
            }
            case INITIAL -> {
                LOGGER.info("No output yet");
                try (var w = getWriter()) {
                    w.newLine();
                } catch (IOException e) {
                    LOGGER.error("Error in commit", e);
                }
            }
        }
    }

    private void _commitOutput() {
        try {
            output.write('\n');
        } catch (IOException e) {
            LOGGER.error("Error in _commitOutput", e);
        }
    }

    private class HeaderStateWrapper implements MutableHttpHeaders {
        private final MutableHttpHeaders delegate;

        public HeaderStateWrapper(MutableHttpHeaders delegate) {
            this.delegate = delegate;
        }

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            LOGGER.info("Add Header {}={}", header, value);
            state.checkModifyHeaders();
            return delegate.add(header, value);
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            LOGGER.warn("Remove Header {}", header);
            state.checkModifyHeaders();
            return delegate.remove(header);
        }

        @Override
        public List<String> getAll(CharSequence name) {
            return delegate.getAll(name);
        }

        @Override
        @Nullable
        public String get(CharSequence name) {
            return delegate.get(name);
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            return delegate.get(name, conversionContext);
        }

        @Override
        public Set<String> names() {
            return delegate.names();
        }

        @Override
        public Collection<List<String>> values() {
            return delegate.values();
        }
    }
}
