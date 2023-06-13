package com.black.fun_net;

import com.black.standard.TypeConvertStandard;
import org.apache.poi.ss.formula.functions.T;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-08 16:04
 */
@SuppressWarnings("all")
public interface RequestParamIndexHandler extends TypeConvertStandard {

    default Map<String, Object> arrangeParamMap(){
        return new LinkedHashMap<>();
    }

    default String index(int i){
        return index(i, String.class);
    }

    default <T> T index(int i, Class<T> type){
        Object[] array = arrangeParamMap().values().toArray(new Object[0]);
        Object value = array[i];
        return convert(value, type);
    }

    default String one(){
        return one(String.class);
    }

    default <T> T one(Class<T> type){
        return index(0, type);
    }

    default String two(){
        return two(String.class);
    }

    default <T> T two(Class<T> type){
        return index(1, type);
    }

    default String three(){
        return three(String.class);
    }

    default <T> T three(Class<T> type){
        return index(2, type);
    }

    default String four(){
        return four(String.class);
    }

    default <T> T four(Class<T> type){
        return index(3, type);
    }

    default String five(){
        return five(String.class);
    }

    default <T> T five(Class<T> type){
        return index(4, type);
    }

    default String six(){
        return six(String.class);
    }

    default <T> T six(Class<T> type){
        return index(5, type);
    }
}
