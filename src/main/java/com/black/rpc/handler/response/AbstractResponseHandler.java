package com.black.rpc.handler.response;

import com.black.rpc.response.Response;
import com.black.rpc.inter.ResponseHandler;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;

import java.io.IOException;

public abstract class AbstractResponseHandler implements ResponseHandler {

    @Override
    public Object resolveResponse(MethodWrapper mw, Response response) throws IOException {
        String readUTF = response.getParam().toString();
        if (!StringUtils.hasText(readUTF)){
            return resolverNullUtfBody(mw);
        }
        return resolveUtfResponseBody(mw, readUTF);
    }

    protected Object resolverNullUtfBody(MethodWrapper mw){
        return null;
    }

    protected Object resolveUtfResponseBody(MethodWrapper mw, String utfBody){
        throw new IllegalStateException("subclass override is required for specific processing");
    }
}
