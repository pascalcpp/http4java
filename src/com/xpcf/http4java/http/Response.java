package com.xpcf.http4java.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

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

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public Response() {
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        this.contentType = "text/html";
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
