package org.ethelred.cgi.util;

import org.ethelred.cgi.CgiParam;
import org.ethelred.cgi.CgiRequest;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

public class QueryStringParser {
    public Map<String, List<String>> parse(CgiRequest request, Charset charset) {
        var queryString = request.getParam(CgiParam.QUERY_STRING);
        if (queryString != null) {
            return parse(queryString, charset);
        }
        return Map.of();
    }

    private Map<String, List<String>> parse(String queryString, Charset charset) {
        Map<String, List<String>> acc = new LinkedHashMap<>();
        for (var p: queryString.split("&")) {
            var kv = p.split("=");
            if (kv.length == 1) {
                add(acc, kv[0], true, charset);
            } else {
                add(acc, kv[0], kv[1], charset);
            }
        }
        return Map.copyOf(acc);
    }

    private void add(Map<String, List<String>> acc, String key, Object value, Charset charset) {
        acc.computeIfAbsent(key, k -> new ArrayList<>()).add(URLDecoder.decode(String.valueOf(value), charset));
    }
}
