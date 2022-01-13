package com.xpcf.http4java.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.cookie.ThreadLocalCookieStore;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 9:37 AM
 */
public class TestServer {
    private static int port = 18080;
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
//            System.out.println(CookieHandler.getDefault());
        }
    }

    @Test
    public void testServerJumpWithAttributes(){
        String http_servlet = getHttpString("/javaweb/jump2");
        System.out.println(http_servlet);
        containAssert(http_servlet,"hello the name is gareen");
    }



    @Test
    public void testGzip() {
        byte[] gzipContent = getContentBytes("/",true);
        byte[] unGzipContent = ZipUtil.unGzip(gzipContent);
        String html = new String(unGzipContent);
        Assert.assertEquals(html, "root index.html@@zxzcxzczxczxcsxcaasdasdasdasdasdasdasd<><>/Mm.['h.'.'b");
    }


    @Test
    public void testSession() throws IOException {
        System.out.println(Thread.currentThread().getName());
        String jsessionid = getContentString("/javaweb/setSession");
        if(null!=jsessionid)
            jsessionid = jsessionid.trim();
        System.out.println(jsessionid);
        String url = StrUtil.format("http://{}:{}{}", ip,port,"/javaweb/getSession");
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie","JSESSIONID="+jsessionid);
        conn.connect();


        InputStream is = conn.getInputStream();
        String html = IoUtil.read(is, "utf-8");
        System.out.println(html);
        containAssert(html,"Gareen(session)");
    }

    @Test
    public void testgetCookie() throws IOException {

        System.out.println(Thread.currentThread().getName());
        String url = StrUtil.format("http://{}:{}{}", ip,port,"/javaweb/getCookie");
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie","name=Gareen(cookie)");
        conn.connect();
        InputStream is = conn.getInputStream();
        String html = IoUtil.read(is, "utf-8");
        conn.disconnect();
        containAssert(html,"name: Gareen(cookie)");
    }

    @Test
    public void testsetCookie() {
        String html = getHttpString("/javaweb/setCookie");
        containAssert(html,"Set-Cookie: name=Gareen(cookie); Expires=");
    }

    private byte[] getContentBytes(String uri) {
        return getContentBytes(uri,false);
    }
    private byte[] getContentBytes(String uri,boolean gzip) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        return MiniBrowser.getContentBytes(url,gzip);
    }


    @Test
    public void testheader() {
        String html = getContentString("/javaweb/header");
//        System.out.println(html);
        Assert.assertEquals(html,"simple browser / java1.8");
    }
    @Test
    public void testgetParam() {
        String uri = "/javaweb/param";
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        Map<String,Object> params = new HashMap<>();
        params.put("name","eko");
        String html = MiniBrowser.getContentString(url, params, true);
        Assert.assertEquals(html,"get name: eko");
    }

    @Test
    public void testJavaweb0Hello() {
        String html = getContentString("/javaweb0/hello");
//        System.out.println(html);
        containAssert(html,"hello@javaweb");
    }

    @Test
    public void testJavaweb1Hello() {
        String html = getContentString("/javaweb1/hello");
//        System.out.println(html);
        containAssert(html,"hello@javaweb");
    }

    @Test
    public void testClientJump(){
        String http_servlet = getHttpString("/javaweb/jump1");
        containAssert(http_servlet,"HTTP/1.1 302 Found");
        String http_jsp = getHttpString("/javaweb/jump1.jsp");
        containAssert(http_jsp,"HTTP/1.1 302 Found");
    }

    @Test
    public void testServerJump(){
        String http_servlet = getHttpString("/javaweb/jump2");
        containAssert(http_servlet,"hello the name is");
    }


    @Test
    public void testpostParam() {
        String uri = "/javaweb/param";
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        Map<String,Object> params = new HashMap<>();
        params.put("name","eko");
        String html = MiniBrowser.getContentString(url, params, false);
        Assert.assertEquals(html,"post name: eko");
    }


    @Test
    public void testaTxt() {
        String response  = getHttpString("/a.txt");
        containAssert(response, "Content-Type: text/plain");
    }
//    @Test
//    public void testPDF() {
//        String uri = "/etf.pdf";
//        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
////        /** Cookie管理 */
////        protected static CookieManager cookieManager;
////        static {
////            cookieManager = new CookieManager(new ThreadLocalCookieStore(), CookiePolicy.ACCEPT_ALL);
////            CookieHandler.setDefault(cookieManager);
////        }
//        HttpUtil.download(url, baos, true);
//        int pdfFileLength = 3590775;
//        Assert.assertEquals(pdfFileLength, baos.toByteArray().length);
//        CookieHandler.setDefault(null);
//    }

    @Test
    public void testPDF() {
        byte[] bytes = getContentBytes("/etf.pdf");
        int pdfFileLength = 3590775;
        Assert.assertEquals(pdfFileLength, bytes.length);
    }


    @Test
    public void testJsp() {
        String html = getContentString("/javaweb/a.jsp");
        Assert.assertEquals("hello", html);
    }


    @Test
    public void testaIndex() {
        String html = getContentString("/a");
        Assert.assertEquals(html,"a.index");
    }
    @Test
    public void testbIndex() {
        String html = getContentString("/b/");
        Assert.assertEquals(html,"hello from index.html@b");
    }

    @Test
    public void testJavawebHelloSingleton() {
        String html1 = getContentString("/javaweb/hello");
        String html2 = getContentString("/javaweb/hello");
        Assert.assertEquals(html1,html2);
    }

    @Test
    public void testhello2() {
        String html = getContentString("/j2ee/hello");
        Assert.assertEquals(html,"hello");
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
        Assert.assertTrue(duration < 3000);

    }

    @Test
    public void test404() {
        String response = getHttpString("/notfoudn");
        System.out.println(response);
        containAssert(response, "HTTP/1.1 404 Not Found");
    }
    @Test
    public void test500() {
        String response  = getHttpString("/500.html");
        containAssert(response, "HTTP/1.1 500 Internal Server Error");
    }


    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }
    private String getHttpString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        String http = MiniBrowser.getHttpString(url);
        return http;
    }

    private void containAssert(String html, String string) {
        boolean match = StrUtil.containsAny(html, string);
        Assert.assertTrue(match);
    }
}
