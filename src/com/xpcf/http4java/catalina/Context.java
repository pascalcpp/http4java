package com.xpcf.http4java.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.watcher.ContextFileChangeWatcher;
import com.xpcf.http4java.classloader.WebappClassLoader;
import com.xpcf.http4java.exception.WebConfigDuplicatedException;
import com.xpcf.http4java.http.ApplicationContext;
import com.xpcf.http4java.util.ContextXMLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

/**
 * web application 上下文
 *
 * @author XPCF
 * @version 1.0
 * @date 1/1/2022 10:07 PM
 */
public class Context {

    private String path;

    private String docBase;

    private File contextWebXmlFile;

    private WebappClassLoader webappClassLoader;

    private Host host;

    private boolean reloadable;

    private ContextFileChangeWatcher contextFileChangeWatcher;

    private Map<String, String> urlToServletClassName;

    private Map<String, String> urlToServletName;

    private Map<String, String> servletNameToClassName;

    private Map<String, String> classNameToServletName;

    private ServletContext servletContext;

    private Map<Class<?>, HttpServlet> servletPool;

    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();

        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.host = host;
        this.reloadable = reloadable;
        this.servletContext = new ApplicationContext(this);
        this.servletPool = new HashMap<>();

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();

        // reload 时重新生成classloader
        webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        this.urlToServletName = new HashMap<>();
        this.urlToServletClassName = new HashMap<>();
        this.servletNameToClassName = new HashMap<>();
        this.classNameToServletName = new HashMap<>();

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        deploy();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());

    }

    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
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

    /**
     * 考虑double check lock
     * @param clazz
     * @return
     */
    public synchronized HttpServlet getServlet(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    private void deploy() {

        init();

        if (reloadable) {
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
        }
    }

    public void stop() {
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();
    }

    public void reload() {
        host.reload(this);
    }
}
