package org.ethelred.cgi.servlet;

import javax.annotation.CheckForNull;
import java.io.PrintWriter;
import java.util.function.*;

class Utils
{
    Utils(){}

    public static void renderErrorPage(PrintWriter writer, int sc, @CheckForNull String msg)
    {
        var message = msg == null ? "Something went wrong" : msg;
        writer.println("<html><head><title>Error " + sc + "</title></head>" +
                "<body><h1>Error " + sc + "</h1>" +
                "<p>" + message + "</p>" +
                "</body></html>" );
    }

    public static <T> void ifNotNull(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public static void splitEntries(String input, String listPattern, String pairPattern, BiConsumer<String, String> function) {
        for (var item: input.split(listPattern)) {
            var pair = item.split(pairPattern, 2);
            var key = pair[0];
            var value = pair.length == 2 ? pair[1] : "";
            function.accept(key, value);
        }
    }
}
