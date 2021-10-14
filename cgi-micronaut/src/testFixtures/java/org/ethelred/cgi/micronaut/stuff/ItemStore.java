package org.ethelred.cgi.micronaut.stuff;

import javax.annotation.CheckForNull;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Singleton
public class ItemStore {
    private final Map<Integer, Item> idToItem = new TreeMap<>();
    private int nextId = 1;

    public ItemStore() {
        _insertTestData();
    }

    private void _insertTestData() {
        List.of(new Item("pot", 2),
                new Item("jar", 19),
                new Item("chair", 4))
                .forEach(this::save);
    }

    public Iterable<Item> getAll() {
        return idToItem.values();
    }

    @CheckForNull
    public Item getById(int id) {
        return idToItem.get(id);
    }

    public Item save(Item item) {
        var id = Objects.requireNonNullElseGet(item.getId(), this::getNextId);
        item  = item.withId(id);
        idToItem.put(id, item);
        return item;
    }

    private Integer getNextId() {
        return nextId++;
    }

    public boolean delete(Item item) {
        var id = item.getId();
        return id != null && idToItem.remove(id, item);
    }
}
