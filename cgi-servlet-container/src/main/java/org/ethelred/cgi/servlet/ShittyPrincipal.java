package org.ethelred.cgi.servlet;

import java.security.Principal;
import java.util.Objects;
import java.util.StringJoiner;

public class ShittyPrincipal implements Principal
{
    public ShittyPrincipal(String name)
    {
        this.name = name;
    }

    private final String name;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShittyPrincipal that = (ShittyPrincipal) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
