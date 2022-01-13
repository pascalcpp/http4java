package com.xpcf.http4java.http;

import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/13/2022 9:00 PM
 */
public class StandardFilterConfig implements FilterConfig {

    private ServletContext servletContext;

    private Map<String, String> initParameters;

    private String filterName;

    public StandardFilterConfig(ServletContext servletContext, Map<String, String> initParameters, String filterName) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.filterName = filterName;

        if (null == initParameters) {
            // avoid null pointer
            this.initParameters = new HashMap<>();
        }
    }

    @Override
    public String getFilterName() {
        return filterName;
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
