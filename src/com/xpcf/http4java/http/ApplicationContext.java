package com.xpcf.http4java.http;

import com.xpcf.http4java.catalina.Context;

import java.io.File;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 1/7/2022 10:11 PM
 */
public class ApplicationContext extends BaseServletContext {
    private Map<String, Object> attributesMap;

    private Context context;

    public ApplicationContext(Context context) {
        this.attributesMap = new HashMap<>();
        this.context = context;
    }

    @Override
    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributesMap.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keySet = attributesMap.keySet();
        return Collections.enumeration(keySet);
    }


    @Override
    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }
}
