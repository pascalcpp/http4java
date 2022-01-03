package com.xpcf.http4java.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import com.xpcf.http4java.util.ServerXMLUtil;

import java.util.List;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/2/2022 1:07 PM
 */
public class Service {

    private String name;

    private Engine engine;

    private Server server;

    private List<Connector> connectors;


    public Service(Server server) {
        this.server = server;
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
        this.connectors = ServerXMLUtil.getConnectors(this);
    }

    public void start() {
        init();
    }

    private void init() {
        TimeInterval timeInterval = DateUtil.timer();
        for (Connector connector : connectors) {
            connector.init();
        }
        LogFactory.get().info("Initialization processed in {} ms", timeInterval.intervalMs());
        for (Connector connector : connectors) {
            connector.start();
        }
    }

    public Engine getEngine() {
        return engine;
    }
}
