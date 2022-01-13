package com.xpcf.http4java.watcher;

import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.catalina.Context;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 7:24 PM
 */


public class ContextFileChangeWatcher {

    private WatchMonitor monitor;

    private boolean stop = false;

    public ContextFileChangeWatcher(Context context) {

        WatchMonitor watchMonitor = new WatchMonitor(Paths.get(context.getDocBase()), Integer.MAX_VALUE, WatchMonitor.EVENTS_ALL);
        watchMonitor.setWatcher(new Watcher() {
            private void dealWith(WatchEvent<?> event) {

                String fileName = event.context().toString();
                if (stop) {
                    return;
                }

                if (fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")) {
                    stop = true;
                    LogFactory.get().info(ContextFileChangeWatcher.this + " Important file changes under the Web application were detected {} ", fileName);
                    context.reload();
                }
            }

            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event);

            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event);

            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

        });

        this.monitor = watchMonitor;
        this.monitor.setDaemon(true);
    }

    public void start() {
        monitor.start();
    }

    public void stop() {
        monitor.close();
    }
}


