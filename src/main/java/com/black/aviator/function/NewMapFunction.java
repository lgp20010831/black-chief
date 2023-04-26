package com.black.aviator.function;

import com.black.aviator.object.AviatorMap;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class NewMapFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "ofMap";
    }


    @Override
    public AviatorObject call(Map<String, Object> env) {
        return new AviatorMap(new HashMap<>());
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Map<Object, Object> map = new HashMap<>();
        map.put(arg1.getValue(env), arg2.getValue(env));
        return new AviatorMap(map);
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3, AviatorObject arg4) {
        Map<Object, Object> map = new HashMap<>();
        map.put(arg1.getValue(env), arg2.getValue(env));
        map.put(arg3.getValue(env), arg4.getValue(env));
        return new AviatorMap(map);
    }
}
