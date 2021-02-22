package org.ethelred.cgi.plain;

import org.ethelred.cgi.CgiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * TODO
 *
 * @author eharman
 * @since 2020-10-12
 */
public class SystemCgiRequest implements CgiRequest
{
    private final Logger LOGGER = LoggerFactory.getLogger(SystemCgiRequest.class);

    public SystemCgiRequest()
    {
        LOGGER.debug("Env = {}", getEnv());
    }

    @Nonnull
    @Override
    public Map<String, String> getEnv()
    {
        return System.getenv();
    }

    @Nonnull
    @Override
    public InputStream getBody()
    {
        return System.in;
    }

    @Nonnull
    @Override
    public OutputStream getOutput()
    {
        return System.out;
    }
}
