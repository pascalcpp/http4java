package com.xpcf.http4java.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.servlet.DefaultServlet;
import com.xpcf.http4java.servlet.InvokerServlet;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.SessionManager;
import com.xpcf.http4java.util.WebXMLUtil;
import com.xpcf.http4java.webappservlet.HelloServlet;
import jdk.net.Sockets;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/4/2022 6:46 PM
 */
public class HttpProcessor {

    public void execute(Socket s, Request request, Response response) {

        try {

            prepareSession(request, response);

            String uri = request.getUri();
            // 根据uri 处理
            if (null == uri) {
                return;
            }

            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);
//            LogFactory.get().info("uri: " + uri);

            if (null != servletClassName) {
                InvokerServlet.getInstance().service(request, response);
            } else {
                DefaultServlet.getInstance().service(request, response);
            }

            if (Constant.CODE200 == response.getStatus()) {
                handle200(s, response);
                return;
            }

            if (Constant.CODE404 == response.getStatus()) {
                handle404(s, uri);
                return;
            }

        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(s, e);
        } finally {
            try {
                if (!s.isClosed()) {
                    s.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void prepareSession(Request request, Response response) {
        String jsessionId = request.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jsessionId, request, response);
        request.setSession(session);
    }

    private static void handle500(Socket s, Exception e) {
        try {
            OutputStream os = s.getOutputStream();
            StackTraceElement[] stes = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString());
            sb.append("\r\n");
            for (StackTraceElement ste : stes) {
                sb.append("\t");
                sb.append(ste.toString());
                sb.append("\r\n");
            }

            String msg = e.getMessage();
            if (null != msg && msg.length() > 20) {
                msg = msg.substring(0, 20);
            }

            String text = StrUtil.format(Constant.textFormat500, msg, e.toString(), sb.toString());
            text = Constant.responseHead500 + text;
            byte[] responseBytes = text.getBytes("UTF-8");
            os.write(responseBytes);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void handle404(Socket s, String uri) throws IOException {
        OutputStream os = s.getOutputStream();
        String responseText = StrUtil.format(Constant.textFormat404, uri, uri);
        responseText = Constant.responseHead404 + responseText;
        byte[] responseBytes = responseText.getBytes("UTF-8");
        os.write(responseBytes);
    }

    private static void handle200(Socket s, Response response) throws IOException {

        // 构建request head
        String contentType = response.getContentType();
        String headText = Constant.responseHead202;
        String cookiesHeader = response.getCookiesHeader();
        headText = StrUtil.format(headText, contentType, cookiesHeader);


        byte[] head = headText.getBytes();
        byte[] body = response.getBody();

        // 将head与body合并
        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);
        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
    }
}
