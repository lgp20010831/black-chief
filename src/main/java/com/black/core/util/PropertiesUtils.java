package com.black.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {

    public static final String SUFFIX = ".properties";

    public static Map<String, String> load(String path){
        String name = getName(path);
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            Properties pro = new Properties();
            pro.load(in);
            Map<String, String> result = new HashMap<>();
            pro.forEach((k, v) ->{
                if (k != null && v != null){
                    result.put(k.toString(), v.toString());
                }
            });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getName(String name){
        if (name == null)
            return null;
        if (!name.endsWith(SUFFIX)) {
            name = name + SUFFIX;
        }
        return name;
    }

}
