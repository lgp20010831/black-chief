package com.black.core.json.annotation;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonDemo {


    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        json.put("name", "lgp");
        json.put("age", null);
        System.out.println(json.toString(SerializerFeature.WriteMapNullValue));
        System.out.println(json);
    }
}
