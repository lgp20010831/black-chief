package com.black.core.convert.v2;

/**
 * @author 李桂鹏
 * @create 2023-05-17 9:44
 */
@SuppressWarnings("all")
public interface BringRearTypeHandler {


    <T> T convert(Object val, Class<T> type);

}
