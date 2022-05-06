package org.ethelred.cgi.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValuesMap;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.ServletHttpRequest;
import org.ethelred.cgi.CgiParam;
import org.ethelred.cgi.CgiRequest;
import org.ethelred.cgi.ParamName;
import org.ethelred.cgi.util.QueryStringParser;
import org.ethelred.util.function.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.ethelred.util.function.Lazy.lazy;

public class RequestWrapper implements ServletHttpRequest<CgiRequest, Object> {
    private static final QueryStringParser queryStringParser = new QueryStringParser();
    private static final Cookies EMPTY_COOKIES = new EmptyCookies();
    private final CgiRequest cgiRequest;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final AtomicBoolean readBody = new AtomicBoolean(false);
    private final HttpHeaders headers;
    private final Lazy<HttpMethod> method;
    private final Lazy<HttpParameters> parameters;
    private final Lazy<URI> uri;
    private final Lazy<Cookies> cookies;
    private final ConversionService conversionService;
    private final MediaTypeCodecRegistry codecRegistry;
    private Object body;

    public RequestWrapper(CgiRequest cgiRequest,
                          ConversionService conversionService,
                          Function<String, Cookies> cookieDecoder,
                          MediaTypeCodecRegistry codecRegistry) {
        this.cgiRequest = cgiRequest;
        this.conversionService = conversionService;
        this.codecRegistry = codecRegistry;
        this.headers = new CgiHeaders(cgiRequest, conversionService);
        this.method = lazy(() -> HttpMethod.parse(cgiRequest.getRequiredParam(CgiParam.REQUEST_METHOD)));
        this.parameters = lazy(CgiParameters::new);
        this.uri = lazy(() -> URI.create(cgiRequest.getRequiredParam(CgiParam.REQUEST_URI)));
        this.cookies = lazy(() -> cgiRequest.getOptionalParam(ParamName.httpHeader("Cookie")).map(cookieDecoder).orElse(EMPTY_COOKIES));
    }

    @CheckForNull
    @Override
    public InputStream getInputStream() throws IOException {
        if (readBody.compareAndSet(false, true)) {
            return cgiRequest.getBody();
        }
        throw new IllegalStateException("Body was already opened");
    }

    @CheckForNull
    @Override
    public BufferedReader getReader() throws IOException {
        if (readBody.compareAndSet(false, true)) {
            var body = cgiRequest.getBody();
            if (body == null) {
                return null;
            }
            return new BufferedReader(new InputStreamReader(body, getCharacterEncoding()));
        }
        throw new IllegalStateException("Body was already opened");
    }

    @Override
    public CgiRequest getNativeRequest() {
        return cgiRequest;
    }

    @Nonnull
    @Override
    public Cookies getCookies() {
        return cookies.get();
    }

    @Nonnull
    @Override
    public HttpParameters getParameters() {
        return parameters.get();
    }

    @Nonnull
    @Override
    public HttpMethod getMethod() {
        return method.get();
    }

    @Nonnull
    @Override
    public URI getUri() {
        return uri.get();
    }

    @Nonnull
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Nonnull
    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    @Override
    public Optional<Object> getBody() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public <T> Optional<T> getBody(@NonNull Argument<T> arg) {
        if (arg != null) {
            final Class<T> type = arg.getType();
            final MediaType contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
            long contentLength = getContentLength();
            if (body == null && contentLength != 0) {

                boolean isConvertibleValues = ConvertibleValues.class == type;
                if (isFormSubmission(contentType)) {
                    body = getParameters();
                    if (isConvertibleValues) {
                        return (Optional<T>) Optional.of(body);
                    } else {
                        return Optional.empty();
                    }
                } else if (CharSequence.class.isAssignableFrom(type)) {
                    try (BufferedReader reader = getReader()) {
                        final T value = (T) IOUtils.readText(reader);
                        body = value;
                        return Optional.ofNullable(value);
                    } catch (IOException e) {
                        throw new CodecException("Error decoding request body: " + e.getMessage(), e);
                    }
                } else {

                    final MediaTypeCodec codec = codecRegistry.findCodec(contentType, type).orElse(null);
                    if (codec != null) {
                        try (InputStream inputStream = getInputStream()) {
                            if (isConvertibleValues) {
                                final Map map = codec.decode(Map.class, inputStream);
                                body = ConvertibleValues.of(map);
                                return (Optional<T>) Optional.of(body);
                            } else {
                                final T value = codec.decode(arg, inputStream);
                                body = value;
                                return Optional.ofNullable(value);
                            }
                        } catch (CodecException | IOException e) {
                            throw new CodecException("Error decoding request body: " + e.getMessage(), e);
                        }

                    }
                }
            } else {
                if (type.isInstance(body)) {
                    return (Optional<T>) Optional.of(body);
                } else {
                    if (body != null && body != parameters) {
                        final T result = ConversionService.SHARED.convertRequired(body, arg);
                        return Optional.ofNullable(result);
                    }
                }

            }
        }
        return Optional.empty();
    }

    private boolean isFormSubmission(MediaType contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }
//
//    @Nonnull
//    @Override
//    public <T> Optional<T> getBody(@Nonnull Argument<T> type) {
//        if (body == null) {
//            var contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
//            if (contentType.isTextBased()) {
//                try (var r = getReader()) {
//                    body = IOUtils.readText(r);
//                } catch (IOException e) {
//                    throw new UncheckedIOException(e);
//                }
//            } else {
//                throw new UnsupportedOperationException("I don't know how to read non-text request bodies yet.");
//            }
//        }
//        if (body != null) {
//            return Optional.ofNullable(
//                    (T) conversionService.convert(body, type)
//            );
//        }
//        return Optional.empty();
//    }

    private class CgiParameters extends ConvertibleMultiValuesMap<String> implements HttpParameters {
        CgiParameters() {
            super(
                    Map.copyOf(queryStringParser.parse(cgiRequest, getCharacterEncoding())),
                    conversionService
            );
        }
    }

    private static class EmptyCookies implements Cookies {
        @Override
        public Set<Cookie> getAll() {
            return Set.of();
        }

        @Override
        public Optional<Cookie> findCookie(CharSequence name) {
            return Optional.empty();
        }

        @Override
        public Collection<Cookie> values() {
            return Set.of();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            return Optional.empty();
        }
    }
}
