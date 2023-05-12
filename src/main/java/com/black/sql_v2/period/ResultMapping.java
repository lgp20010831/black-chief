package com.black.sql_v2.period;

import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-12 18:10
 */
@SuppressWarnings("all")
public interface ResultMapping<T> {


    List<Map<String, Object>> mapping(List<Map<String, Object>> resultList, T user);

}
