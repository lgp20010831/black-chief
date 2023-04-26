package com.black.bin;

import java.util.Map;

public interface InstanceBeanResolver {

    boolean support(InstanceType type);

    <T> T instance(Class<T> type, Map<String, Object> source);
}
