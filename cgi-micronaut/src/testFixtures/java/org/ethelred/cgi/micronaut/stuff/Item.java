package org.ethelred.cgi.micronaut.stuff;

import io.micronaut.core.annotation.Introspected;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@Introspected
public class Item {
    @Nullable
    private final Integer id;
    private final String name;
    private final int count;

    public Item(@Nullable Integer id, String name, int count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public Item(String name, int count) {
        this(null, name, count);
    }

    @CheckForNull
    public Integer getId() {
        return id;
    }

    public Item withId(Integer id) {
        return new Item(id, name, count);
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
