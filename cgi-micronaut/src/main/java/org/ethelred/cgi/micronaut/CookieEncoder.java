package org.ethelred.cgi.micronaut;

import io.micronaut.http.cookie.Cookie;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;

import javax.annotation.CheckForNull;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

public class CookieEncoder implements Function<Cookie, String> {
    /*
    largely copied from Netty ServerCookieEncoder
     */
    @Override
    public String apply(Cookie cookie) {
        var name = Objects.requireNonNull(cookie.getName(), "cookie");
        var value = Objects.requireNonNullElse(cookie.getValue(), "");

        var buf = new StringBuilder();
        addIfValueNonNull(buf, name, value);

        if (cookie.getMaxAge() != Long.MIN_VALUE) {
            addIfValueNonNull(buf, CookieHeaderNames.MAX_AGE, cookie.getMaxAge());
            var expires = new Date(cookie.getMaxAge() * 1000 + System.currentTimeMillis());            buf.append(CookieHeaderNames.EXPIRES);
            buf.append('=');
            DateFormatter.append(expires, buf);
            buf.append(';').append(' ');
        }

        addIfValueNonNull(buf, CookieHeaderNames.PATH, cookie.getPath());
        addIfValueNonNull(buf, CookieHeaderNames.DOMAIN, cookie.getDomain());
        addIfSet(buf, CookieHeaderNames.SECURE, cookie.isSecure());
        addIfSet(buf, CookieHeaderNames.HTTPONLY, cookie.isHttpOnly());
        cookie.getSameSite().ifPresent(
                sameSite -> addIfValueNonNull(buf, CookieHeaderNames.SAMESITE, sameSite.name())
        );

        // strip last separator characters
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }

    private void addIfSet(StringBuilder buf, String name, boolean test) {
        if (test) {
            buf.append(name)
                    .append(';')
                    .append(' ');
        }
    }

    private void addIfValueNonNull(StringBuilder buf, String name, @CheckForNull Object value) {
        if (value == null) {
            return;
        }
        buf.append(name)
                    .append('=')
                    .append(value)
                    .append(';')
                    .append(' ');
    }
}
