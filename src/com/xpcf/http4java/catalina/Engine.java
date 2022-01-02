package com.xpcf.http4java.catalina;

import com.sun.security.ntlm.Server;
import com.xpcf.http4java.util.ServerXMLUtil;

import java.util.List;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/2/2022 12:17 PM
 */
public class Engine {

    private String defaultHost;

    private List<Host> hosts;

    private Service service;

    public Engine(Service service) {
        this.defaultHost = ServerXMLUtil.getEngineDefaultHost();
        this.hosts = ServerXMLUtil.getHosts(this);
        this.service = service;
        checkDefault();
    }

    public Service getService() {
        return service;
    }

    private void checkDefault() {
        if (null == getDefaultHost()) {
            throw new RuntimeException("this defaultHost" + defaultHost + "dose not exist!");
        }
    }

    public Host getDefaultHost() {
        for (Host host : hosts) {
            if (host.getName().equals(defaultHost)) {
                return host;
            }
        }
        return null;
    }

}
