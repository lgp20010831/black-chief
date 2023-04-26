package com.black.core.aop.servlet;

import java.io.Serializable;

public interface RestResponse extends Serializable {

    void setResult(Object result);

    default void setSuccessful(Boolean successful){}

    default void setCode(Integer code){}

    void setMessage(String message);

    default void setTotal(Long total){}

    Object obtainResult();

    default Long obtainTotal(){
        return null;
    }

    default boolean enabledThrowableStack(){
        return false;
    }

    default void setThrowableStackTrace(String msg){}
}
