package org.ethelred.cgi.standalone;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.fcgi.server.proxy.FastCGIProxyServlet;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

/**
 *
 * @author edward
 */
public class ExtFastCGIProxyServlet extends FastCGIProxyServlet {

    @Override
    protected void customizeFastCGIHeaders(Request proxyRequest, HttpFields fastCGIHeaders) {
        super.customizeFastCGIHeaders(proxyRequest, fastCGIHeaders);
        fastCGIHeaders.put("HTTP_AUTHORIZATION", proxyRequest.getHeaders().get(HttpHeader.AUTHORIZATION));
        System.err.println("Poopoo " + proxyRequest.getHeaders().get(HttpHeader.AUTHORIZATION));   
    }

}
