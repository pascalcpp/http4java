package com.xpcf.http4java.webappservlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/4/2022 7:17 PM
 */
public class HelloServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.getWriter().println("hello");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
