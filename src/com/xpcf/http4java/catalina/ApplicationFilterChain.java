package com.xpcf.http4java.catalina;

import cn.hutool.core.util.ArrayUtil;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/13/2022 11:02 PM
 */
public class ApplicationFilterChain implements FilterChain {

    private Filter[] filters;

    private Servlet servlet;

    int pos = 0;

    public ApplicationFilterChain(List<Filter> filterList, Servlet servlet) {
        this.filters = ArrayUtil.toArray(filterList, Filter.class);
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        if (pos < filters.length) {
            Filter filter = filters[pos++];
            filter.doFilter(servletRequest, servletResponse, this);
        } else {
            servlet.service(servletRequest, servletResponse);
        }
    }


}
