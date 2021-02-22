package org.ethelred.cgi.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

public class ServletOutputStreamWrapper extends ServletOutputStream
{
    private final OutputStream delegate;

    public ServletOutputStreamWrapper(OutputStream delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isReady()
    {
        throw new UnsupportedOperationException("ServletOutputStreamWrapper.isReady");
    }

    @Override
    public void setWriteListener(WriteListener writeListener)
    {
        throw new UnsupportedOperationException("ServletOutputStreamWrapper.setWriteListener");
    }

    @Override
    public void write(int b) throws IOException
    {
        delegate.write(b);
    }
}
