package com.black.excel.active;

import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

@SuppressWarnings("all") @Data
public class CellValueWrapper {

    private final Sheet sheet;
    private final Cell cell;
    private boolean merge = false;
    private int height = 1;
    private int width = 1;
    private Object value;
    private int firstRowIndex;
    private int lastRowIndex;
    private int firstColumnIndex;
    private int lastColumnIndex;
    public CellValueWrapper(Sheet sheet, Cell cell) {
        this.sheet = sheet;
        this.cell = cell;
    }
}
