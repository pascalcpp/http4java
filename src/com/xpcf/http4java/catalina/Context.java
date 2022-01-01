package com.xpcf.http4java.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;

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

    public Context(String path, String docBase) {
        TimeInterval timer = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms",
                this.docBase, timer.intervalMs());


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
}
