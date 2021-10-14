package org.ethelred.cgi.micronaut;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.http.simple.cookies.SimpleCookies;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;

import javax.annotation.CheckForNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class CookieDecoder implements Function<String, Cookies> {
    /*
largely copied from Netty ServerCookieDecoder
 */

    private static final String RFC2965_VERSION = "$Version";

    private static final String RFC2965_PATH = "$" + CookieHeaderNames.PATH;

    private static final String RFC2965_DOMAIN = "$" + CookieHeaderNames.DOMAIN;

    private static final String RFC2965_PORT = "$Port";

    private final ConversionService<?> conversionService;

    public CookieDecoder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Cookies apply(String s) {
        var cookies = new SimpleCookies(conversionService);
        decode(s, cookies);
        return cookies;
    }

    private void decode(String header, SimpleCookies simpleCookies) {
        if (header == null) {
            return;
        }

        final int headerLen = header.length();

        if (headerLen == 0) {
            return;
        }

        int i = 0;

        boolean rfc2965Style = false;
        if (header.regionMatches(true, 0, RFC2965_VERSION, 0, RFC2965_VERSION.length())) {
            // RFC 2965 style cookie, move to after version value
            i = header.indexOf(';') + 1;
            rfc2965Style = true;
        }

        loop: for (;;) {

            // Skip spaces and separators.
            for (;;) {
                if (i == headerLen) {
                    break loop;
                }
                char c = header.charAt(i);
                if (c == '\t' || c == '\n' || c == 0x0b || c == '\f'
                        || c == '\r' || c == ' ' || c == ',' || c == ';') {
                    i++;
                    continue;
                }
                break;
            }

            int nameBegin = i;
            int nameEnd;
            int valueBegin;
            int valueEnd;

            for (;;) {

                char curChar = header.charAt(i);
                if (curChar == ';') {
                    // NAME; (no value till ';')
                    nameEnd = i;
                    valueBegin = valueEnd = -1;
                    break;

                } else if (curChar == '=') {
                    // NAME=VALUE
                    nameEnd = i;
                    i++;
                    if (i == headerLen) {
                        // NAME= (empty value, i.e. nothing after '=')
                        valueBegin = valueEnd = 0;
                        break;
                    }

                    valueBegin = i;
                    // NAME=VALUE;
                    int semiPos = header.indexOf(';', i);
                    valueEnd = i = semiPos > 0 ? semiPos : headerLen;
                    break;
                } else {
                    i++;
                }

                if (i == headerLen) {
                    // NAME (no value till the end of string)
                    nameEnd = headerLen;
                    valueBegin = valueEnd = -1;
                    break;
                }
            }

            if (rfc2965Style && (header.regionMatches(nameBegin, RFC2965_PATH, 0, RFC2965_PATH.length()) ||
                    header.regionMatches(nameBegin, RFC2965_DOMAIN, 0, RFC2965_DOMAIN.length()) ||
                    header.regionMatches(nameBegin, RFC2965_PORT, 0, RFC2965_PORT.length()))) {

                // skip obsolete RFC2965 fields
                continue;
            }

            var name = validName(header, nameBegin, nameEnd);
            if (name != null) {
                var value = validValue(header, valueBegin, valueEnd);
                if (value != null) {
                    simpleCookies.put(name, new SimpleCookie(name, value));
                }
            }
        }
    }

    @CheckForNull
    String validName(String s, int begin, int end) {
        if (begin > -1 && end > begin) {
            return s.substring(begin, end);
        }
        return null;
    }

    @CheckForNull
    String validValue(String s, int begin, int end) {
        if (begin > -1 && end > begin) {
            if (s.charAt(begin) == '"') {
                if ((end - begin) > 2 && s.charAt(end - 1) == '"') {
                    return s.substring(begin + 1, end - 1);
                }
            }
            else
            {
                return s.substring(begin, end);
            }
        }
        return null;
    }
}
