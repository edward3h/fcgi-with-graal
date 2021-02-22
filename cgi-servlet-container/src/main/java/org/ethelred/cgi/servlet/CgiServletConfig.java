package org.ethelred.cgi.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class CgiServletConfig implements ServletConfig
{
    public CgiServletConfig(CgiServletContainer cgiServletContainer)
    {
    }

    @Override
    public String getServletName()
    {
        throw new UnsupportedOperationException("CgiServletConfig.getServletName");
    }

    @Override
    public ServletContext getServletContext()
    {
        throw new UnsupportedOperationException("CgiServletConfig.getServletContext");
    }

    @Override
    public String getInitParameter(String name)
    {
        throw new UnsupportedOperationException("CgiServletConfig.getInitParameter");
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        throw new UnsupportedOperationException("CgiServletConfig.getInitParameterNames");
    }
}
