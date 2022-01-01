package com.xpcf.http4java;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 7:49 AM
 */
public class Bootstrap {
    public static void main(String[] args) {
        Socket s = null;
        try {
            int port = 4396;
            if (!NetUtil.isUsableLocalPort(port)) {
                Logger.println(port + " The port has been occupied, please find reason");
                return;
            }

            ServerSocket serverSocket = new ServerSocket(port);
            Logger.println("server success start at port : " + port);

            while (true) {

                // 与对应client的socket 并且生成请求对象
                s = serverSocket.accept();
                Request request = new Request(s);
                Logger.println("browser request string: \r\n" + request.getRequestString());
                Logger.println("uri: " + request.getUri());

                // 输出response

                Response response = new Response();
                String html = "hello world";
                response.getWriter().println(html);
                handle200(s, response);
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                s.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Logger.println("server star failed");
        }
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
        s.close();

    }

}
