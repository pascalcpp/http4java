package com.xpcf.http4java.test;

import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 9:37 AM
 */
public class TestServer {
    private static int port = 4396;
    private static String ip = "127.0.0.1";


    /**
     * 检测server是否启动
     */
    @BeforeClass
    public static void beforeClass() {
        if (NetUtil.isUsableLocalPort(port)) {
            Logger.printError("please start server at port: " + port);
            System.exit(1);
        } else {
            Logger.println("start test");
        }
    }

    @Test
    public void testServer() {
        String html = getContentString("/");
        Assert.assertEquals(html, "hello world");
    }

    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

}
