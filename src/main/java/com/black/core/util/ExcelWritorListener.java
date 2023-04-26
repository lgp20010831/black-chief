package com.black.core.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Map;

public interface ExcelWritorListener {


    default int beforeWriteTitle(Sheet sheet, Map<String, Object> env)  throws Throwable{
        return 0;
    }

    default String beforeWriteTitleCell(Cell cell, int cellIndex, String key, String value, Map<String, Object> env){
        return value;
    }

    default int beforeWriteDataRows(Sheet sheet, int titleRowIndex, Map<String, Object> env, int currentDataIndex){
        return currentDataIndex;
    }

    default Object beforeWriteDataCell(Cell cell, int cellIndex, String key, Object value, Map<String, Object> env){
        return value;
    }

}
