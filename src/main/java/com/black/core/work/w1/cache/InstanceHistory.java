package com.black.core.work.w1.cache;

import com.black.core.work.w1.TaskGlobalListener;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter @Setter
public class InstanceHistory {

    String task_id;
    int size, index;
    String key, param, start_time, end_time, alias, result;

    public InstanceHistory(TaskGlobalListener listener, String result){
        task_id = listener.getTaskId();
        size = listener.size();
        index = listener.currentIndex();
        key = listener.getUniqueKey() == null ? null : listener.getUniqueKey().toDatabaseString();
        param = listener.getGlobalParam().toJson().toString();
        start_time = listener.getStartTime();
        end_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        alias = listener.getAlias();
        this.result = result;
    }
}
