package com.xpcf.http4java.http;

import com.xpcf.http4java.catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/13/2022 3:45 PM
 */
public class ApplicationRequestDispatcher implements RequestDispatcher {

    private String uri;

    public ApplicationRequestDispatcher(String uri) {
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        this.uri = uri;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;
        request.setUri(uri);
        HttpProcessor httpProcessor = new HttpProcessor();
        httpProcessor.execute(request.getSocket(), request, response);
        request.setForwarded(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

    }
}
