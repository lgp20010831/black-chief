package com.black.core.work.w2.connect.entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.work.utils.ArrayString;
import com.black.core.work.utils.ObjectString;
import com.black.core.work.utils.WorkUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceEntry {

    private String id, workflowId, createTime, updateTime;

    @ArrayString
    private String routePath, currentNodeName;

    @ObjectString
    private String nodeScheduledTime, formData, properties;

    public boolean isScheduled(){
        return nodeScheduledTime != null;
    }

    public JSONObject getFormDataObject(){
        return WorkUtils.parseObject(formData);
    }

    public JSONObject getPropertiesJson(){
        return WorkUtils.parseObject(properties);
    }

    public JSONArray getRoutePathArray(){
        return WorkUtils.parseArray(routePath);
    }

    public JSONArray getCurrentNodeNames(){
        return WorkUtils.parseArray(currentNodeName);
    }
}
