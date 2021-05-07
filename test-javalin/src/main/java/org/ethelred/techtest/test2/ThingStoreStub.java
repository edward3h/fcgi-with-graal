package org.ethelred.techtest.test2;

import com.github.javafaker.Faker;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class ThingStoreStub implements ThingStore
{
    private final SortedMap<Integer, Thing> map = new TreeMap<>();

    private static class ThingImpl implements Thing
    {
        private final int id;
        private final String name;
        private final String colour;

        public ThingImpl(int id, String name, String colour)
        {
            this.id = id;
            this.name = name;
            this.colour = colour;
        }

        @Override
        public int id()
        {
            return id;
        }

        @Override
        public String name()
        {
            return name;
        }

        @Override
        public String colour()
        {
            return colour;
        }
    }

    public ThingStoreStub()
    {
        var faker = new Faker(new Random(1));
        for (int i = 0; i < 10; i++)
        {
            createThing(new ThingImpl(0, faker.name().fullName(), faker.color().name()));
        }
    }

    @Override
    public List<Thing> getThings()
    {
        return List.copyOf(map.values());
    }

    @Override
    public Optional<Thing> getThingById(int id)
    {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public Thing createThing(Thing thing)
    {
        int id = map.isEmpty() ? 1 : map.lastKey() + 1;
        var newValue = new ThingImpl(id, thing.name(), thing.colour());
        map.put(id, newValue);
        return newValue;
    }

    @Override
    public void updateThing(Thing thing)
    {
        var id = thing.id();
        if (map.containsKey(id))
        {
            var newValue = new ThingImpl(id, thing.name(), thing.colour());
            map.put(id, newValue);
        }
    }

    @Override
    public void deleteThing(Thing thing)
    {
        var id = thing.id();
        map.remove(id);
    }
}
