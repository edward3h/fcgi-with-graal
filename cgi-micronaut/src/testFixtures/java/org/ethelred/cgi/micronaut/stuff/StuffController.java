package org.ethelred.cgi.micronaut.stuff;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.ArrayList;

@Controller("/stuff")
public class StuffController {
    private final ItemStore itemStore;

    public StuffController(ItemStore itemStore) {
        this.itemStore = itemStore;
    }

    @Get("/{?query}")
    public  Iterable<Item> search(@Nullable @QueryValue String query) {
        var items = itemStore.getAll();
        if (query == null || query.isBlank()) {
            return items;
        }
        var result = new ArrayList<Item>();
        for (var item: items
             ) {
            if (item.getName().contains(query)) {
                result.add(item);
            }
        }
        return result;
    }

    @Nullable
    @Get("/{id}")
    public Item getById(@PathVariable int id) {
        return itemStore.getById(id);
    }

    @Post(consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Item saveItem(@Body Item value) {
        return itemStore.save(value);
    }

    @Delete("{id}")
    public boolean deleteItem(@PathVariable int id) {
        return itemStore.delete(new Item(id, "", 0));
    }

}
