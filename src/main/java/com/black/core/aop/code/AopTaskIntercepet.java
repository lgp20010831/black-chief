package com.black.core.aop.code;

public interface AopTaskIntercepet {

    Object processor(HijackObject hijack) throws Throwable;
}
