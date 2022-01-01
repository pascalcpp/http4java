package com.xpcf.http4java.util;

import org.apache.tools.ant.taskdefs.EchoXML;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 8:08 AM
 */
public class MiniBrowser {


    public static void main(String[] args) {
        int port = 4396;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket accept = serverSocket.accept();
            InputStream is = accept.getInputStream();
            System.out.println("c1");
            byte[] bytes = MiniBrowser.readBytes(is);
            System.out.println("c132");
            System.out.println(new String(bytes));
            System.out.println("asdasd");
        } catch (IOException e) {
            e.printStackTrace();
        }


//        String url = "http://127.0.0.1:4396/";
//        String contentString= getContentString(url,false);
//        System.out.println(contentString);
//        String httpString= getHttpString(url,false);
//        System.out.println(httpString);
    }


    public static byte[] getContentBytes(String url) {
        return getContentBytes(url, false);
    }

    public static String getContentString(String url) {
        return getContentString(url, false);
    }

    public static String getContentString(String url, boolean gzip) {
        byte[] result = getContentBytes(url, gzip);

        if (null == result) {
            return null;
        }

        try {
            return new String(result, "utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getContentBytes(String url, boolean gzip) {
        byte[] response = getHttpBytes(url, gzip);
        byte[] doubleReturn = "\r\n\r\n".getBytes();

        int pos = -1;
        for (int i = 0; i < response.length - doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);

            if (Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }

        if (-1 == pos) {
            return null;
        }

        pos += doubleReturn.length;
        byte[] result = Arrays.copyOfRange(response, pos, response.length);
        return result;
    }

    public static String getHttpString(String url, boolean gzip) {
        byte[] bytes = getHttpBytes(url, gzip);
        return new String(bytes).trim();
    }

    public static String getHttpString(String url) {
        return getHttpString(url, false);
    }

    public static byte[] getHttpBytes(String url, boolean gzip) {
        byte[] result = null;
        try {
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if (port == -1) {
                port = 80;
            }
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            client.connect(inetSocketAddress, 1000);
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Host", u.getHost()+":"+port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "simple browser / java1.8");

            if (gzip) {
                requestHeaders.put("Accept-Encoding", "gzip");
            }

            String path = u.getPath();
            if (path.length() == 0) {
                path = "/";
            }

            String firstLine = "GET " + path + " HTTP/1.1\r\n";

            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                String headerLine = header + ": " + requestHeaders.get(header) + "\r\n";
                httpRequestString.append(headerLine);
            }

            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
            //在请求最后有两个/r/n/r/n
            //构建request写入
            writer.println(httpRequestString);

            // 接受response
            InputStream is = client.getInputStream();
            result = readBytes(is);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                result = e.toString().getBytes("utf-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    public static byte[] readBytes(InputStream is) throws IOException {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
                int length = is.read(buffer);

                //没有读到数据 结束
                if (-1 == length) {
                    break;
                }

                baos.write(buffer, 0, length);
                // 读到最后的数据 结束
                if (length != bufferSize) {
                    break;
                }
        }
        byte[] result = baos.toByteArray();
        return result;
    }
}
