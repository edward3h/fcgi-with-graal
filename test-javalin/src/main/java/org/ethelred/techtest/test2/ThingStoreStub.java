package org.ethelred.techtest.test2;

import java.util.List;
import java.util.Optional;

public class ThingStoreStub implements ThingStore
{
    @Override
    public List<Thing> getThings()
    {
        throw new UnsupportedOperationException("ThingStoreStub.getThings");
    }

    @Override
    public Optional<Thing> getThingById(int id)
    {
        throw new UnsupportedOperationException("ThingStoreStub.getThingById");
    }

    @Override
    public Thing createThing(Thing thing)
    {
        throw new UnsupportedOperationException("ThingStoreStub.createThing");
    }

    @Override
    public void updateThing(Thing thing)
    {
        throw new UnsupportedOperationException("ThingStoreStub.updateThing");
    }

    @Override
    public void deleteThing(Thing thing)
    {
        throw new UnsupportedOperationException("ThingStoreStub.deleteThing");
    }
}
