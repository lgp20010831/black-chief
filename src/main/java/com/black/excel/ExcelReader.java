package com.black.excel;

import org.apache.poi.ss.usermodel.Sheet;

public interface ExcelReader {


    boolean support(Configuration configuration);


    Object read(Configuration configuration, Sheet sheet) throws Throwable;
}
