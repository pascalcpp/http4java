package com.xpcf.http4java.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.WebXMLUtil;

import javax.sql.rowset.JoinRowSet;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/3/2022 12:10 AM
 */
public class Connector implements Runnable {
    private int port;

    private Service service;

    public Connector(Service service) {
        this.service = service;
    }


    public Service getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }



    protected static void handle500(Socket s, Exception e) {
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

    protected static void handle404(Socket s, String uri) throws IOException {
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
        headText = StrUtil.format(headText, contentType);


        byte[] head = headText.getBytes();
        byte[] body = response.getBody();

        // 将head与body合并
        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);
        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
    }

    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]", port);
    }

    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]", port);
        new Thread(this).start();
    }

    @Override
    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket s = serverSocket.accept();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(s, service);
                            Response response = new Response();
                            String uri = request.getUri();
                            // 根据uri 处理
                            if (null == uri) {
                                return;
                            }

                            LogFactory.get().info("uri: " + uri);
                            Context context = request.getContext();


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
                            } else {
                                handle404(s, uri);
                                return;
                            }

                            // 输出response
                            handle200(s, response);
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
                };
                ThreadUtil.execute(r);
            }

        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }
}
