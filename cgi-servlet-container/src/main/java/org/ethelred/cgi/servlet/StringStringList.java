package org.ethelred.cgi.servlet;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;

public class StringStringList {
    private final Map<String, List<String>> data = new HashMap<>();

    public Collection<String> names() {
        return data.keySet();
    }

    public Collection<String> values(String name) {
        return data.get(name);
    }

    @CheckForNull
    public String firstValue(String name) {
        return data.containsKey(name) ? data.get(name).get(0) : null;
    }

    public void add(String name, String value) {
        data.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    public Collection<Map.Entry<String, List<String>>> entries() {
        return data.entrySet();
    }

    @Override
    public String toString() {
        return data.entrySet().stream()
                .map(e -> "%s=%s".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(";"));
    }
}
