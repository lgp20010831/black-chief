package com.black.core.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public enum ExcelBookType {


    XLS, XLSX;

    public static Workbook getBook(ExcelBookType type){
        switch (type){
            case XLS:
                return new HSSFWorkbook();
            case XLSX:
                return new XSSFWorkbook();
            default:
                throw new IllegalStateException("异常无法识别的 excel 类型");
        }
    }
}
