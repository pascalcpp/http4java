package com.xpcf.http4java.classloader;

import cn.hutool.core.util.StrUtil;
import com.xpcf.http4java.catalina.Context;
import com.xpcf.http4java.util.Constant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/12/2022 12:07 AM
 */
public class JspClassLoader extends URLClassLoader {

    private static Map<String, JspClassLoader> map = new HashMap<>();

    public static JspClassLoader getJspClassloader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        JspClassLoader jspClassLoader = map.get(key);
        if (null == jspClassLoader) {
            jspClassLoader = new JspClassLoader(context);
            map.put(key, jspClassLoader);
        }
        return jspClassLoader;
    }

    public static void invalidJspClassloader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        map.remove(key);
    }



    private JspClassLoader(Context context) {
        super(new URL[] {}, context.getWebappClassLoader());

        try {
            String subFolder;
            String path = context.getPath();
            if ("/".equals(path)) {
                subFolder = "_";
            } else {
                subFolder = StrUtil.subAfter(path, '/', false);
            }

            File classesFolder = new File(Constant.workFolder, subFolder);
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
