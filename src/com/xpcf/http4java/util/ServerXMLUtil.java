package com.xpcf.http4java.util;

import cn.hutool.core.io.FileUtil;
import com.xpcf.http4java.catalina.Context;
import com.xpcf.http4java.catalina.Engine;
import com.xpcf.http4java.catalina.Host;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/1/2022 11:40 PM
 */
public class ServerXMLUtil {

    public static List<Context> getContexts() {
        List<Context> result = new ArrayList<>();;
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);

        Elements es = d.select("Context");
        for (Element e : es) {
            String path = e.attr("path");
            String docBase = e.attr("docBase");
            Context context = new Context(path, docBase);
            result.add(context);
        }

        return result;
    }


    public static String getServiceName() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);
        Element service = d.select("Service").first();
        return service.attr("name");
    }

    public static String getEngineDefaultHost() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);
        Element host = d.select("Engine").first();
        return host.attr("defaultHost");
    }

    public static List<Host> getHosts(Engine engine) {
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("Host");

        for (Element e : es) {
            String name = e.attr("name");
            Host host = new Host(name, engine);
            result.add(host);
        }

        return result;
    }

}
