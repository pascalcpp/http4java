package com.xpcf.http4java.catalina;

import com.xpcf.http4java.util.ServerXMLUtil;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/2/2022 1:07 PM
 */
public class Service {

    private String name;

    private Engine engine;

    private Server server;

    public Service(Server server) {
        this.server = server;
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
    }


    public Engine getEngine() {
        return engine;
    }
}
