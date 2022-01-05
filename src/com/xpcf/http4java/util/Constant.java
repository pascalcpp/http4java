package com.xpcf.http4java.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/31/2021 10:29 PM
 */
public class Constant {


    public static final String responseHead202 = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: {}\r\n\r\n";


    public static final String responseHead404 = "HTTP/1.1 404 Not Found\r\n" +
            "Content-Type: text/html\r\n\r\n";


    public static final String responseHead500 = "HTTP/1.1 500 Internal Server Error\r\n"
            + "Content-Type: text/html\r\n\r\n";

    public static final String textFormat500 = "<html><head><title>HTTP4JAVA/1.0.0 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> "
            + "</head><body><h1>HTTP Status 500 - An exception occurred processing {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Exception report</p><p><b>message</b> <u>An exception occurred processing {}</u></p><p><b>description</b> "
            + "<u>The server encountered an internal error that prevented it from fulfilling this request.</u></p>"
            + "<p>Stacktrace:</p>" + "<pre>{}</pre>" + "<HR size='1' noshade='noshade'><h3>HTTP4JAVA 1.0.0</h3>"
            + "</body></html>";

    public static final String textFormat404 =
            "<html><head><title>HTTP4JAVA/1.0.0 - Error report</title><style>" +
                    "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
                    "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
                    "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
                    "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
                    "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
                    "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
                    "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> " +
                    "</head><body><h1>HTTP Status 404 - {}</h1>" +
                    "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> " +
                    "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>HTTP4JAVA 1.0.0</h3>" +
                    "</body></html>";

    /**
     * webapps 目录
     */
    public static final File webappsFolder = new File(SystemUtil.get("user.dir"), "webapps");

    public static final File rootFolder = new File(webappsFolder, "ROOT");

    public static final File confFolder = new File(SystemUtil.get("user.dir"), "conf");

    public static final File serverXmlFile = new File(confFolder, "server.xml");

    public static final File WebXmlFile = new File(confFolder, "web.xml");

    public static final File contextXmlFile = new File(confFolder, "context.xml");

}
