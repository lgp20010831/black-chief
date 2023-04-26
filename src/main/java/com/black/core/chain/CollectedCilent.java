package com.black.core.chain;

public interface CollectedCilent {

    void registerCondition(QueryConditionRegister register);

    void collectFinish(ConditionResultBody resultBody);
}
