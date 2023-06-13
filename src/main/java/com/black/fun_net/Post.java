package com.black.fun_net;

import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("all")
public interface Post<T> extends Servlet{

    @Override
    default RequestMethod getRequestMethod() {
        return RequestMethod.POST;
    }
}
