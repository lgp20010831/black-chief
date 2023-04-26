package com.black.core.work.w1;

import com.alibaba.fastjson.JSONObject;

public class UniqueKeyUtils {

    public static UniqueKey<JSONObject> bool(){
        return bool(new JSONObject());
    }

    public static UniqueKey<JSONObject> bool(JSONObject json){
        return new JsonUniqueKey(json);
    }



}
