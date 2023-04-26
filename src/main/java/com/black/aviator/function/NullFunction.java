package com.black.aviator.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class NullFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "nill";
    }

    @Override
    public AviatorObject call() throws Exception {
        return null;
    }

    @Override
    public AviatorObject call(Map<String, Object> env) {
        return null;
    }
}
