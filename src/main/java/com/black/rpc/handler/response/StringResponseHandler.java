package com.black.rpc.handler.response;

import com.black.rpc.response.Response;
import com.black.core.query.MethodWrapper;

public class StringResponseHandler extends AbstractResponseHandler{
    @Override
    public boolean support(MethodWrapper mw, Response response) {
        return mw.getReturnType().equals(String.class);
    }

    @Override
    protected Object resolveUtfResponseBody(MethodWrapper mw, String utfBody) {
        return utfBody;
    }
}
