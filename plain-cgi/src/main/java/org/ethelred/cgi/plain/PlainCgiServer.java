package org.ethelred.cgi.plain;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author eharman
 * @since 2020-10-12
 */
public class PlainCgiServer implements CgiServer
{
    private final Logger LOGGER = LoggerFactory.getLogger(PlainCgiServer.class);
    private Callback callback = Callback.ignore();

    @Override
    public void init(Callback callback, Options options)
    {
        this.callback = callback;
    }

    @Override
    public void start(CgiHandler handler)
    {
        try
        {
            handler.handleRequest(new SystemCgiRequest());
        }
        catch (Exception e) {
            LOGGER.error("Unhandled handler error", e);
        }
        finally
        {
            callback.onCompleted();
        }
    }

    @Override
    public void shutdown()
    {
        // no-op
    }

    @Override
    public boolean isSingleRequest()
    {
        return true;
    }

    @Override
    public void waitForCompletion(long timeout, TimeUnit unit)
    {
        // no-op
    }

    @Override
    public boolean isRunning()
    {
        // kinda, since it doesn't continue running after handling a request
        return false;
    }
}
