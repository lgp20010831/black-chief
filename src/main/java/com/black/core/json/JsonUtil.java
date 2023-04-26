package com.black.core.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;
import java.util.Map;

public class JsonUtil {

    /** 创建json */
    public JSONObject buildJSON()  {return new JSONObject();}

    public JSONObject buildJSON(Map<String, Object> map) {return new JSONObject(map);}

    public JSONObject buildJSON(String text)  {return JSON.parseObject(text);}

    public JSONObject initJSON(String key, Object value){
        JSONObject json = buildJSON();
        json.put(key, value);
        return json;
    }

    /** 将map转换成json */
    public JSONObject convertMapToJSON(Map<String, Object> map){
        final JSONObject json = new JSONObject();
        json.putAll(map);
        return json;
    }

    /** 载入json */
    public JSONObject loadJSON(String key, Object value, JSONObject jsonObject){
        jsonObject.put(key, value);
        return jsonObject;
    }

    public JSONArray buildArray(){return new JSONArray();}

    public JSONArray buildArray(List<?> array){
        return new JSONArray((List<Object>) array);
    }


    public void judgeNotNull(Object pojo){

        ReflexHandler.getAccessibleFields(pojo)
                .forEach(
                        f ->{
                            if (AnnotationUtils.getAnnotation(f, NotNull.class) == null)
                                return;
                            try {
                                if (f.get(pojo) == null)
                                    throw new RuntimeException("字段:" + f +"不能为空");
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("get field : "+ f + "fail");
                            }
                        }
                );
    }


}
