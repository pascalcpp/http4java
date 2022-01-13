package com.xpcf.http4java.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.http.StandardFilterConfig;
import com.xpcf.http4java.http.StandardServletConfig;
import com.xpcf.http4java.watcher.ContextFileChangeWatcher;
import com.xpcf.http4java.classloader.WebappClassLoader;
import com.xpcf.http4java.exception.WebConfigDuplicatedException;
import com.xpcf.http4java.http.ApplicationContext;
import com.xpcf.http4java.util.ContextXMLUtil;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
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

    private Map<String, List<String>> urlToFilterClassNames;

    private Map<String, List<String>> urlToFilterNames;

    private Map<String, String> filterNameToClassName;

    private Map<String, String> classNameToFilterName;

    private Map<String, Map<String, String>> filterClassNameToInitParams;

    private ServletContext servletContext;

    private Map<Class<?>, HttpServlet> servletPool;

    private Map<String, Filter> filterPool;

    private List<ServletContextListener> listeners;

    private Map<String, Map<String, String>> servletClassNameInitParams;

    private List<String> loadOnStartupServletClassNames;

    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();

        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.host = host;
        this.reloadable = reloadable;
        this.listeners = new ArrayList<>();
        this.servletContext = new ApplicationContext(this);
        this.servletPool = new HashMap<>();
        this.filterPool = new HashMap<>();
        this.servletClassNameInitParams = new HashMap<>();
        this.loadOnStartupServletClassNames = new ArrayList<>();
        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();

        // reload 时重新生成classloader
        webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        this.urlToServletName = new HashMap<>();
        this.urlToServletClassName = new HashMap<>();
        this.servletNameToClassName = new HashMap<>();
        this.classNameToServletName = new HashMap<>();

        this.urlToFilterClassNames = new HashMap<>();
        this.urlToFilterNames = new HashMap<>();
        this.filterNameToClassName = new HashMap<>();
        this.classNameToFilterName = new HashMap<>();
        this.filterClassNameToInitParams = new HashMap<>();

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        deploy();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());

    }

    public List<Filter> getMatchedFilters(String uri) {
        List<Filter> filterList = new ArrayList<>();
        Set<String> patterns = urlToFilterClassNames.keySet();
        Set<String> matchedPatterns = new LinkedHashSet<>();

        for (String pattern : patterns) {
            if (match(pattern, uri)) {
                matchedPatterns.add(pattern);
            }
        }

        Set<String> matchedFilterClassNames = new LinkedHashSet<>();
        for (String matchedPattern : matchedPatterns) {
            List<String> filterClassNames = urlToFilterClassNames.get(matchedPattern);
            matchedFilterClassNames.addAll(filterClassNames);
        }

        for (String matchedFilterClassName : matchedFilterClassNames) {
            Filter filter = filterPool.get(matchedFilterClassName);
            filterList.add(filter);
        }

        return filterList;
    }

    public void parseLoadOnStartup(Document d) {
        Elements es = d.select("load-on-startup");
        for (Element e : es) {
            String loadOnStartupServletClassName = e.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadOnStartupServletClassName);
        }
    }

    public void parseFilterMapping(Document d) {

        Elements mappingUrlElements = d.select("filter-mapping url-pattern");
        for (Element mappingUrlElement : mappingUrlElements) {
            String urlPattern = mappingUrlElement.text();
            String filterName = mappingUrlElement.parent().select("filter-name").first().text();

            List<String> filterNames = urlToFilterNames.get(urlPattern);
            if (null == filterNames) {
                filterNames = new ArrayList<>();
                urlToFilterNames.put(urlPattern, filterNames);
            }
            filterNames.add(filterName);
        }


        Elements filterNameElements = d.select("filter filter-name");
        for (Element filterNameElement : filterNameElements) {
            String filterName = filterNameElement.text();
            String filterClass = filterNameElement.parent().select("filter-class").text();
            filterNameToClassName.put(filterName, filterClass);
            classNameToFilterName.put(filterClass, filterName);
        }

        Set<String> urls = urlToFilterNames.keySet();
        for (String url : urls) {
            List<String> filterNames = urlToFilterNames.get(url);
            if (null == filterNames) {
                filterNames = new ArrayList<>();
                urlToFilterNames.put(url, filterNames);
            }

            for (String filterName : filterNames) {
                String filterClassName = filterNameToClassName.get(filterName);
                List<String> filterClassNames = urlToFilterClassNames.get(url);
                if (null == filterClassNames) {
                    filterClassNames = new ArrayList<>();
                    urlToFilterClassNames.put(url, filterClassNames);
                }
                filterClassNames.add(filterClassName);
            }
        }

    }

    private void parseFilterInitParams(Document d) {
        Elements filterClassNameElements = d.select("filter-class");
        for (Element filterClassNameElement : filterClassNameElements) {
            String filterClassName = filterClassNameElement.text();

            Elements initElements = filterClassNameElement.parent().select("init-param");
            if (initElements.isEmpty()) {
                continue;
            }

            Map<String, String> initParams = new HashMap<>();
            for (Element initElement : initElements) {
                String name = initElement.select("param-name").get(0).text();
                String value = initElement.select("param-value").get(0).text();
                initParams.put(name, value);
            }

            filterClassNameToInitParams.put(filterClassName, initParams);
        }
    }

    public void handleLoadOnStartup() {
        for (String loadOnStartupServletClassName : loadOnStartupServletClassNames) {
            try {
                Class<?> clazz = webappClassLoader.loadClass(loadOnStartupServletClassName);
                getServlet(clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    public ServletContext getServletContext() {
        return servletContext;
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

    public void addListener(ServletContextListener listener) {
        listeners.add(listener);
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    private void loadListeners() {
        try {
            if (!contextWebXmlFile.exists()) {
                return;
            }
            String xml = FileUtil.readUtf8String(contextWebXmlFile);
            Document d = Jsoup.parse(xml);
            Elements es = d.select("listener listener-class");

            for (Element e : es) {
                String listenerClassName = e.text();
                Class<?> clazz = this.getWebappClassLoader().loadClass(listenerClassName);
                ServletContextListener listener = (ServletContextListener) clazz.newInstance();
                addListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fireEvent(String type) {
        ServletContextEvent event = new ServletContextEvent(servletContext);
        for (ServletContextListener listener : listeners) {
            if ("init".equals(type)) {
                listener.contextInitialized(event);
            }

            if ("destroy".equals(type)) {
                listener.contextDestroyed(event);
            }
        }
    }

    private void parseServletInitParams(Document d) {
        Elements servletClassNameElements = d.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initElements = servletClassNameElement.parent().select("init-param");
            if (initElements.isEmpty()) {
                continue;
            }

            Map<String, String> initParams = new HashMap<>();
            for (Element initElement : initElements) {
                String name = initElement.select("param-name").get(0).text();
                String value = initElement.select("param-value").get(0).text();
                initParams.put(name, value);
            }

            servletClassNameInitParams.put(servletClassName, initParams);
        }
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
        parseFilterMapping(d);

        parseServletInitParams(d);
        parseFilterInitParams(d);

        initFilter();

        parseLoadOnStartup(d);
        handleLoadOnStartup();

        fireEvent("init");
    }

    private void initFilter() {
        Set<String> classNames = classNameToFilterName.keySet();
        for (String className : classNames) {
            try {
                Class<?> clazz = getWebappClassLoader().loadClass(className);
                Map<String, String> initParameters = filterClassNameToInitParams.get(className);
                String filterName = classNameToFilterName.get(className);

                FilterConfig filterConfig = new StandardFilterConfig(servletContext, initParameters, filterName);
                Filter filter = filterPool.get(clazz);

                if (null == filter) {
                    filter = (Filter) ReflectUtil.newInstance(clazz);
                    filter.init(filterConfig);
                    filterPool.put(className, filter);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void destroyServlets() {
        Collection<HttpServlet> servlets = servletPool.values();
        for (HttpServlet servlet : servlets) {
            servlet.destroy();
        }
    }

    private boolean match(String pattern, String uri) {

        // 完全匹配
        if (StrUtil.equals(pattern, uri)) {
            return true;
        }

        // /* 匹配
        if (StrUtil.equals(pattern, "/*")) {
            return true;
        }

        // 后缀名匹配 /*.jsp
        if (StrUtil.startWith(pattern, "/*.")) {
            String patternExtName = StrUtil.subAfter(pattern, '.', false);
            String uriExtName = StrUtil.subAfter(uri, '.', false);
            if (StrUtil.equals(patternExtName, uriExtName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 考虑double check lock
     *
     * @param clazz
     * @return
     */
    public synchronized HttpServlet getServlet(Class<?> clazz) throws IllegalAccessException, InstantiationException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {

            servlet = (HttpServlet) clazz.newInstance();
            ServletContext servletContext = getServletContext();
            String className = clazz.getName();
            String servletName = classNameToServletName.get(className);
            Map<String, String> initParameters = servletClassNameInitParams.get(className);
            ServletConfig servletConfig = new StandardServletConfig(servletContext, initParameters, servletName);
            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);


        }
        return servlet;
    }

    private void deploy() {
        loadListeners();

        init();

        if (reloadable) {
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
        }

        JspC jspC = new JspC();
        new JspRuntimeContext(servletContext, jspC);
    }

    public void stop() {
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();
        destroyServlets();

        fireEvent("destroy");
    }

    public void reload() {
        // before reload wait OS change file

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        host.reload(this);
    }
}
