package com.black.bin;

import com.black.core.json.ReflexUtils;

import java.util.Map;

public class ReflexInstanceResolver implements InstanceBeanResolver{
    @Override
    public boolean support(InstanceType type) {
        return type == InstanceType.REFLEX;
    }

    @Override
    public <T> T instance(Class<T> type, Map<String, Object> source) {
        return ReflexUtils.instance(type);
    }
}
