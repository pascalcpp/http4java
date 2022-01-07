package com.xpcf.http4java.watcher;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.catalina.Context;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 7:24 PM
 */
public class ContextFileChangeWatcher {

    private WatchMonitor monitor;

    private boolean stop = false;

    public ContextFileChangeWatcher(Context context) {

        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, new Watcher() {
            private void dealWith(WatchEvent<?> event, Path currentPath) {
                // 可能不需要加锁，监听只有一个线程
                synchronized (ContextFileChangeWatcher.class) {


//                    String fileName = event.context().toString();
                    String fileName = "";
                    System.err.println(Thread.currentThread().getName() + " ");

                    System.err.println(currentPath.toString());
                    System.err.println("process method" + event.kind().name());
                    if (stop) {
//                        System.err.println(Thread.currentThread().getName() + " ");
//                        System.err.println("stop method");
                        return;
                    }

                    if (fileName.endsWith(".jar") || fileName.endsWith(".xml") || fileName.endsWith(".class")) {
//                        System.err.println(Thread.currentThread().getName() + " ");
//                        System.err.println("process method");
                        stop = true;
                        LogFactory.get().info(ContextFileChangeWatcher.this + " file changes under the Web application were detected {}", fileName);
                        context.reload();
                    }
                }
            }

            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }
        });

        this.monitor.setDaemon(true);
    }


    public void start() {
        monitor.start();
    }

    public void stop() {
        monitor.close();
    }
}

