package com.black.core.work.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.json.Alias;
import com.black.core.json.ReflexUtils;
import com.black.utils.IdUtils;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class WorkUtils {

    public static String getTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static List<String> convertList(JSONArray array){
        return array.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public static Set<String> convertSet(JSONArray array){
        return array.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public static String getRandomId(){
        return IdUtils.createShort8Id();
    }


    public static String getArrayString(Collection<String> collection){
        return new JSONArray(new ArrayList<>(collection)).toString();
    }

    public static JSONArray convertArray(List<String> list){
        JSONArray array = new JSONArray();
        array.addAll(list);
        return array;
    }

    public static long parserTimeStr(String timeStr){
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public static <T> Collection<T> merge(Collection<T>... nodes){
        ArrayList<T> list = new ArrayList<>();
        for (Collection<T> node : nodes) {
            if (node != null){
                list.addAll(node);
            }
        }
        return list;
    }

    public static JSONArray parseArray(String arrayText){
        if (StringUtils.hasText(arrayText)) {
            return JSON.parseArray(arrayText);
        }
        return new JSONArray();
    }

    public static JSONObject parseObject(String jsonText){
        if (StringUtils.hasText(jsonText)) {
            return JSON.parseObject(jsonText);
        }
        return new JSONObject();
    }

    public static <T> T load(Object obj, T target){
        if (obj == null){
            return target;
        }

        Map<String, Object> values = new HashMap<>();
        for (Field field : ReflexHandler.getAccessibleFields(obj, true)) {
            Alias alias = AnnotationUtils.getAnnotation(field, Alias.class);
            String name = alias == null ? field.getName() : alias.value();
            values.put(name, ReflexUtils.getValue(field, obj));
        }

        for (Field field : ReflexHandler.getAccessibleFields(target, true)) {
            final String name = field.getName();
            final Class<?> type = field.getType();
            Object source = values.get(name);
            if (source != null){
                if (!type.isAssignableFrom(source.getClass())){
                    TypeHandler typeHandler = TypeConvertCache.initAndGet();
                    if (typeHandler != null){
                        source = typeHandler.convert(type, source);
                    }
                }
                ReflexUtils.setValue(field, target, source);
            }
        }
        return target;
    }

    public static <T> T parse(Object obj, Class<T> clazz){
        return load(obj, ReflexUtils.instance(clazz));
    }

}
