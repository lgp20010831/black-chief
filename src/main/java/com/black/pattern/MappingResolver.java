package com.black.pattern;

import com.black.core.util.StringUtils;
import lombok.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//@MapMappings({"name -> supplierName", "code -> supplierCode", "now_age -> age"})
public class MappingResolver {

    private static MappingResolver resolver;

    public static MappingResolver getInstance() {
        if (resolver == null) resolver = new MappingResolver();
        return resolver;
    }

    private final Map<String, String> mappingCache = new ConcurrentHashMap<>();

    public Collection<Map<String, Object>> purify(Collection<Map<String, Object>> source){
        for (Map<String, Object> map : source) {
            purify(map);
        }
        return source;
    }

    public Map<String, Object> purify(Map<String, Object> source){
        Map<String, String> replaceMap = new HashMap<>();
        for (String key : source.keySet()) {
            String lowKey;
            if (mappingCache.containsKey(lowKey = getLowString(key))){
                replaceMap.put(key, lowKey);
            }
        }
        replaceMap.forEach((pk, nk) ->{
            source.put(mappingCache.get(nk), source.remove(pk));
        });
        return source;
    }

    public MappingResolver reset(){
        mappingCache.clear();
        return this;
    }

    public MappingResolver parse(AnnotatedElement element){
        MapMappings annotation = element.getAnnotation(MapMappings.class);
        if (annotation != null){
            return parse(annotation.division(), annotation.value());
        }
        return this;
    }

    public MappingResolver parse(String... entry){
        return parse("->", entry);
    }

    public MappingResolver parse(String division, String... entry){
        for (String e : entry) {
            if (e.contains(division)){
                String[] ss = e.split(division);
                if (ss.length != 2){
                    throw new IllegalStateException("entry split after should has 2, but " + ss.length);
                }
                register(ss[0].trim(), ss[1].trim());
            }
        }
        return this;
    }

    public MappingResolver register(String primary, String rear){
        if (!StringUtils.hasText(primary) || !StringUtils.hasText(rear))
            return this;

        mappingCache.put(getLowString(primary), rear);
        return this;
    }

    public String getLowString(@NonNull String primary){
        if (primary.contains("_")) {
            primary = StringUtils.linkStr(primary.split("_"));
        }
        return primary.toLowerCase();
    }
}
