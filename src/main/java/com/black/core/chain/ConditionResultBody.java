package com.black.core.chain;

import java.util.Collection;

public interface ConditionResultBody {

    String getAlias();

    void registerObject(Object target);

    //获取收集到的资源
    Collection<Object> getCollectSource();
}
