package com.black.core.aop.servlet.plus;

import com.black.core.cache.EntryCache;
import com.black.core.entry.EntryExtenderDispatcher;
import com.black.core.tools.BeanUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MethodEntryExecutor {
    public static final String END_FLAG = ")";
    public static final String START_FLAG = "(";

    public static Map<String, String> handlerFillSet(Set<String> fillSet){
        Map<String, String> result = new HashMap<>();
        for (String set : fillSet) {
            String[] vs = set.split(":");
            if (vs.length == 1){
                result.put(vs[0], null);
            }else {
                result.put(vs[0].trim(), vs[1].trim());
            }
        }
        return result;
    }

    public static Map<String, Object> processor(Map<String, String> source,
                                                EntryWrapper entryWrapper,
                                                Map<String, Object> totalSource){
        EntryExtenderDispatcher dispatcher = EntryCache.getDispatcher();
        Map<String, Object> result = new HashMap<>();
        source.forEach((key, entry) ->{
            int i = entry.indexOf(START_FLAG);
            if (i == -1){
                Class<?> type = getType(entryWrapper, key);
                if (entry != null){
                    result.put(key, BeanUtil.getDefaultValue(type, entry));
                }else {
                    result.put(key, BeanUtil.getTimeValue(type));
                }
            }else {
                // ....
                if (dispatcher != null){
                    result.put(key, dispatcher.handlerByMap(entry, totalSource));
                }
            }
        });
        return result;
    }
    protected static Class<?> getType(EntryWrapper entryWrapper, String name){
        Field field = entryWrapper.getFields().get(name);
        if (field == null){
            throw new RuntimeException("无法找到字段:" + name);
        }
        return field.getType();
    }

}
