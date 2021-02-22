package org.ethelred.cgi.servlet;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServletInputStreamWrapper extends ServletInputStream
{
    private final InputStream delegate;

    public ServletInputStreamWrapper(InputStream delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isFinished()
    {
        throw new UnsupportedOperationException("ServletInputStreamWrapper.isFinished");
    }

    @Override
    public boolean isReady()
    {
        throw new UnsupportedOperationException("ServletInputStreamWrapper.isReady");
    }

    @Override
    public void setReadListener(ReadListener readListener)
    {
        throw new UnsupportedOperationException("ServletInputStreamWrapper.setReadListener");
    }

    @Override
    public int read() throws IOException
    {
        return delegate.read();
    }
}
