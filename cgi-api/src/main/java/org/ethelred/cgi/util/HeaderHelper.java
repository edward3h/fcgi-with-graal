package org.ethelred.cgi.util;

import java.util.*;
import java.util.stream.Collectors;

public class HeaderHelper
{
    private final static Set<String> KNOWN_HEADERS = Set.of(
            "Accept",
            "Accept-Charset",
            "Accept-Encoding",
            "Accept-Language",
            "Accept-Ranges",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Origin",
            "Access-Control-Expose-Headers",
            "Access-Control-Max-Age",
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Age",
            "Allow",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Content-Encoding",
            "Content-Disposition",
            "Content-Language",
            "Content-Length",
            "Content-Location",
            "Content-Range",
            "Content-Type",
            "Cookie",
            "Date",
            "ETag",
            "Expect",
            "Expires",
            "From",
            "Host",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Range",
            "If-Unmodified-Since",
            "Last-Modified",
            "Link",
            "Location",
            "Max-Forwards",
            "Origin",
            "Pragma",
            "Proxy-Authenticate",
            "Proxy-Authorization",
            "Range",
            "Referer",
            "Retry-After",
            "Server",
            "Set-Cookie",
            "Sec-WebSocket-Key",
            "TE",
            "Trailer",
            "Transfer-Encoding",
            "Upgrade",
            "User-Agent",
            "Vary",
            "Via",
            "Warning",
            "WWW-Authenticate",
            "X-HTTP-Method-Override"
            );

    private static final Map<String, String> MULTI_VALUE_HEADER_TO_SEPARATOR =
            Map.of("Accept", ",",
                    "Cookie", ";"); //TODO

    private static final Map<String, String> ENV_NAME_TO_KNOWN_NAME =
            KNOWN_HEADERS.stream()
            .flatMap(known ->
                List.of(
                     Map.entry(toEnvName(known), known),
                     Map.entry("HTTP_" + toEnvName(known), known)
                ).stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Set<String> BLACKLIST = Set.of("HTTP_ACCEPT_ENCODING");

    private static String toEnvName(String known)
    {
        return known.toUpperCase().replaceAll("-", "_");
    }

    private final Map<String, List<String>> headers;
    public HeaderHelper(Map<String, String> cgiRequestEnv)
    {
        headers = cgiRequestEnv.entrySet()
                .stream()
                .filter(e -> !BLACKLIST.contains(e.getKey()))
                .collect(Collectors.toMap(this::mapKey, this::mapValue));
    }

    private List<String> mapValue(Map.Entry<String, String> entry)
    {
        var separator = MULTI_VALUE_HEADER_TO_SEPARATOR.get(mapKey(entry));
        if (separator == null) {
            return List.of(entry.getValue());
        }
        return List.of(entry.getValue().split(separator));
    }

    private String mapKey(Map.Entry<String, String> entry)
    {
        return ENV_NAME_TO_KNOWN_NAME.getOrDefault(entry.getKey(), entry.getKey());
    }

    public Optional<String> getOptionalHeader(String name)
    {
        var e = getHeaders(name);
        return e.hasMoreElements() ? Optional.of(e.nextElement()) : Optional.empty();
    }

    public List<String> getAll(String name) {
        var k = name;
        if (!KNOWN_HEADERS.contains(name))
        {
            k = toEnvName(name);
        }
        var r = headers.get(k);
        return r == null ? List.of() : r;
    }

    public Enumeration<String> getHeaders(String name)
    {
        return Collections.enumeration(getAll(name));
    }

    public Enumeration<String> getHeaderNames()
    {
        return Collections.enumeration(headers.keySet());
    }

    public Set<String> names() {
        return headers.keySet();
    }

    public Collection<List<String>> values() {
        return headers.values();
    }
}
