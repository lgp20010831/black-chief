package com.black.core.sql.code;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapperXmlApplicationContext extends AnnotationMapperSQLApplicationContext{

    private Map<String, ElementWrapper> cache = new ConcurrentHashMap<>();

    public MapperXmlApplicationContext(@NonNull GlobalSQLConfiguration configuration) {
        super(configuration);
    }

    public ElementWrapper findXmlBind(String id){
        return cache.get(id);
    }

    public void addBindMapper(String id, ElementWrapper ew){
        cache.put(id, ew);
    }

    public boolean save(String id){
        return cache.containsKey(id);
    }

    public Map<String, ElementWrapper> getCache() {
        return cache;
    }
}
