package com.black.sql_v2.period;

import com.alibaba.fastjson.JSONObject;
import com.black.core.chain.GroupUtils;
import com.black.core.json.JsonUtils;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StreamUtils;
import com.black.io.in.ObjectInputStream;
import com.black.utils.TypeUtils;

import java.sql.SQLException;
import java.util.*;

import static com.black.utils.ServiceUtils.patternGetValue;

/**
 * @author 李桂鹏
 * @create 2023-05-12 18:28
 */
@SuppressWarnings("all")
public interface ListResourceHandler {

    default List<Map<String, Object>> list(){
        return null;
    }

    default List<JSONObject> jsonList(){
        return StreamUtils.mapList(list(), JsonUtils::letJson);
    }

    default JSONObject json(){
        return SQLUtils.getSingle(jsonList());
    }

    default int intVal(){
        return intVal(0);
    }

    default int intVal(int defVal){
        Integer intValue = intValue();
        return intValue == null ? defVal : intValue;
    }

    default Integer intValue(){
        return SQLUtils.getSingle(intList());

    }

    default List<Integer> intList(){
        return StreamUtils.mapList(values(), e -> Integer.valueOf(String.valueOf(e)));
    }

    default String stringValue(){
        return SQLUtils.getSingle(stringList());
    }

    default List<String> stringList(){
        return StreamUtils.mapList(values(), TypeUtils::castToString);
    }

    default double doubleVal(){
        return doubleVal(0);
    }

    default double doubleVal(double defVal){
        Double doubleValue = doubleValue();
        return doubleValue == null ? defVal : doubleValue;
    }

    default Double doubleValue(){
        return SQLUtils.getSingle(doubleList());

    }

    default List<Object> values(){
        ArrayList<Object> list = new ArrayList<>();
        List<Map<String, Object>> resultList = list();
        for (Map<String, Object> map : resultList) {
            Collection<Object> values = map.values();
            ArrayList<Object> arrayList = new ArrayList<>(values);
            if (arrayList.isEmpty()) {
                list.add(null);
            }else {
                list.add(arrayList.get(0));
            }
        }
        return list;
    }

    default List<Double> doubleList(){
        return StreamUtils.mapList(values(), e -> Double.valueOf(String.valueOf(e)));
    }

    default boolean booleanVal(){
        return booleanVal(false);
    }

    default boolean booleanVal(boolean defVal){
        Boolean value = booleanValue();
        return value == null ? defVal : value;
    }

    default Boolean booleanValue(){
        return SQLUtils.getSingle(booleanList());
    }

    default List<Boolean> booleanList(){
        return StreamUtils.mapList(values(), e -> Boolean.valueOf(String.valueOf(e)));
    }

    default <T> ObjectInputStream<T> getObjectInputStream(){
        Map<String, Object> map = map();
        if (map == null) return null;
        Collection<Object> values = map.values();
        return new ObjectInputStream<T>((Collection<T>) values);
    }

    default <T> Collection<T> valueList(){
        return (Collection<T>) map().values();
    }

    default <T> T javaSingle(Class<T> type){
        return SQLUtils.getSingle(javaList(type));
    }

    default <T> List<T> javaList(Class<T> type){
        List<JSONObject> list = jsonList();
        return StreamUtils.mapList(list, json -> JSONObject.toJavaObject(json, type));
    }

    default Map<String, String> custom(String keyPattern, String valPattern){
        List<Map<String, Object>> list = list();
        Map<String, String> map = new LinkedHashMap<>();
        for (Map<String, Object> ele : list) {
            String key = patternGetValue(ele, keyPattern);
            String val = patternGetValue(ele, valPattern);
            map.put(key, val);
        }
        return map;
    }

    default Map<String, List<String>> customList(String keyPattern, String valPattern){
        List<Map<String, Object>> list = list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Map<String, Object> ele : list) {
            String key = patternGetValue(ele, keyPattern);
            String val = patternGetValue(ele, valPattern);
            List<String> stringList = map.computeIfAbsent(key, k -> new ArrayList<>());
            stringList.add(val);
        }
        return map;
    }

    default Map<String, Map<String, Object>> singleGroup(String expression){
        return GroupUtils.singleGroupArray(list(), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }

    default Map<String, List<Map<String, Object>>> listGroup(String expression){
        return GroupUtils.groupArray(list(), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }

    default <T> Map<String, T> singleJavaGroup(String expression, Class<T> type){
        return GroupUtils.singleGroupArray(javaList(type), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }

    default <T> Map<String, List<T>> listJavaGroup(String expression, Class<T> type){
        return GroupUtils.groupArray(javaList(type), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }
    default Map<String, Object> map(){
        return SQLUtils.getSingle(list());
    }
}
