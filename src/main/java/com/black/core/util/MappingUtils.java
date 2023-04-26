package com.black.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingUtils {

    public static void groupMerge(List<Map<String, Object>> source,
                                  List<Map<String, Object>> target,
                                  MapMapping targetMapping,
                                  String echoName){
        groupMerge(source, target, targetMapping, targetMapping, echoName);
    }

    public static void groupMerge(List<Map<String, Object>> source,
                                  List<Map<String, Object>> target,
                                  MapMapping sourceMapping,
                                  MapMapping targetMapping,
                                  String echoName){
        Map<String, Map<String, Object>> mapping = new HashMap<>();
        for (Map<String, Object> map : source) {
            String key = sourceMapping.getMapping(map);
            mapping.put(key, map);
        }

        for (Map<String, Object> map : target) {
            String key = targetMapping.getMapping(map);
            Map<String, Object> sourceMap = mapping.get(key);
            if (sourceMap != null){

                @SuppressWarnings("all")
                List<Object> list = (List<Object>) sourceMap.computeIfAbsent(echoName, en -> new ArrayList<>());
                list.add(map);
            }
        }
    }

    public static String getString(Map<String, Object> map, String key){
        if (map == null){
            return null;
        }
        Object val = map.get(key);
        return val == null ? null : val.toString();
    }

    public static <K, V> List<Map<String, ?>> turn(Map<K, V> map, String keyKey, String valKey){
        if (map == null){
            return new ArrayList<>();
        }
        List<Map<String, ?>> result = new ArrayList<>();
        map.forEach((k, v) ->{
            result.add(Av0.of(keyKey, k, valKey, v));
        });
        return result;
    }

}
