package com.black.core.work.w2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.ConversionWay;
import com.black.core.convert.TypeHandler;

public class WorkflowTypeHandler {


    public static void load(){
        TypeHandler handler = TypeConvertCache.initAndGet();
        if (!handler.isParsed(WorkflowTypeHandler.class)) {
            handler.parseSingle(new WorkflowTypeHandler());
        }
    }

    @ConversionWay
    JSONObject tj(String txt){
        return JSONObject.parseObject(txt);
    }

    @ConversionWay
    JSONArray ta(String txt){
        return JSONArray.parseArray(txt);
    }

}
