package org.ethelred.cgi.micronaut;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private boolean got = false;
    private T value;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <V> Lazy<V> lazy(Supplier<V> supplier) {
        return new Lazy<>(supplier);
    }

    public T get() {
        if (!got) {
            got = true;
            value = supplier.get();
        }
        return value;
    }
}
