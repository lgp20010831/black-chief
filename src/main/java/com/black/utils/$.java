package com.black.utils;

import com.black.core.util.TextUtils;

import java.util.List;
import java.util.function.Function;

/**
 * @author 李桂鹏
 * @create 2023-06-20 17:09
 */
@SuppressWarnings("all")
public class $ {

    public static void print(Object obj, Object... params){
        String value = String.valueOf(obj);
        String content = TextUtils.parseContent(value, params);
        System.out.println(content);
    }

    public static <T> List<T> sort(List<T> list, Function<T, Object> function){
        return sort(list, function, true);
    }

    public static <T> List<T> sort(List<T> list, Function<T, Object> function, boolean asc){
       return ServiceUtils.sort(list, function, asc);
    }

    public static void main(String[] args) {
        $.print("hello world");
    }
}
