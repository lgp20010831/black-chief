package com.black.resolve;

import com.black.io.in.JHexByteArrayInputStream;

public interface InputStreamResolver {

    //当前支架是否支持处理
    boolean support(Object rack);

    Object doResolve(Object rack, JHexByteArrayInputStream inputStream) throws Throwable;
}
