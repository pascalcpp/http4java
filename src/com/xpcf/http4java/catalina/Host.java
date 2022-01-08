package com.xpcf.http4java.catalina;

import cn.hutool.log.LogFactory;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.ServerXMLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/2/2022 11:58 AM
 */
public class Host {

    private String name;

    private Map<String, Context> contextMap;

    private Engine engine;

    public Host(String name, Engine engine) {

        this.contextMap = new HashMap<>();
        this.name = name;
        this.engine = engine;

        scanContextsOnWebAppsFolder();
        scanContextsInServerXML();
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }


    private void scanContextsOnWebAppsFolder() {
        File[] folders = Constant.webappsFolder.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory()) {
                continue;
            }
            loadContext(folder);
        }
    }

    private void scanContextsInServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts(this);
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    private void loadContext(File folder) {
        String path = folder.getName();

        if ("ROOT".equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, true);
        contextMap.put(context.getPath(), context);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void reload(Context context) {
        LogFactory.get().info("Reloading Context with name [{}] has start", context.getPath());
        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.isReloadable();

        context.stop();
        contextMap.remove(context);

        Context newContext = new Context(path, docBase, this, reloadable);
        contextMap.put(newContext.getPath(), newContext);
        LogFactory.get().info("Reloading Context with name [{}] has completed", context.getPath());

    }
}
