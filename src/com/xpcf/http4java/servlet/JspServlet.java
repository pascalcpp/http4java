package com.xpcf.http4java.servlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.catalina.Context;
import com.xpcf.http4java.classloader.JspClassLoader;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.JspUtil;
import com.xpcf.http4java.util.WebXMLUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/11/2022 10:18 PM
 */
public class JspServlet extends HttpServlet {
    public static final long serialVersion  = 1L;
    public static JspServlet jspServlet = new JspServlet();

    public static synchronized JspServlet getInstance() {
        return jspServlet;
    }

    public JspServlet() {
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;

        String uri = request.getUri();

        if ("/".equals(uri)) {
            uri = WebXMLUtil.getWelComeFile(request.getContext());
        }

        String fileName = StrUtil.removePrefix(uri, "/");
        File jspFile = FileUtil.file(request.getRealPath(uri));

        try {
            if (jspFile.exists()) {
                Context context = request.getContext();
                String path = context.getPath();
                String subFolder;
                if ("/".equals(path)) {
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, "/", false);
                }

                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);
                if (!jspServletClassFile.exists()) {
                    JspUtil.compileJsp(context, jspFile);
                } else if (jspFile.lastModified() > jspServletClassFile.lastModified()) {
                    JspUtil.compileJsp(context, jspFile);
                    JspClassLoader.invalidJspClassloader(uri, context);
                }

                String extName = FileUtil.extName(jspFile);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                JspClassLoader jspClassloader = JspClassLoader.getJspClassloader(uri, context);
                String jspServletClassName = JspUtil.getJspServletClassName(uri, subFolder);
                Class<?> jspServletClass = jspClassloader.loadClass(jspServletClassName);

                HttpServlet servlet = context.getServlet(jspServletClass);
                servlet.service(request, response);
                if (null != response.getRedirectPath()) {
                    response.setStatus(Constant.CODE302);
                } else {
                    response.setStatus(Constant.CODE200);
                }

            } else {
                response.setStatus(Constant.CODE404);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}

