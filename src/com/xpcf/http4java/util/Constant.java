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


    /**
     * webapps 目录
     */
    public static final File webappsFolder = new File(SystemUtil.get("user.dir"), "webapps");


    public static final File rootFolder = new File(webappsFolder, "ROOT");

    public static final File confFolder = new File(SystemUtil.get("user.dir"), "conf");

    public static final File serverXmlFile = new File(confFolder, "server.xml");

}
