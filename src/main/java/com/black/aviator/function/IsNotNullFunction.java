package com.black.aviator.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class IsNotNullFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "isNotNull";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return arg1.getValue(env) == null ?  AviatorBoolean.FALSE : AviatorBoolean.TRUE;
    }
}
