package com.black.excel.active;

import com.black.core.json.JsonUtils;
import com.black.core.sql.code.util.SQLUtils;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.throwable.IOSException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("all")
public class ActiveSheet {

    private final Sheet sheet;

    private volatile boolean invokeTitleMethod = false;

    private int titleIndex = 0;

    private int titleMergeLenght = 0;

    private final int rowStart, rowLimit,  columnStart,  columnEnd;

    private Map<String, String> titleEscape;

    private Map<Integer, String> indexEscape;

    private final Workbook workbook;

    private Function<Workbook, Sheet> function;

    public ActiveSheet(Function<Workbook, Sheet> function, Workbook workbook){
        this(function, 1, 1, workbook);
    }

    public ActiveSheet(Function<Workbook, Sheet> function, int rowStart, int columnStart, Workbook workbook){
        this(function, rowStart, -1, columnStart, -1, workbook);
    }

    public ActiveSheet(Function<Workbook, Sheet> function, int rowStart, int rowLimit, int columnStart, int columnEnd, Workbook workbook) {
        this.function = function;
        this.sheet = function.apply(workbook);
        this.rowStart = rowStart;
        this.rowLimit = rowLimit;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
        this.workbook = workbook;
        workbook.setForceFormulaRecalculation(true);
    }

    public void setTitleIndex(int titleIndex) {
        this.titleIndex = titleIndex;
    }

    public void setIndexEscape(Map<Integer, String> indexEscape) {
        this.indexEscape = indexEscape;
    }

    public void setTitleEscape(Map<String, String> titleEscape) {
        this.titleEscape = titleEscape;
    }

    public void setTitleMergeLenght(int titleMergeLenght) {
        this.titleMergeLenght = titleMergeLenght;
    }

