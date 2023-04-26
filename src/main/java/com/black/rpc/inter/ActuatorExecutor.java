package com.black.rpc.inter;

import com.black.rpc.MethodInvoker;
import com.black.rpc.request.Request;

public interface ActuatorExecutor {


    Object execute(MethodInvoker methodInvoker, Object param, Request request) throws Throwable;

}
