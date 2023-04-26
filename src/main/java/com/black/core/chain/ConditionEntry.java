package com.black.core.chain;

import com.black.bin.InstanceType;
import com.black.core.spring.factory.AgentLayer;

public interface ConditionEntry {

    //设置别名
    ConditionEntry setAlias(String alias);

    //是否符合条件,返回 true 收集
    ConditionEntry condition(Judge judge);

    Judge getJudge();

    //是否需要排序
    ConditionEntry needOrder(boolean order);

    ConditionEntry proxy(AgentLayer layer);

    void setInstanceType(InstanceType instanceType);

    InstanceType getInstanceType();

    boolean isProxy();

    AgentLayer getLayer();

    //设置是否需要进行实例化
    void instance(boolean instance);

    boolean isInstance();
}
