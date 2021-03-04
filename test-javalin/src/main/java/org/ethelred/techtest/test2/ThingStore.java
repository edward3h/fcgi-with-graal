package org.ethelred.techtest.test2;

import java.util.List;
import java.util.Optional;

/**
 * data store - traditional CRUD example
 */
public interface ThingStore
{
    List<Thing> getThings();

    Optional<Thing> getThingById(int id);

    Thing createThing(Thing thing);

    void updateThing(Thing thing);

    void deleteThing(Thing thing);
}
