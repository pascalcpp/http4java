package com.xpcf.http4java.classloader;

import com.xpcf.http4java.catalina.Context;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/12/2022 12:07 AM
 */
public class JspClassLoader extends URLClassLoader {



    private JspClassLoader(Context context) {
        super(new URL[] {}, context.getWebappClassLoader());
    }
}
