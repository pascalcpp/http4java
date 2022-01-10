package com.xpcf.http4java.http;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.Bootstrap;
import com.xpcf.http4java.catalina.*;
import com.xpcf.http4java.util.MiniBrowser;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 11:03 AM
 */
public class Request extends BaseRequest {

    private String requestString;

    private String uri;

    private Socket socket;

    private Context context;

    private Connector connector;

    private String method;

    private String queryString;

    private Map<String, String[]> parameterMap;

    private Map<String, String> headerMap;

    private Cookie[] cookies;

    private HttpSession session;



    public Request(Socket socket, Connector connector) throws IOException {

        this.socket = socket;
        this.connector = connector;
        this.parameterMap = new HashMap<>();
        this.headerMap = new HashMap<>();

        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) {
//            LogFactory.get().info("requestString: " +(requestString == null ? "null" : "empty") );
            return;
        }
        parseUri();
        parseContext();
        parseMethod();
        if (!"/".equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
            if (StrUtil.isEmpty(uri)) {
                uri = "/";
            }
        }

        parseParameters();
        parseHeaders();
        parseCookies();
    }


    /**
     * 根据uri 解析context
     */
    private void parseContext() {
        Service service = connector.getService();
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

    private void parseParameters() {

        if ("GET".equals(getMethod())) {
            String uri = StrUtil.subBetween(requestString, " ", " ");
            if (StrUtil.contains(uri, '?')) {
                queryString = StrUtil.subAfter(uri, '?', false);
            }
        }

        if ("POST".equals(getMethod())) {
            queryString = StrUtil.subAfter(requestString, "\r\n\r\n", false);
        }

        if (null == queryString) {
            return;
        }

        queryString = URLUtil.decode(queryString);
        String[] parameterValues = queryString.split("&");

        if (null != parameterValues) {
            for (String parameterValue : parameterValues) {
                String[] nameWithValue = parameterValue.split("=");
                String name = nameWithValue[0];
                String value = nameWithValue[1];

                String[] values = parameterMap.get(name);
                if (null == values) {
                    values = new String[]{value};
                    parameterMap.put(name, values);
                } else {
                    values = ArrayUtil.append(values, value);
                    parameterMap.put(name, values);
                }
            }
        }
    }

    public void parseHeaders() {
        StringReader stringReader = new StringReader(requestString);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(stringReader, lines);
        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i);
            if (0 == line.length()) {
                break;
            }
            String[] segs = line.split(": ");
//            System.out.println(line);
            String headerName = segs[0].toLowerCase();
            String headerValue = segs[1];
            headerMap.put(headerName, headerValue);
        }
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public String getJSessionIdFromCookie() {
        if (null == cookies) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    private void parseCookies() {
        List<Cookie> cookieList = new ArrayList<>();
        String cookies = headerMap.get("cookie");

        if (null != cookies) {
            String[] pairs = StrUtil.split(cookies, ";");
            for (String pair : pairs) {
                if (StrUtil.isBlank(pair)) {
                    continue;
                }

                String[] segs = StrUtil.split(pair, "=");
                String name = segs[0].trim();
                String value = segs[1].trim();
                Cookie cookie = new Cookie(name, value);
                cookieList.add(cookie);
            }
        }

        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
    }

    public Connector getConnector() {
        return connector;
    }

    @Override
    public String getHeader(String name) {
        if (null == name) {
            return null;
        }
        name = name.toLowerCase();
        return headerMap.get(name);
    }
    @Override
    public Enumeration getHeaderNames() {
        Set<String> keys = headerMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public int getIntHeader(String name) {
        String value = headerMap.get(name);
        return Convert.toInt(value, 0);
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        if (null != values && values.length != 0) {
            return values[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public String getMethod() {
        return method;
    }

    private void parseMethod() {
        method = StrUtil.subBefore(requestString, " ", false);
    }


    @Override
    public String getRealPath(String path) {
        return context.getServletContext().getRealPath(path);
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    @Override
    public String getProtocol() {
        return "HTTP:/1.1";
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        String remoteAddr = isa.getAddress().toString();
        return StrUtil.subAfter(remoteAddr, "/", false);
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        return isa.getHostName();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("host").trim();
    }

    @Override
    public int getRemotePort() {
        return socket.getPort();
    }

    @Override
    public int getServerPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getContextPath() {
        String path = context.getPath();
        if ("/".equals(path)) {
            return "";
        }
        return path;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            port = 80;
        }
        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if (("http".equals(scheme) && (port != 80)) || ("https".equals(scheme) && (port != 443))) {
            url.append(":");
            url.append(port);
        }
        url.append(getUri());
        return url;
    }

    private void parseHttpRequest() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream, false);
        requestString = new String(bytes, "utf-8");
        LogFactory.get().info(requestString);

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

