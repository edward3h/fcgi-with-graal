package org.ethelred.cgi.graal;

import java.util.function.Supplier;

import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.graal.libfcgi.LibFCGI;
import org.ethelred.cgi.plain.PlainCgiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author eharman
 * @since 2020-10-22
 */
public class CgiServerFactory implements Supplier<CgiServer>
{
    protected final Logger LOGGER = LoggerFactory.getLogger(CgiServerFactory.class);
    private CgiServer instance;

    @Override
    public synchronized CgiServer get() {
        if (instance == null) {
            try
            {
                if (LibFCGI.FCGX_IsCGI() > 0)
                {
                    instance = new PlainCgiServer();
                    LOGGER.info("Using Plain CGI");
                } else
                {
                    instance = new LibFCGIServer();
                    LOGGER.info("Using libfcgi");
                }
            } catch (Throwable e) {
                LOGGER.warn("Error evaluating CGI type, will default to plain CGI", e);
                instance = new PlainCgiServer();
            }
        }
        return instance;
    }
}
