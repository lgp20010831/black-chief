package com.black.rpc.handler;

import com.black.rpc.inter.RequestParamResolver;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

public class DefaultRequestParamResolver implements RequestParamResolver {
    @Override
    public boolean support(MethodWrapper mw, ParameterWrapper pw) {
        return true;
    }

    @Override
    public void resolverParam(MethodWrapper mw, ParameterWrapper pw, Object param, RequestParamCarrier rpc) {

    }
}
