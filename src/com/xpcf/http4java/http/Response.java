package com.xpcf.http4java.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/31/2021 10:32 PM
 */
public class Response {

    private StringWriter stringWriter;

    private PrintWriter writer;

    private String contentType;


    public Response() {
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        this.contentType = "text/html";
    }

    public String getContentType() {
        return contentType;
    }

    public PrintWriter getWriter() {
        return writer;
    }


    public byte[] getBody() throws UnsupportedEncodingException {
        String content = stringWriter.toString();
        byte[] bytes = content.getBytes("UTF-8");
        return bytes;
    }

}
