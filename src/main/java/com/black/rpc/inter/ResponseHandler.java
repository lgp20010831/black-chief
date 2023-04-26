package com.black.rpc.inter;

import com.black.rpc.response.Response;
import com.black.core.query.MethodWrapper;

import java.io.IOException;

public interface ResponseHandler {

    boolean support(MethodWrapper mw, Response response);

    Object resolveResponse(MethodWrapper mw, Response response) throws IOException;
}
