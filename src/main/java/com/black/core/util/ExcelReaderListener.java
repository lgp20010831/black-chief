package com.black.core.util;

import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Map;

public interface ExcelReaderListener {

    //如果返回 true 则表示已经处理不需要外部在此将标题加入数组
    default boolean postJoinTitle(List<String> titleList, String title){
        return false;
    }

    default void postTitles(List<String> titleList){

    }

    //返回true则过滤掉该行
    default boolean filteRow(Row row, Map<Integer, String> titleMap){
       return false;
    }

    default boolean postCellJoinMap(Map<String, Object> rowMap, String title, Object cellValue){
        return false;
    }

    default void postResultMapList(List<Map<String, Object>> resultList){

    }
}
