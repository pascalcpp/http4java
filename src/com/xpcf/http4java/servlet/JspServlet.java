package com.xpcf.http4java.servlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.util.Constant;
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
        File file = FileUtil.file(request.getRealPath(uri));

        if (file.exists()) {
            String extName = FileUtil.extName(file);
            String mimeType = WebXMLUtil.getMimeType(extName);
            response.setContentType(mimeType);

            byte[] body = FileUtil.readBytes(file);
            response.setBody(body);
            response.setStatus(Constant.CODE200);

        } else {
            response.setStatus(Constant.CODE404);
        }



    }

}

