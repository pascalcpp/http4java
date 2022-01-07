package com.xpcf.http4java.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 5:29 PM
 */
public class CommonClassLoader extends URLClassLoader {

    public CommonClassLoader() {
        //这个加载器会到 urls 对应的这些文件里去找类文件。
        super(new URL[] {});
        try {
            File workFolder = new File(System.getProperty("user.dir"));
            File libFolder = new File(workFolder, "lib");
            File[] jarFiles = libFolder.listFiles();
            for (File jarFile : jarFiles) {
                if (jarFile.getName().endsWith(".jar")) {
                    URL url = new URL("file:" + jarFile.getAbsolutePath());
                    this.addURL(url);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
