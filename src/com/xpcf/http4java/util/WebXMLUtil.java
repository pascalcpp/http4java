package com.xpcf.http4java.util;

import cn.hutool.core.io.FileUtil;
import com.xpcf.http4java.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/2/2022 4:37 PM
 */
public class WebXMLUtil {

    private static Map<String, String> mimeTyeMapping = new HashMap<>();

    /**
     * @param extName
     * @return
     */
    public static String getMimeType(String extName) {


        // double check lock
        if (mimeTyeMapping.isEmpty()) {
            synchronized (WebXMLUtil.class) {
                if (mimeTyeMapping.isEmpty()) {
                    initMimeType();
                }
            }
        }

        String mimeType = mimeTyeMapping.get(extName);
        if (null == mimeType) {
            return "text/html";
        }

        return mimeType;
    }

    private static void initMimeType() {
        String xml = FileUtil.readUtf8String(Constant.WebXmlFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("mime-mapping");

        for (Element e : es) {
            String extName = e.select("extension").first().text();
            String mimeType = e.select("mime-type").first().text();
            mimeTyeMapping.put(extName, mimeType);
        }
    }

    public static String getWelComeFile(Context context) {
        String xml = FileUtil.readUtf8String(Constant.WebXmlFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("welcome-file");
        for (Element e : es) {
            String welcomeFileName = e.text();
            File f = new File(context.getDocBase(), welcomeFileName);
            if (f.exists()) {
                return f.getName();
            }
        }
        return "index.html";
    }
}
