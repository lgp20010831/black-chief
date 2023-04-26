package com.black.rpc.inter;

import com.black.rpc.request.Request;
import com.black.core.query.MethodWrapper;

import java.io.IOException;

public interface RequestDistinctBuilder {

    boolean support(MethodWrapper mw);

    Request createRequest(MethodWrapper mw, Object requestParam, Object[] rawArgs) throws IOException;
}
