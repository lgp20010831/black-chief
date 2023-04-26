package com.black.aviator.function;

import com.black.aviator.object.AviatorList;
import com.black.core.util.Av0;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.ArrayList;
import java.util.Map;

public class NewListFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "asList";
    }

    @Override
    public AviatorObject call(Map<String, Object> env) {
        return new AviatorList(new ArrayList<>());
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return new AviatorList(Av0.as(arg1.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return new AviatorList(Av0.as(arg1.getValue(env), arg2.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        return new AviatorList(Av0.as(arg1.getValue(env), arg2.getValue(env), arg3.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3, AviatorObject arg4) {
        return new AviatorList(Av0.as(arg1.getValue(env), arg2.getValue(env), arg3.getValue(env), arg4.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3, AviatorObject arg4, AviatorObject arg5) {
        return new AviatorList(Av0.as(arg1.getValue(env), arg2.getValue(env), arg3.getValue(env), arg4.getValue(env), arg5.getValue(env)));
    }
}
