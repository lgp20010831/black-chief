package com.black.project;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class ProjectUtils {

    public static <T> T findInMap(Map<String, Object> map, Class<T> type){
        if (map == null){
            return null;
        }

        for (Object value : map.values()) {
            if (value != null){
                Class<?> valueClass = value.getClass();
                if (type.isAssignableFrom(valueClass)){
                    return (T) value;
                }
            }
        }
        return null;
    }


    public static int getOneGenericClass(Class<?> type){
        return type.getTypeParameters().length;
    }

    public static void main(String[] args) {
        Class<List> listClass = List.class;
        System.out.println(Arrays.toString(listClass.getTypeParameters()));
    }
}
