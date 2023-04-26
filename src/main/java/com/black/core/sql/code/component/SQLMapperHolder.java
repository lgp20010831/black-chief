package com.black.core.sql.code.component;

public class SQLMapperHolder {

    static SQLMapComponent mapComponent;

    public static <T> T getMapper(Class<T> mapperClass){
        if(mapComponent != null){
            return mapComponent.getMapper(mapperClass);
        }
        return null;
    }

    public static Object getMapper(String name){
        if(mapComponent != null){
            return mapComponent.getMapper(name);
        }
        return null;
    }
}
