package com.xpcf.http4java.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testHtml() {
        String html = getContentString("/a.html");
        Assert.assertEquals(html, "Hello server from a.html");
    }

    @Test
    public void testTimeConsumeHtml() throws InterruptedException {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10));
        TimeInterval timeInterval = DateUtil.timer();

        for (int i = 0; i < 3; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    getContentString("/timeConsume.html");
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        long duration = timeInterval.intervalMs();
        System.out.println(duration);
        Assert.assertTrue(duration < 3000);

    }

    @Test
    public void testaIndex() {
        String html = getContentString("/a/index.html");
        System.out.println(html);
        Assert.assertEquals(html,"a.index");
    }

    @Test
    public void testbIndex() {
        String html = getContentString("/b/index.html");
        System.out.println(html);
        Assert.assertEquals(html,"hello from index.html@b");
    }

    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

}
