package com.black.core.work.w1;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class DefaultMapTaskGlobalParam implements TaskGlobalParam {

    private Map<String, Object> source;


    public DefaultMapTaskGlobalParam(Map<String, Object> source) {
        this.source = source;
    }


    @Override
    public Object get(String key) {
        return source.get(key);
    }

    @Override
    public void set(String k, Object v) {
        source.put(k, v);
    }

    @Override
    public Object remove(String key) {
        return source.remove(key);
    }

    @Override
    public JSONObject toJson() {
        if (source instanceof JSONObject) {
            return (JSONObject) source;
        }
        return (JSONObject) (source = new JSONObject(source));
    }

    @Override
    public Map<String, Object> getSource() {
        return source;
    }
}
