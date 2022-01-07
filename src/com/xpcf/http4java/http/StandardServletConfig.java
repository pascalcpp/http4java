package com.xpcf.http4java.http;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/8/2022 12:08 AM
 */
public class StandardServletConfig implements ServletConfig {

    private ServletContext servletContext;

    private Map<String, String> initParameters;

    private String servletName;

    public StandardServletConfig(ServletContext servletContext, Map<String, String> initParameters, String servletName) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.servletName = servletName;

        if (null == this.initParameters) {
            this.initParameters = new HashMap<>();
        }
    }


    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }
}
