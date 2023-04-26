package com.black.api;

import com.black.core.query.MethodWrapper;

public interface ApiRequestAndResponseResolver {

    boolean support(MethodWrapper mw);

    void doResolve(MethodWrapper mw, HttpMethod method, Configuration configuration, Class<?> type);

}
