package org.ethelred.cgi;

import java.util.HashMap;
import java.util.Map;

public class Options {
    public static Options empty() {
        return new Options();
    }

    public static Options of(String key1, Object value1) {
        var o = new Options();
        o.values.put(key1, value1);
        return o;
    }

    private Options() {}

    private final Map<String, Object> values = new HashMap<>();

    public <T> T get(String key, T defaultValue) {
        return (T) values.getOrDefault(key, defaultValue);
    }
    public <T> T get(String key) {
        return (T) values.get(key);
    }

    @Override
    public String toString() {
        return "Options{" + values +
                '}';
    }
}
