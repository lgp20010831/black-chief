package com.black.test;

import com.alibaba.fastjson.JSONObject;
import com.black.core.builder.HttpBuilder;
import com.black.core.json.JsonUtils;
import com.black.core.log.Catalog;

import java.util.Map;
import java.util.StringJoiner;

public class DefaultGetApiInvoker implements ApiInvoker{

    final Catalog log;

    public DefaultGetApiInvoker(Catalog log) {
        this.log = log;
    }

    @Override
    public boolean supportMethod(String method) {
        return "GET".equals(method);
    }

    @Override
    public String execute(String url, Map<String, String> headerMap, Object param, RecordObject object) {
        JSONObject json = JsonUtils.letJson(param);
        url = parseUrl(url, json);
        log.debug("===> request [GET] url: " + url);
        log.debug(" do execute .... please wait");
        long start = System.currentTimeMillis();
        try {
            return HttpBuilder.get(url).putHeaders(headerMap).executeAndGetBody();
        }finally {
            log.debug("execute finish, take [" + (System.currentTimeMillis() - start) + "] ms");
        }
    }
    private String parseUrl(String url, JSONObject json){
        if (url.contains("?")){
            return url;
        }
        StringJoiner joiner = new StringJoiner("&");
        json.forEach((k, v) -> {
            joiner.add(k + "=" + v);
        });
        url = url + "?" + joiner;
        return url;
    }
}
