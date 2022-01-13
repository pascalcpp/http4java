package com.xpcf.http4java.watcher;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.catalina.Host;
import com.xpcf.http4java.util.Constant;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/14/2022 12:40 AM
 */
public class WarFileWatcher {
    private WatchMonitor monitor;

    public WarFileWatcher(Host host) {
        WatchMonitor watchMonitor = new WatchMonitor(Paths.get(Constant.webappsFolder.getAbsolutePath()), 1, WatchMonitor.EVENTS_ALL);
        watchMonitor.setWatcher(new Watcher() {
            private void dealWith(WatchEvent<?> event) {
                String fileName = event.context().toString();
                if (fileName.toLowerCase().endsWith(".war") && WatchMonitor.ENTRY_CREATE.equals(event.kind())) {
                    File warFile = FileUtil.file(Constant.webappsFolder, fileName);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    host.loadWar(warFile);
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
