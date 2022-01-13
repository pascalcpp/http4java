package com.xpcf.http4java.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.ServerXMLUtil;
import com.xpcf.http4java.watcher.WarFileWatcher;

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
        scanWarOnWebAppsFolder();

        new WarFileWatcher(this).start();
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }

    public void load(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, false);
        contextMap.put(context.getPath(), context);
    }

    public void loadWar(File warFile) {
        String fileName = warFile.getName();
        String folderName = StrUtil.subBefore(fileName, ".", true);

        Context context = contextMap.get("/" + folderName);
        if (null != context) {
            return;
        }

        File folder = new File(Constant.webappsFolder, folderName);
        if (folder.exists()) {
            return;
        }

        // 移动war文件， jar命令只支持解压到当前目录
        File tempWarFile = FileUtil.file(Constant.webappsFolder, folderName, fileName);
        File contextFolder = tempWarFile.getParentFile();
        contextFolder.mkdir();
        FileUtil.copyFile(warFile, tempWarFile);

        // 解压 注意要加空格
        String command = "jar xvf " + fileName;
        Process p = RuntimeUtil.exec(null, contextFolder, command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempWarFile.delete();
        load(contextFolder);
    }

    private void scanWarOnWebAppsFolder() {
        File folder = FileUtil.file(Constant.webappsFolder);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (!file.getName().toLowerCase().endsWith(".war")) {
                continue;
            }
            loadWar(file);
        }
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
