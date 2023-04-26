package com.black.core.work.w1;

public interface TaskGlobalListener {

    //存活的时间
    Long life();

    //任务开始的时间
    Long futureStart();

    //服务停止的时间
    Long serverStopTime();

    String getAlias();

    String getStartTime();

    String getUpdateTime();

    void setUpdateTime(String updateTime);

    //返回任务总节点数量
    int size();

    //当前任务正在执行的节点的下标
    int currentIndex();

    //返回全局参数
    TaskGlobalParam getGlobalParam();

    void setCuurentIndex(int index);

    //获取任务唯一 id
    String getTaskId();

    //唯一建
    void setUniqueKey(UniqueKey<?> uniqueKey);

    UniqueKey<?> getUniqueKey();
}
