package org.ethelred.cgi.util;

import org.ethelred.cgi.CgiParam;
import org.ethelred.cgi.CgiRequest;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class QueryStringParser {
    private static final Set<String> METHOD_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    public Map<String, List<String>> parse(CgiRequest request, Charset charset) {
        var result = new LinkedHashMap<String, List<String>>();
        var queryString = request.getOptionalParam(CgiParam.QUERY_STRING);
        queryString.ifPresent(qs -> parse(result, qs, charset));

        var method = request.getRequiredParam(CgiParam.REQUEST_METHOD).toUpperCase();
        if (METHOD_WITH_BODY.contains(method)) {
            var contentLength = request.getOptionalParam(CgiParam.CONTENT_LENGTH)
                    .map(Long::valueOf)
                    .orElse(-1L);
            if (contentLength > 0) {
                var contentType = request.getParam(CgiParam.CONTENT_TYPE);
                if (contentType != null) {
                    if (contentType.startsWith("application/x-www-form-urlencoded")) { // TODO multipart/form-data
                        try (var reader = new BufferedReader(
                                new InputStreamReader(
                                        Objects.requireNonNullElse(request.getBody(), InputStream.nullInputStream())
                                )
                        )
                        ) {
                            var body = reader.lines()
                                    .collect(Collectors.joining("\n"));
                            parse(result, body, charset);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
            }
        }

        return Map.copyOf(result);
    }

    private void parse(Map<String, List<String>> acc, String queryString, Charset charset) {
        for (var p : queryString.split("&")) {
            var kv = p.split("=");
            if (kv.length == 1) {
                add(acc, kv[0], true, charset);
            } else {
                add(acc, kv[0], kv[1], charset);
            }
        }
    }

    private void add(Map<String, List<String>> acc, String key, Object value, Charset charset) {
        acc.computeIfAbsent(key, k -> new ArrayList<>()).add(URLDecoder.decode(String.valueOf(value), charset));
    }
}
