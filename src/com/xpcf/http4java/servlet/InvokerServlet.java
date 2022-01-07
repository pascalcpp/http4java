package com.xpcf.http4java.servlet;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.catalina.Context;
import com.xpcf.http4java.classloader.WebappClassLoader;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.util.Constant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 3:23 PM
 */
public class InvokerServlet extends HttpServlet {
    private static InvokerServlet instance = new InvokerServlet();


    public static InvokerServlet getInstance() {
        return instance;
    }

    public InvokerServlet() {

    }


    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);

        try {
            Class<?> servletClazz = context.getWebappClassLoader().loadClass(servletClassName);

            LogFactory.get().info("servletClass: " + servletClazz);
            LogFactory.get().info("servletClass ClassLoader: " + servletClazz.getClassLoader());

            Object servletObject = ReflectUtil.newInstance(servletClazz);
            ReflectUtil.invoke(servletObject, "service", request, response);
            response.setStatus(Constant.CODE200);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
