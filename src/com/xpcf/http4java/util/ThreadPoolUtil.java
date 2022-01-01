package com.xpcf.http4java.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 *
 * thread pool 的封装
 * @author XPCF
 * @version 1.0
 * @date 1/1/2022 9:36 PM
 */
public class ThreadPoolUtil {
    TimeUnit unit;
    BlockingQueue workQueue;
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20,
            100, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    public static void run(Runnable r) {
        threadPool.execute(r);
    }
}
