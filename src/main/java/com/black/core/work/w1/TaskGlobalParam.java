package com.black.core.work.w1;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public interface TaskGlobalParam {

    Object get(String key);

    void set(String k, Object v);

    Object remove(String key);

    JSONObject toJson();

    Map<String, Object> getSource();
}