    public void setInvokeTitleMethod(boolean invokeTitleMethod) {
        this.invokeTitleMethod = invokeTitleMethod;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public int getRowOffset(){
        return rowStart < 0 ? 0 : rowStart;
    }

    public int getColumnOffset(){
        return columnStart < 0 ? 0 : columnStart;
    }

    public int getRowIndex(int index){
        return getRowOffset() + index - 1;
    }

    public int getColumnIndex(int index){
        return getColumnOffset() + index - 1;
    }

    public void checkRowNum(int rowNum){
        if (rowLimit > 0 && getRowIndex(rowNum) >= rowLimit){
            throw new IllegalStateException("Operation row out of range");
        }
    }

    public void checkColumnNum(int columnNum){
        if (columnEnd > 0 && getColumnIndex(columnNum) >= rowLimit){
            throw new IllegalStateException("Operation column out of range");
        }
    }


    public List<String> getTitleList(){
        return getTitleList(0);
    }

    public List<String> getTitleList(int rowNum){
        checkRowNum(rowNum);
        titleIndex = rowNum;
        try {
            int rowIndex = getRowIndex(rowNum);
            Row row = sheet.getRow(rowIndex);
            List<String> titleList = new ArrayList<>();
            for (Cell cell : getCells(row)) {
                CellValueWrapper wrapper = ActiveExcelUtils.getMergeCellInfo(sheet, rowIndex, cell);
                if (wrapper.isMerge()) {
                    titleMergeLenght = Math.max(titleMergeLenght, wrapper.getHeight());
                }
                titleList.add(String.valueOf(wrapper.getValue()));
            }
            return titleList;
        }finally {
            invokeTitleMethod = true;
        }

    }


    protected List<Cell> getCells(Row row){
        return getCells(row, 0);
    }

    protected List<Cell> getCells(Row row, int min){
        List<Cell> cells = new ArrayList<>();
        int limit = columnEnd < 0 ? row.getLastCellNum() : columnEnd;
        limit = Math.max(min, limit);
        for (int i = columnStart - 1; i < limit; i++) {
            Cell cell = row.getCell(i);
            if (cell == null){
                cell = row.createCell(i);
            }
            cells.add(cell);
        }
        return cells;
    }

    public List<Row>  getRows(int start){
        return getRows(start, false);
    }

    public List<Row> getRows(int start, boolean filterNullRow){
        checkRowNum(start);
        int rowEnd = rowLimit < 0 ? sheet.getLastRowNum() : rowLimit;
        int rowIndex = getRowIndex(start);
        List<Row> rows = new ArrayList<>();
        for (int i = rowIndex; i < rowEnd; i++) {
            Row row = sheet.getRow(i);
            if (row == null ){
                if (filterNullRow){
                    continue;
                }
                row = sheet.createRow(i);
            }
            rows.add(row);
        }
        return rows;
    }


    public List<Map<String, Object>> getMergeDatas(){
        return getMergeDatas(true);
    }

    public List<Map<String, Object>> getMergeDatas(boolean filterNullRow){
        return getMergeDatas(titleIndex + titleMergeLenght + 1, filterNullRow);
    }

    public List<Map<String, Object>> getMergeDatas(int start, boolean filterNullRow){
        checkRowNum(start);
        List<Map<String, Object>> result = new ArrayList<>();

        List<String> titleList = getTitleList();
        int specification = titleIndex + titleMergeLenght + 1;
        start = Math.max(specification, start);
        List<Row> rows = getRows(start, filterNullRow);
        loop: for (Row row : rows) {
            List<Cell> cells = getCells(row, titleList.size());
            Map<String, Object> data = new LinkedHashMap<>();
            int i = 0;
            for (Cell cell : cells) {
                if (i >= titleList.size()){
                    continue loop;
                }
                String title = titleList.get(i++);
                Object cellValue = ActiveExcelUtils.getMergeCellValue(sheet, row.getRowNum(), cell);
                title = castTitle(title, cell.getColumnIndex());
                data.put(title, cellValue);
            }
            result.add(data);
        }
        return result;
    }

    protected String castTitle(String title, int index){
        index = index - getColumnOffset();
        if (titleEscape != null){
            title = titleEscape.get(title);
        }

        if (indexEscape != null){
            title = indexEscape.get(index);
        }
        return title;
    }


    public void writeData(Object source, boolean cover){
        writeData(source, cover ? titleIndex + titleMergeLenght + 1 :
                (rowLimit < 0 ? sheet.getLastRowNum() : rowLimit));
    }

    public void writeData(Object source, int start){
        checkRowNum(start);
        List<String> titleList = getTitleList();
        List<Object> datas = SQLUtils.wrapList(source);
        int specification = titleIndex + titleMergeLenght + 1;
        start = Math.max(specification, start);
        int rowIndex = getRowIndex(start);
        List<Row> rows = getRows(rowIndex);
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> data = JsonUtils.letJson(datas.get(i));
            Row row = i >= rows.size() ? sheet.createRow(rowIndex + i - 1) : rows.get(i);
            List<Cell> cells = getCells(row, titleList.size());
            for (int j = 0; j < titleList.size(); j++) {
                String title = titleList.get(j);
                Cell cell = cells.get(j);
                Object value = data.get(title);
                CellType cellType = cell.getCellTypeEnum();
                if (value == null && cellType == CellType.FORMULA){
                    continue;
                }
                ActiveExcelUtils.setCellValue(cell, value);
            }
        }

    }

    public ActiveSheet recalculate(){
        JHexByteArrayOutputStream outputStream = new JHexByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            JHexByteArrayInputStream inputStream = outputStream.getInputStream();
            Workbook newWrokbook = WorkbookFactory.create(inputStream);
            return new ActiveSheet(function,  rowStart, rowLimit, columnStart, columnEnd, newWrokbook);
        } catch (IOException e) {
            throw new IOSException(e);
        } catch (InvalidFormatException e) {
            throw new IllegalStateException(e);
        }
    }

}
