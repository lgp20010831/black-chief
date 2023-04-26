package com.black.rpc;


import com.alibaba.fastjson.JSONObject;
import com.black.rpc.annotation.InputBody;
import com.black.rpc.inter.ActuatorExecutor;
import com.black.rpc.inter.JsonRequestHandler;
import com.black.rpc.request.Request;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.json.JsonUtils;
import com.black.core.query.MethodWrapper;

import java.util.Collection;


public class DefaultActuatorExecutor implements ActuatorExecutor {

    final RpcConfiguration configuration;

    public DefaultActuatorExecutor(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object execute(MethodInvoker methodInvoker, Object param, Request request) throws Throwable {
        MethodWrapper mw = methodInvoker.getMw();
        Object[] args = new Object[mw.getParameterCount()];
        if (isInputBody(mw)){
            wiredInputBody(args, mw, param);
        }else {
            Collection<ParameterWrapper> parameterWrappers = mw.getParameterWrappersSet();
            JSONObject json = JsonUtils.letJson(param);
            for (ParameterWrapper pw : parameterWrappers) {
                JsonRequestHandler specialHandler = null;
                Object arg;
                for (JsonRequestHandler requestHandler : configuration.getRequestHandlers()) {
                    if (requestHandler.support(mw, pw)) {
                        specialHandler = requestHandler;
                        break;
                    }
                }
                if (specialHandler != null){
                    arg = defaultWiredValue(json, pw);
                }else {
                    arg = specialHandler.wiredParm(mw, pw, json, request);
                }
                args[pw.getIndex()] = arg;
            }
        }
        return methodInvoker.invoke(args);
    }

    private Object defaultWiredValue(JSONObject json, ParameterWrapper pw){
        return json.get(pw.getName());
    }

    private void wiredInputBody(Object[] args, MethodWrapper mw, Object param){
        ParameterWrapper inputBodyParam = mw.getSingleParameterByAnnotation(InputBody.class);
        args[inputBodyParam.getIndex()] = param;
    }

    private boolean isInputBody(MethodWrapper mw){
        return mw.parameterHasAnnotation(InputBody.class);
    }
}
