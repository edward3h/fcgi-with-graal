package org.ethelred.techtest.test2;

import org.immutables.value.Value;

import javax.annotation.Nullable;

/**
 * example model
 */
@Value.Immutable
public interface Thing
{
    int id();
    @Nullable
    String name();
    @Nullable
    String colour();
}
