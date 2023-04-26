package com.black.core.sql.xml;

import com.black.core.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EscapeProcessor {

    public static final Map<String, String> escapeMap = new ConcurrentHashMap<>();

    static {
        escapeMap.put("&lt;", "<");
        escapeMap.put("&gt;", ">");
    }

    public static String escape(String txt){
        if (!StringUtils.hasText(txt)){
            return txt;
        }
        for (String key : escapeMap.keySet()) {
            String val = escapeMap.get(key);
            txt = txt.replace(key, val);
        }
        return txt;
    }

}
