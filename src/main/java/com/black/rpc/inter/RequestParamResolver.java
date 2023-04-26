package com.black.rpc.inter;

import com.black.rpc.handler.RequestParamCarrier;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

public interface RequestParamResolver {

    boolean support(MethodWrapper mw, ParameterWrapper pw);

    void resolverParam(MethodWrapper mw, ParameterWrapper pw, Object param, RequestParamCarrier rpc);
}
