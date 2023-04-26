package com.black.core.http.code;

import com.alibaba.fastjson.JSON;
import com.black.core.util.Av0;
import com.black.core.util.Utils;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

public class HttpUrlCreator {

    public static String create(String url, Map<String, Object> params){
        String escape = escape(url);
        if (Utils.isEmpty(params)){
            return escape;
        }
        StringJoiner joiner = new StringJoiner("&", "?", "");
        for (String name : params.keySet()) {
            Object param = params.get(name);
            if (param instanceof Collection){
                Collection<Object> collection = (Collection<Object>) param;
                for (Object val : collection) {
                    append(joiner, name, val);
                }
            }else if (param instanceof Map){
                append(joiner, name, JSON.toJSONString(param));
            }else {
                append(joiner, name, param);
            }
        }
        return escape + joiner;
    }

    public static void append(StringJoiner joiner, String name, Object value){
        String urlVal = value == null ? "" : value.toString();
        joiner.add(name + "=" + urlVal);
    }

    public static String escape(String url){
        url = url.replace(" ", "%20");
        url = url.replace("#", "%23");
        return url;
    }

    public static void main(String[] args) {
        System.out.println(create("http://www.baidu.com", Av0.js("name", "lgp", "age", Av0.as(1, 2, 3, 4, 5))));
    }

}
