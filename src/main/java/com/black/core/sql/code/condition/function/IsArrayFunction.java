package com.black.core.sql.code.condition.function;

import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Collection;
import java.util.Map;

public class IsArrayFunction extends BooleanFunction{

    @Override
    public String getName() {
        return "isArray";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        return condition(value != null && (value.getClass().isArray() || value instanceof Collection));
    }
}
