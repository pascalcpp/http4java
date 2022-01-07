package com.xpcf.http4java.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.classloader.WebappClassLoader;
import com.xpcf.http4java.exception.WebConfigDuplicatedException;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.ContextXMLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.*;

/**
 *
 * web application 上下文
 * @author XPCF
 * @version 1.0
 * @date 1/1/2022 10:07 PM
 */
public class Context {

    private String path;

    private String docBase;

    private File contextWebXmlFile;

    private WebappClassLoader webappClassLoader;

    private Map<String, String> urlToServletClassName;

    private Map<String, String> urlToServletName;

    private Map<String, String> servletNameToClassName;

    private Map<String, String> classNameToServletName;


    public Context(String path, String docBase) {
        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        this.urlToServletName = new HashMap<>();
        this.urlToServletClassName = new HashMap<>();
        this.servletNameToClassName = new HashMap<>();
        this.classNameToServletName = new HashMap<>();

        deploy();
    }
    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }


    public String getServletClassName(String uri) {
        return urlToServletClassName.get(uri);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    private void parseServletMapping(Document d) {
        Elements mappingUrlElements = d.select("servlet-mapping url-pattern");

        for (Element mappingUrlElement : mappingUrlElements) {
            String urlPattern = mappingUrlElement.text();
            String servletName = mappingUrlElement.parent().select("servlet-name").first().text();
            urlToServletName.put(urlPattern, servletName);
        }

        Elements servletNameElements = d.select("servlet servlet-name");
        for (Element servletNameElement : servletNameElements) {
            String servletName = servletNameElement.text();
            String servletClass = servletNameElement.parent().select("servlet-class").first().text();
            servletNameToClassName.put(servletName, servletClass);
            classNameToServletName.put(servletClass, servletName);
        }

        Set<String> urls = urlToServletName.keySet();
        for (String url : urls) {
            String servletName = urlToServletName.get(url);
            String servletClassName = servletNameToClassName.get(servletName);
            urlToServletClassName.put(url, servletClassName);
        }

    }

    private void checkDuplicated(Document d, String mapping, String desc) throws WebConfigDuplicatedException {
        Elements elements = d.select(mapping);

        List<String> contents = new ArrayList<>();
        for (Element element : elements) {
            contents.add(element.text());
        }


        Collections.sort(contents);
        for (int i = 0; i < contents.size() - 1; i++) {
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);
            if (contentPre.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }
    }

    private void checkDuplicated() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        checkDuplicated(d, "servlet-mapping url-pattern", "servlet url 重复，请保持其唯一性:{}");
        checkDuplicated(d, "servlet servlet-name", "servlet name 重复，请保持其唯一性:{}");
        checkDuplicated(d, "servlet servlet-class", "servlet class 重复，请保持其唯一性:{}");
    }

    private void init() {

        if (!contextWebXmlFile.exists()) {
            return;
        }

        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            return;
        }

        // 完成重复性检验
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        parseServletMapping(d);
    }

    private void deploy() {
        TimeInterval timeInterval = DateUtil.timer();
        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        init();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());
    }
}
