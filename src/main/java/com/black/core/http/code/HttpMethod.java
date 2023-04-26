package com.black.core.http.code;

public enum HttpMethod {

    GET("GET"),
    POST("POST"),
    PART("PART");

    String name;

    HttpMethod(String method) {
        name = method;
    }

    public String getName() {
        return name;
    }
}
