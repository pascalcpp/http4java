package com.xpcf.http4java.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 6:19 PM
 */
public class WebappClassLoader extends URLClassLoader {

    public WebappClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[] {}, commonClassLoader);
        try {
            File webinfFolder = new File(docBase, "WEB-INF");
            File classesFolder = new File(webinfFolder, "classes");
            File libFolder = new File(webinfFolder, "lib");
            // 识别为目录
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + "/");

            this.addURL(url);
            List<File> jarFiles = FileUtil.loopFiles(libFolder);
            for (File jarFile : jarFiles) {
                url = new URL("file:" + jarFile.getAbsolutePath());
                this.addURL(url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
