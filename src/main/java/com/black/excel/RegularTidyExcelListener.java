package com.black.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Map;

public interface RegularTidyExcelListener {

    default String joinTitle(String title, int at, Cell cell, RegularTidyConfiguration configuration){
        return title;
    }


    default void finishTitle(Map<Integer, String> map, RegularTidyConfiguration configuration){

    }

    default void collectContextRows(List<Row> rows, RegularTidyConfiguration configuration){

    }

    default Object joinData(String title, Object value, int at, Cell cell, RegularTidyConfiguration configuration){
        return value;
    }

    default void finishData(Map<String, Object> data, Row row, RegularTidyConfiguration configuration){

    }

    default void finishContextDataResult(List<Map<String, Object>> dataList, RegularTidyConfiguration configuration){

    }

}
