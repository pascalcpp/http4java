package com.xpcf.http4java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import com.xpcf.http4java.catalina.Context;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.ServerXMLUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 7:49 AM
 */
public class Bootstrap {

    public static Map<String, Context> contextMap = new HashMap<>();

    public static void main(String[] args) {
        Socket s = null;
        try {
            int port = 4396;

            // 打印server信息
            logJVM();

            // 根据webapps内folder 生成context
            scanContextsOnWebAppsFolder();
            // 根据server.xml
            scanContextiInServerXML();
//            if (!NetUtil.isUsableLocalPort(port)) {
//                Logger.println(port + " The port has been occupied, please find reason");
//                return;
//            }

            ServerSocket serverSocket = new ServerSocket(port);
            Logger.println("server success start at port : " + port);

            while (true) {

                s = serverSocket.accept();
                Socket finalS = s;

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(finalS);
                            Response response = new Response();
                            String uri = request.getUri();
                            // 根据uri 处理
                            if (null == uri) {
                                return;
                            }

                            Logger.println("uri: " + uri);
                            Context context = request.getContext();

                            // 构建response body
                            if ("/".equals(uri)) {
                                String html = "hello world";
                                response.getWriter().println(html);

                            } else {

                                // 去掉第一个/ 直接在ROOT中查询
                                String fileName = StrUtil.removePrefix(uri, "/");
                                //根据uri从ROOT dir 中获取指定文件
                                File file = FileUtil.file(context.getDocBase(), fileName);
                                if (file.exists()) {
                                    String fileContent = FileUtil.readUtf8String(file);
                                    response.getWriter().println(fileContent);

                                    if ("timeConsume.html".equals(fileName)) {
                                        ThreadUtil.sleep(1000);
                                    }

                                } else {
                                    response.getWriter().println("File Not Found");
                                }

                            }

                            // 输出response

                            handle200(finalS, response);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                };

                ThreadUtil.execute(r);

            }

        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
            try {
                s.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } finally {
//            try {
//                s.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    /**
     * 将构建成的response 写入到socket输出流
     * @param s
     * @param response
     * @throws IOException
     */
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


    /**
     * initial log
     */
    private static void logJVM() {
        Map<String, String> infos = new LinkedHashMap<>();

        infos.put("Server version", "HTTP4JAVA/1.0.0");
        infos.put("Server built", "2022-01-01");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key + ":\t\t" + infos.get(key));
        }

    }


    /**
     *
     */
    private static void scanContextsOnWebAppsFolder() {
        File[] folders = Constant.webappsFolder.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory()) {
                continue;
            }
            loadContext(folder);
        }
    }


    private static void scanContextiInServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts();
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    /**
     *
     * @param folder
     */
    private static void loadContext(File folder) {
        String path = folder.getName();

        if ("ROOT".equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);
        contextMap.put(context.getPath(), context);

    }

}
