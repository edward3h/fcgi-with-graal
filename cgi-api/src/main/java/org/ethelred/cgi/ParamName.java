package org.ethelred.cgi;

/**
 * TODO
 *
 * @author eharman
 * @since 2020-10-27
 */
@FunctionalInterface
public interface ParamName
{
    static ParamName httpHeader(CharSequence headerName) {
        return () -> "HTTP_" + of(headerName).getName();
    }

    static ParamName of(CharSequence headerName)
    {
        return () -> headerName.toString().toUpperCase().replaceAll("-", "_");
    }

    String getName();
}
