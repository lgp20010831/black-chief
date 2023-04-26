package com.black.core.work.w1.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w1.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DatabaseEntry {
    String taskId;
    Integer size;
    Integer currentIndex;
    String key;
    String jsonString;

    //任务开始时间
    String startTime;
    //上一次更新时间
    String updateTime;
    //工作流别名
    String alias;
    //任务延迟的毫秒数, 只有当服务停止时, 才会记录到数据库中
    Long life;
    //任务开始的毫秒数
    Long futureStart;
    //服务停止的毫秒数
    Long serverStopTime;
    public DatabaseEntry(TaskGlobalListener listener){
        this.taskId = listener.getTaskId();
        this.size = listener.size();
        this.currentIndex = listener.currentIndex();
        UniqueKey uniqueKey = listener.getUniqueKey();
        this.key = uniqueKey == null ? null : uniqueKey.toDatabaseString();
        jsonString = listener.getGlobalParam().toJson().toString();
        alias = listener.getAlias();
        startTime = listener.getStartTime();
        updateTime = listener.getUpdateTime();
        life = listener.life();
        futureStart = listener.futureStart();
        serverStopTime = listener.serverStopTime();
    }

    public TaskGlobalListener convert(){
        JSONObject jsonObject = JSON.parseObject(jsonString);
        BooleanTaskListener listener = new BooleanTaskListener(size, new DefaultMapTaskGlobalParam(jsonObject), taskId, alias);
        listener.setCurrentIndex(currentIndex);
        listener.setUniqueKey(key != null ? new JsonUniqueKey(JSON.parseObject(key)) : new JsonUniqueKey());
        listener.setStartTime(startTime);
        listener.setUpdateTime(updateTime);
        listener.setLife(life);
        listener.setFutureStart(futureStart);
        listener.setServerStopTime(serverStopTime);
        return listener;
    }

}
