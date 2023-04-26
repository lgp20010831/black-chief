package com.black.core.factory.beans.method;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class MethodEnvironmentObject {

    private final Map<String, Object> paramMap = new HashMap<>();

    private Object[] rawArgs;

    private final MethodWrapper mw;

    private final String methodName;

    private final Object bean;

    private String id;

    public MethodEnvironmentObject(Object[] rawArgs, MethodWrapper mw, Object bean) {
        this.rawArgs = rawArgs;
        if (rawArgs == null){
            this.rawArgs = new Object[0];
        }
        for (ParameterWrapper pw : mw.getParameterWrappersSet()) {
            paramMap.put(pw.getName(), this.rawArgs[pw.getIndex()]);
        }
        this.mw = mw;
        methodName = mw.getName() + "|" + mw.getParameterCount();
        this.bean = bean;
    }

    public String getId() {
        if (id == null){
            id = UUID.randomUUID().toString();
        }
        return id;
    }
}
