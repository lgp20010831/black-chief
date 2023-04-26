package com.black.core.sql.code.condition.function;

import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class IsMapFunction extends BooleanFunction{
    @Override
    public String getName() {
        return "isMap";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return condition(arg1.getValue(env) instanceof Map);
    }
}
