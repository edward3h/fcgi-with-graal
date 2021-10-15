package org.ethelred.cgi;

import java.io.PrintWriter;

/**
 * TODO
 *
 * @author eharman
 * @since 2020-10-12
 */
public interface CgiHandler
{
    CgiHandler NOT_IMPLEMENTED = cgiRequest -> {
        try (var w = new PrintWriter(cgiRequest.getOutput())) {
            w.print("Status: ");
            w.println(501); // Not Implemented
            w.println();
            w.println("Not Implemented.");
        }
    };

    void handleRequest(CgiRequest request);
}
