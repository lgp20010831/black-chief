package com.black.core.work.w1;

import com.alibaba.fastjson.JSONObject;

public interface BlockingHandlerConditionType extends TaskType {

    @Override
    default boolean isBlocking() {
        return true;
    }

    @Override
    default boolean isServerBraking(){
        return false;
    }

    //去处理, 然后返回唯一 key, 进行堵塞
    UniqueKey<JSONObject> handler(TaskGlobalParam globalParam);
}
