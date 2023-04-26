package com.black.core.sql.action;

import com.alibaba.fastjson.JSONObject;
import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.json.JsonUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@GlobalAround
public class NullMapAround implements GlobalAroundResolver {

    public static String[] DEFAULT_NULL_ARRAY = {"null", ""};

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper mw) {
        List<ParameterWrapper> parameters = mw.getParameterByAnnotation(NullRepair.class);
        for (ParameterWrapper parameter : parameters) {
            NullRepair annotation = parameter.getAnnotation(NullRepair.class);
            Class<?> type = parameter.getType();
            Object param = args[parameter.getIndex()];
            if (param != null){
                param = processorObject(annotation.value(), param);
                args[parameter.getIndex()] = param;
            }
        }
        return args;
    }

    public static <T> T processorObject(T t){
        return processorObject(DEFAULT_NULL_ARRAY, t);
    }

    public static <T> T processorObject(String[] targets, T t){
        if (t == null)
            return t;
        Class<?> type = t.getClass();
        if (Map.class.isAssignableFrom(type)){
            processorMap(targets, (Map<String, Object>)t);
        }else if (Collection.class.isAssignableFrom(type)){
            Collection<?> collection = (Collection<?>) t;
            for (Object obj : collection) {
                if (obj instanceof Map){
                    processorMap(targets, (Map<String, Object>) obj);
                }else {
                    processorMap(targets, JsonUtils.letJson(obj));
                }
            }
        }else if (String.class.equals(type)){
            for (String target : targets) {
                if (target.equals(t)){
                    return null;
                }
            }
        }else {
            JSONObject map = JsonUtils.letJson(t);
            processorMap(targets, map);
        }
        return t;
    }

    public static Map<String, Object> processorMap(Map<String, Object> map){
        return processorMap(DEFAULT_NULL_ARRAY, map);
    }

    public static Map<String, Object> processorMap(String[] targets, Map<String, Object> map){
        for (String key : map.keySet()) {
            Object o = map.get(key);
            for (String target : targets) {
                if (target.equals(o)){
                    map.replace(key, null);
                }
            }
        }
        return map;
    }

}
