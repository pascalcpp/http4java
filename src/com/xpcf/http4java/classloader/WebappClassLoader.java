package com.xpcf.http4java.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 6:19 PM
 */
public class WebappClassLoader extends URLClassLoader {

    public WebappClassLoader(String docBase, CommonClassLoader commonClassLoader) {
        super(new URL[] {}, commonClassLoader);


    }

}
