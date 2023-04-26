package com.black.core.work.w1;

public interface IfConditionType extends TaskType {

    @Override
    default boolean isBlocking(){
        return false;
    }

    @Override
    default boolean isServerBraking(){
        return false;
    }

    //根据全局参数进行处理, 结果返回 bool 类型
    //3个  null. true,
    Boolean judge(TaskGlobalParam globalParam);
}
