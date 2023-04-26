package com.black.test;

import com.black.api.JSONTool;
import com.black.core.builder.HttpBuilder;
import com.black.core.log.Catalog;

import java.util.Map;

public class DefaultPostApiInvoker implements ApiInvoker{


    final Catalog log;

    public DefaultPostApiInvoker(Catalog log) {
        this.log = log;
    }

    @Override
    public boolean supportMethod(String method) {
        return method.equals("POST");
    }

    @Override
    public String execute(String url, Map<String, String> headerMap, Object param, RecordObject object) throws Throwable {
        String paramString = param == null ? "" : param.toString();
        String body = JSONTool.formatJson(paramString);
        object.setRequestDemo(body);
        log.debug("===> request [POST] url: " + url);
        log.trace("===> request [POST] Body: \n" + body);
        log.debug(" do execute .... please wait");
        long start = System.currentTimeMillis();
        try {
            return HttpBuilder.post(url).putHeaders(headerMap).body(body).executeAndGetBody();
        }finally {
            log.debug("execute finish, take [" + (System.currentTimeMillis() - start) + "] ms");
        }

    }
}
