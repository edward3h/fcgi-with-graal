package org.ethelred.cgi.micronaut;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpHeaders;
import org.ethelred.cgi.CgiRequest;
import org.ethelred.cgi.util.HeaderHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CgiHeaders implements HttpHeaders {
    private final HeaderHelper helper;
    private final ConversionService conversionService;

    public CgiHeaders(CgiRequest cgiRequest, ConversionService conversionService) {
        this.helper = new HeaderHelper(cgiRequest.getEnv());
        this.conversionService = conversionService;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        return helper.getAll(name.toString());
    }

    @Override
    public String get(CharSequence name) {
        var r = getAll(name);
        return r == null || r.isEmpty() ? null : r.get(0);
    }

    @Override
    public Set<String> names() {
        return helper.names();
    }

    @Override
    public Collection<List<String>> values() {
        return helper.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        var r = Optional.ofNullable(get(name));
        return r.flatMap(v -> conversionService.convert(v, conversionContext));
    }
}
