package com.xpcf.http4java.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/31/2021 10:32 PM
 */
public class Response extends BaseResponse {

    private StringWriter stringWriter;

    private PrintWriter writer;

    private String contentType;

    private byte[] body;

    private int status;

    private List<Cookie> cookies;


    public Response() {
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        this.contentType = "text/html";
        this.cookies = new ArrayList<>();
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public byte[] getBody() throws UnsupportedEncodingException {
        if (null == body) {
            String content = stringWriter.toString();
            body = content.getBytes("UTF-8");
        }
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }


    public String getCookiesHeader() {
        if (null == cookies) {
            return "";
        }

        String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuffer sb = new StringBuffer();

        for (Cookie cookie : getCookies()) {
            sb.append("\r\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
            if (-1 != cookie.getMaxAge()) {
                sb.append("Expires=");
                Date now = new Date();
                DateTime expire = DateUtil.offset(now, DateField.SECOND, cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append("; ");
            }

            if (null != cookie.getPath()) {
                sb.append("Path=" + cookie.getPath());
            }
        }
        return sb.toString();
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }


    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }
}
