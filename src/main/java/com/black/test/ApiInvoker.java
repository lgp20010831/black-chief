package com.black.test;

import java.util.Map;

public interface ApiInvoker {

    // GET POST PUT ....
    boolean supportMethod(String method);

    // 执行
    String execute(String url, Map<String, String> headerMap, Object param, RecordObject object) throws Throwable;
}
