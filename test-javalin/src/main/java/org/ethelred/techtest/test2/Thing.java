package org.ethelred.techtest.test2;

import org.immutables.value.Value;

/**
 * example model
 */
@Value.Immutable
public interface Thing
{
    int id();
    String name();
    String colour();
}
