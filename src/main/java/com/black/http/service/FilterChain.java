package com.black.http.service;

public interface FilterChain {

    void fireRequest() throws Throwable;

    void fireResponse() throws Throwable;
}
