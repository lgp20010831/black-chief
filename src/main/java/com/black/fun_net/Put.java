package com.black.fun_net;

import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author 李桂鹏
 * @create 2023-06-12 15:36
 */
@SuppressWarnings("all")
public interface Put<T> extends Servlet{

    @Override
    default RequestMethod getRequestMethod() {
        return RequestMethod.PUT;
    }
}
