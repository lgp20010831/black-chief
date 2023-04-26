package com.black.core.work.w1;

import com.alibaba.fastjson.JSONObject;

public class JsonUniqueKey implements UniqueKey<JSONObject>{

    private final JSONObject jsonObject;

    public JsonUniqueKey(){
        this(new JSONObject());
    }

    public JsonUniqueKey(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public JSONObject getSource() {
        return jsonObject;
    }

    @Override
    public String toDatabaseString() {
        return jsonObject != null ? jsonObject.toString() : null;
    }

    @Override
    public UniqueKey<JSONObject> addAllUniqueKey(UniqueKey<JSONObject> uniqueKey) {
        JSONObject source = uniqueKey.getSource();
        if (source != null){
            jsonObject.putAll(source);
        }
        return this;
    }
}
