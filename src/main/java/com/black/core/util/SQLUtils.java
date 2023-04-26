package com.black.core.util;

import com.black.core.chain.GroupUtils;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLUtils {

    public static List<Map<String, Object>> integration(List<Map<String, Object>> source, @NonNull FUN fun, FunGroup... groups){
        if (source == null){
            return new ArrayList<>();
        }
        if (groups == null || groups.length == 0){
            return source;
        }

        for (Map<String, Object> map : source) {
            Object mapping = fun.getMapping(map);
            if (mapping != null){
                for (FunGroup group : groups) {
                    @SuppressWarnings("all")
                    List<Map<String, Object>> array = (List<Map<String, Object>>) map.computeIfAbsent(group.name, na -> new ArrayList<>());
                    List<Map<String, Object>> list = group.map.get(mapping);
                    if (list != null){
                        array.addAll(list);
                    }
                }
            }
        }
        return source;
    }

    public static FUN mapFun(String key){
        return new FUN() {
            @Override
            public Object getMapping(Map<String, Object> map) {
                return map.get(key);
            }
        };
    }

    public static FunGroup create(List<Map<String, Object>> subSource, String name, FUN fun){
        return new FunGroup(name, fun, subSource);
    }

    @Getter
    public static class FunGroup{
        private final String name;
        private final FUN fun;
        private final Map<Object, List<Map<String, Object>>> map = new HashMap<>();

        public FunGroup(String name, FUN fun, List<Map<String, Object>> subSource) {
            this.name = name;
            this.fun = fun;
            if (subSource != null && !subSource.isEmpty()){
                map.putAll(GroupUtils.groupArray(subSource, fun::getMapping));
            }
        }
    }

    public interface FUN{
        Object getMapping(Map<String, Object> map);
    }

}
