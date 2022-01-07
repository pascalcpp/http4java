package com.xpcf.http4java.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/5/2022 11:52 PM
 */
public class ContextXMLUtil {

    public static String getWatchedResource() {
        try {
            String xml = FileUtil.readUtf8String(Constant.contextXmlFile);
            Document d = Jsoup.parse(xml);
            Element e = d.select("WatchedResource").first();
            return e.text();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "WEB-INF/web.xml";
        }
    }

}
