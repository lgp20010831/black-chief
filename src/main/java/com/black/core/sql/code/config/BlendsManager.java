package com.black.core.sql.code.config;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.BlendsPattern;
import com.black.core.sql.code.parse.BlendObjects;
import com.black.core.sql.code.parse.CharParser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlendsManager {


    public static final Map<Method, Map<String, BlendObjects>> blendsMap = new ConcurrentHashMap<>();


    public static Map<String, BlendObjects> getAndParse(MethodWrapper mw){
        Method method = mw.getMethod();
        return blendsMap.computeIfAbsent(method, m ->{
            BlendsPattern annotation = mw.getAnnotation(BlendsPattern.class);
            Map<String, BlendObjects> map = new HashMap<>();
            if (annotation != null){
                List<BlendObjects> blendObjects = CharParser.parseBlends(annotation.value());
                return CharParser.toMaps(blendObjects);
            }
            return map;
        });
    }
}
