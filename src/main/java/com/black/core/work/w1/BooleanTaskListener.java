package com.black.core.work.w1;


import lombok.Setter;

public class BooleanTaskListener implements TaskGlobalListener {

    final int size;

    @Setter volatile int currentIndex = 1;

    final TaskGlobalParam taskGlobalParam;

    //任务唯一 id
    final String taskId;

    UniqueKey<?> uniqueKey;

    final String alias;

    @Setter String startTime;

    @Setter String updateTime;
    @Setter Long life;
    @Setter Long futureStart;
    @Setter Long serverStopTime;

    public BooleanTaskListener(int size, TaskGlobalParam taskGlobalParam, String taskId, String alias) {
        this.size = size;
        this.taskGlobalParam = taskGlobalParam;
        this.taskId = taskId;
        this.alias = alias;
    }

    @Override
    public Long life() {
        return life;
    }

    @Override
    public Long futureStart() {
        return futureStart;
    }

    @Override
    public Long serverStopTime() {
        return serverStopTime;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String getStartTime() {
        return startTime;
    }

    @Override
    public String getUpdateTime() {
        return updateTime;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int currentIndex() {
        return currentIndex;
    }

    @Override
    public TaskGlobalParam getGlobalParam() {
        return taskGlobalParam;
    }

    @Override
    public void setCuurentIndex(int index) {
        this.currentIndex = index;
    }

    public void setUniqueKey(UniqueKey<?> uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public UniqueKey<?> getUniqueKey() {
        return uniqueKey;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }
}
