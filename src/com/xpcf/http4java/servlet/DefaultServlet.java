package com.xpcf.http4java.servlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.catalina.Context;
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

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 3:48 PM
 */
public class DefaultServlet extends HttpServlet {

    private static DefaultServlet instance = new DefaultServlet();

    public static DefaultServlet getInstance() {
        return instance;
    }

    public DefaultServlet() {

    }


    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Request request = (Request) req;
        Response response = (Response) resp;

        String uri = request.getUri();
        Context context = request.getContext();


        //test exception
        if ("/500.html".equals(uri)) {
            throw new RuntimeException("this is a deliberately created exception");
        }


        if ("/".equals(uri)) {
            uri = WebXMLUtil.getWelComeFile(request.getContext());
        }
        // 去掉第一个/
        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(context.getDocBase(), fileName);
        if (file.exists()) {

            String extName = FileUtil.extName(fileName);
            String mimeType = WebXMLUtil.getMimeType(extName);
            response.setContentType(mimeType);

//                                String fileContent = FileUtil.readUtf8String(file);
//                                response.getWriter().println(fileContent);

            byte[] body = FileUtil.readBytes(file);
            response.setBody(body);

            if ("timeConsume.html".equals(fileName)) {
                ThreadUtil.sleep(1000);
            }
            response.setStatus(Constant.CODE200);
        } else {
            response.setStatus(Constant.CODE404);
        }
    }


}