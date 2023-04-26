package com.black;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.black.core.json.JsonUtils;


public class JsonBean implements ChiefObject{

    protected SerializerFeature[] features(){
        return new SerializerFeature[]{SerializerFeature.WriteMapNullValue};
    }

    public JSONObject toJson(){
        return JsonUtils.letJson(this);
    }

    @Override
    public String toString() {
        return toJson().toString(features());
    }

}
