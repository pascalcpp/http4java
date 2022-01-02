package com.xpcf.http4java.http;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.Bootstrap;
import com.xpcf.http4java.catalina.Context;
import com.xpcf.http4java.catalina.Engine;
import com.xpcf.http4java.catalina.Host;
import com.xpcf.http4java.catalina.Service;
import com.xpcf.http4java.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 11:03 AM
 */
public class Request {

    private String requestString;

    private String uri;

    private Socket socket;

    private Context context;

    private Service service;

    public Request(Socket socket, Service service) throws IOException {

        this.socket = socket;
        this.service = service;

        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) {
            return;
        }
        parseUri();
        parseContext();

        if (!"/".equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
            if (StrUtil.isEmpty(uri)) {
                uri = "/";
            }
        }
    }


    /**
     * 根据uri 解析context
     */
    private void parseContext() {
        Engine engine = service.getEngine();
        context = engine.getDefaultHost().getContext(uri);

        if (null != context) {
            return;
        }

        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path) {
            path = "/";
        } else {
            path = "/" + path;
        }
        context = engine.getDefaultHost().getContext(path);
        // 如果 /path 在contextmap中没有 则默认使用 / 对应的context
        if (null == context) {
            context = engine.getDefaultHost().getContext("/");
        }
    }

    private void parseHttpRequest() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream);
        requestString = new String(bytes, "utf-8");
//        LogFactory.get().info(requestString);
    }

    private void parseUri() {
        String temp;
        temp = StrUtil.subBetween(requestString, " ", " ");
        if (!StrUtil.contains(temp, '?')) {
            uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp, '?', false);
        uri = temp;
    }

    public String getUri() {
        return uri;
    }

    public String getRequestString() {
        return requestString;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

