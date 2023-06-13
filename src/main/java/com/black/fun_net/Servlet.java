package com.black.fun_net;

import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("all")
public interface Servlet {

    void fetch() throws Throwable;

    default RequestMethod getRequestMethod(){
        throw new UnsupportedOperationException("Require subclasses to indicate " +
                "specific representative request methods");
    }

    default String getDesc(){
        return null;
    }
}
