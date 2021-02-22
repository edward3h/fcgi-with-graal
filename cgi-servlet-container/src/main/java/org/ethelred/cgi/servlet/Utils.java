package org.ethelred.cgi.servlet;

import javax.annotation.CheckForNull;
import java.io.PrintWriter;

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
}
