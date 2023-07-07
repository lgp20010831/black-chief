package com.black.excel.active;


import com.black.core.json.JsonUtils;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.throwable.IOSException;
import com.black.utils.TypeUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.print.ServiceUI;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("all") @Getter @Setter
public class ActiveSheet {

    private final Sheet sheet;

    private volatile boolean invokeTitleMethod = false;

    private int dataNumberOfrowsOccupied = 1;

    private int titleIndex = 0;

    private String distinctKey;

    private int titleMergeLenght = 0;

    private boolean trimTitle = false;

    private Consumer<CellStyle> configCellStyle;

    private boolean appendMergeTitle = true;

    private String appendMergeTitleFlage = "-";

    private final int rowStart, rowLimit,  columnStart,  columnEnd;

    private Map<String, String> titleEscape;

    private Map<Integer, String> indexEscape;

    private final Workbook workbook;

    private Function<Workbook, Sheet> function;

    public static ActiveSheet create(InputStream inputStream, Function<Workbook, Sheet> function){
        return create(inputStream, function, 0, 0);
    }

    public static ActiveSheet create(InputStream inputStream, Function<Workbook, Sheet> function,
                                     int rowStart, int columnStart){
        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            return new ActiveSheet(function, rowStart, columnStart, workbook);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public ActiveSheet(Function<Workbook, Sheet> function, Workbook workbook){
        this(function, 0, 0, workbook);
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
        return getRowOffset() + index;
    }

    public int getColumnIndex(int index){
        return getColumnOffset() + index;
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

    protected void processCell(Cell cell){
        CellStyle cellStyle = cell.getCellStyle();
        if (configCellStyle != null){
            configCellStyle.accept(cellStyle);
        }
    }

    public void writeCellValue(int row, String column, Object value){
        Cell cell = getCell(row, column);
        ActiveExcelUtils.setCellValue(cell, value);
    }

    public void writeCellValue(int row, int column, Object value){
        Cell cell = getCell(row, column);
        ActiveExcelUtils.setCellValue(cell, value);
    }

    public Object getCellValue(int row, String column){
        Cell cell = getCell(row, column);
        return ActiveExcelUtils.getCellValue(cell);
    }

    public Object getCellValue(int row, int column){
        Cell cell = getCell(row, column);
        return ActiveExcelUtils.getCellValue(cell);
    }

    public Cell getCell(int row, String column){
        return getCell(row, ActiveExcelUtils.excelColStrToNum(column));
    }

    public Cell getCell(int row, int column){
        checkRowNum(row);
        int rowIndex = getRowIndex(row);
        Row r = getRow(rowIndex);
        return getCell(r, column);
    }

    public List<String> getTitleList(){
        return getTitleList(titleIndex);
    }

    public List<String> getTitleList(int rowNum){
        return new ArrayList<>(getTitleInfo(rowNum).keySet());
    }

    public Map<String, CellValueWrapper> getTitleInfo(){
        return getTitleInfo(titleIndex);
    }

    public Map<String, CellValueWrapper> getTitleInfo(int rowNum){
        checkRowNum(rowNum);
        titleIndex = rowNum;
        try {
            int rowIndex = getRowIndex(rowNum);
            Row row = sheet.getRow(rowIndex);
            Map<String, CellValueWrapper> titleList = new LinkedHashMap<>();
            List<Cell> cells = getCells(row, row.getLastCellNum());
            for (int i = 0; i < cells.size(); i++) {
                Cell cell = cells.get(i);
                List<CellValueWrapper> wrappers = getTitle(rowIndex, cell, -1, 0);
                i = i + getWrapperLenght(wrappers) - 1;
                for (CellValueWrapper wrapper : wrappers) {
                    String title = String.valueOf(wrapper.getValue());
                    if (isTrimTitle()){
                        title = title.trim();
                        title = title.replace("\n", "");
                    }
                    titleList.put(title, wrapper);
                }
            }

            return titleList;
        }finally {
            invokeTitleMethod = true;
        }
    }

    protected int getWrapperLenght(List<CellValueWrapper> wrappers){
        int i = 0;
        for (CellValueWrapper valueWrapper : wrappers) {
            if (valueWrapper.isMerge()){
                i = i + valueWrapper.getWidth();
            }else {
                i++;
            }
        }
        return i;
    }

    protected List<CellValueWrapper> getTitle(int rowIndex, Cell cell, int parentWidth, int parentHeight){
        CellValueWrapper wrapper = ActiveExcelUtils.getMergeCellInfo(sheet, rowIndex, cell);
        List<CellValueWrapper> result = new ArrayList<>();
        if (wrapper.isMerge()) {
            int width = wrapper.getWidth();
            int height = wrapper.getHeight();
            titleMergeLenght = Math.max(titleMergeLenght, height + parentHeight);
            if (height < titleMergeLenght && height + parentHeight < titleMergeLenght){
                if (parentWidth == -1 || width < parentWidth){
                    for (int i = 0; i < width; i++) {
                        int next = rowIndex + height;
                        Row row = getRow(next);
                        Cell nextCell = row.getCell(cell.getColumnIndex() + i);
                        //说明底下还有标题
                        List<CellValueWrapper> nextWrapper = getTitle(next, nextCell, width, height + parentHeight);
                        i = i + getWrapperLenght(nextWrapper) - 1;
                        String parentTitle = String.valueOf(wrapper.getValue());
                        for (CellValueWrapper valueWrapper : nextWrapper) {
                            String sonValue = String.valueOf(valueWrapper.getValue());
                            sonValue = parentTitle + appendMergeTitleFlage + sonValue;
                            valueWrapper.setValue(sonValue);
                        }
                        result.addAll(nextWrapper);
                    }
                }

            }else {
                result.add(wrapper);
            }
        }else {
            result.add(wrapper);
        }
        return result;
    }

    protected List<Cell> getCells(Row row){
        return getCells(row, 0);
    }

    protected List<Cell> getCells(Row row, int min){
        List<Cell> cells = new ArrayList<>();
        int limit = columnEnd < 0 ? row.getLastCellNum() : columnEnd;
        limit = Math.max(min, limit);
        for (int i = columnStart; i < limit; i++) {
            Cell cell = row.getCell(i);
            if (cell == null){
                cell = row.createCell(i);
            }
            cells.add(cell);
        }
        return cells;
    }

    public Cell getCell(Row row, int i){
        Cell cell = row.getCell(i);
        if (cell == null){
            cell = row.createCell(i);
        }
        return cell;
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

    public Row getRow(int i){
        Row row = sheet.getRow(i);
        if (row == null ){
            row = sheet.createRow(i);
        }
        return row;
    }


    public List<Map<String, Object>> getDatas(){
        return getDatas(true);
    }

    public List<Map<String, Object>> getDatas(boolean filterNullRow){
        return getDatas(titleIndex + titleMergeLenght - 1, filterNullRow);
    }

    public List<Map<String, Object>> getDatas(int start, boolean filterNullRow){
        checkRowNum(start);
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, CellValueWrapper> titleInfo = getTitleInfo();
        List<String> titleList = new ArrayList<>(titleInfo.keySet());
        int specification = titleIndex + titleMergeLenght -1;
        start = Math.max(specification, start);
        List<Row> rows = getRows(start, filterNullRow);
        loop: for (Row row : rows) {
            List<Cell> cells = getCells(row);
            Map<String, Object> data = new LinkedHashMap<>();
            int i = 0;
            for (int j = 0; j < cells.size(); j++) {
                Cell cell = cells.get(j);
                if (i >= titleList.size()){
                    continue loop;
                }
                String title = titleList.get(i++);
                CellValueWrapper wrapper = titleInfo.get(title);
                if (wrapper.isMerge()){
                    j = j + wrapper.getWidth() -1;
                }
                Object cellValue = ActiveExcelUtils.getMergeCellValue(sheet, row.getRowNum(), cell);
                title = castTitle(title, cell.getColumnIndex());
                data.put(title, cellValue);
            }
            result.add(data);
        }
        return result;
    }


    public List<Map<String, Object>> getMergeDatas(){
        return getMergeDatas(true);
    }

    public List<Map<String, Object>> getMergeDatas(boolean filterNullRow){
        return getMergeDatas(titleIndex + titleMergeLenght - 1, filterNullRow);
    }

    public List<Map<String, Object>> getMergeDatas(int start, boolean filterNullRow){
        checkRowNum(start);
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, CellValueWrapper> titleInfo = getTitleInfo();
        List<String> titleList = new ArrayList<>(titleInfo.keySet());
        int specification = titleIndex + titleMergeLenght -1;
        start = Math.max(specification, start);
        List<Row> rows = getRows(start, filterNullRow);
        int maxHeight = 1;
        int commonTitleHight = 1;
        int accumulation = 0;
        Map<String, Object> mergeStartData = null;
        Set<Object> distinctSet = new HashSet<>();
        loop: for (Row row : rows) {
            List<Cell> cells = getCells(row);
            Map<String, Object> data = mergeStartData == null ? new LinkedHashMap<>() : mergeStartData;
            int i = 0;
            int mergeHandlerHight = 0;
            boolean across = false;
            Map<String, Object> sonBody = null;
            for (int j = 0; j < cells.size(); j++) {
                Cell cell = cells.get(j);
                if (i >= titleList.size()){
                    continue loop;
                }
                String title = titleList.get(i++);
                CellValueWrapper wrapper = titleInfo.get(title);
                if (wrapper.isMerge()){
                    j = j + wrapper.getWidth() -1;
                    commonTitleHight = Math.max(wrapper.getHeight(), commonTitleHight);
                }
                CellValueWrapper mergeCellInfo = ActiveExcelUtils.getMergeCellInfo(sheet, row.getRowNum(), cell);

                int height = mergeCellInfo.getHeight();
                maxHeight = Math.max(height, maxHeight);
                if(mergeStartData == null){
                    mergeStartData = data;
                }
                if (wrapper.getHeight() < commonTitleHight){
                    across = true;
                    int index = title.lastIndexOf(getAppendMergeTitleFlage());
                    if (index != -1){
                        String key = title.substring(0, index);
                        title = title.substring(index + 1);
                        List<Map<String, Object>> list = (List<Map<String, Object>>) mergeStartData.computeIfAbsent(key, k -> new ArrayList<>());
                        if (sonBody == null){
                            sonBody = new LinkedHashMap<>();
                            list.add(sonBody);
                        }
                    }
                }

                Object cellValue = mergeCellInfo.getValue();
                String castTitle = castTitle(title, cell.getColumnIndex());
                if (sonBody != null){
                    sonBody.put(castTitle == null ? title : castTitle, cellValue);
                }else {
                    data.put(castTitle == null ? title : castTitle, cellValue);
                }
                if (sonBody != null){
                    mergeHandlerHight = Math.max(mergeCellInfo.getHeight(), mergeHandlerHight);
                }
            }
            accumulation += mergeHandlerHight;
            if (accumulation >= maxHeight){
                mergeStartData = null;
                accumulation = 0;
            }
            if (!across || mergeStartData == null){
                boolean save = true;
                if (getDistinctKey() != null){
                    Object val = data.get(getDistinctKey());
                    if (distinctSet.contains(val)){
                        save = false;
                    }
                    distinctSet.add(val);
                }
                if (save)
                    result.add(data);
            }
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

    protected int getCateringTitleLenght(Map<String, CellValueWrapper> titleInfo){
        int i = 0;
        for (CellValueWrapper wrapper : titleInfo.values()) {
            if (wrapper.isMerge()){
                i = i + wrapper.getWidth();
            }else {
                i++;
            }
        }
        return i;
    }

    public void writeData(Object source, boolean cover, int titleIndex){
        this.titleIndex = titleIndex;
        writeData(source, cover ? titleIndex + titleMergeLenght :
                (rowLimit < 0 ? sheet.getLastRowNum() : rowLimit), titleIndex);
    }

    public void writeData(Object source, boolean cover){
        writeData(source, cover ? titleIndex + titleMergeLenght :
                (rowLimit < 0 ? sheet.getLastRowNum() : rowLimit), titleIndex);
    }

    public void writeData(Object source, int start, int titleIndex){
        checkRowNum(start);
        Map<String, CellValueWrapper> titleInfo = getTitleInfo(titleIndex);
        List<String> titleList = new ArrayList<>(titleInfo.keySet());
        List<Object> datas = SQLUtils.wrapList(source);
        int specification = titleIndex + titleMergeLenght;
        start = Math.max(specification, start);
        int rowIndex = getRowIndex(start);
        List<Row> rows = getRows(rowIndex);
        int lenght = getCateringTitleLenght(titleInfo);
        int k = 0;
        for (int i = 0; i < datas.size(); i++) {

            Map<String, Object> data = JsonUtils.letJson(datas.get(i));
            Row row = k >= rows.size() ? sheet.createRow(rowIndex + k ) :
                    rows.get(k);
            List<Cell> cells = getCells(row, lenght);
            int cellIndex = getColumnIndex(0);
            for (int j = 0; j < titleList.size(); j++) {
                try {
                    String title = titleList.get(j);
                    CellValueWrapper wrapper = titleInfo.get(title);
                    Cell cell = cells.get(cellIndex);
                    if(wrapper.isMerge()){
                        cellIndex = cellIndex + wrapper.getWidth() - 1;
                        int rowNum = row.getRowNum();
                        int columnIndex = cell.getColumnIndex();
                        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum + dataNumberOfrowsOccupied - 1, columnIndex, columnIndex + wrapper.getWidth() - 1));
                    }
                    String key = castTitle(title, cellIndex);
                    Object value = data.get(key);
                    CellType cellType = cell.getCellTypeEnum();
                    if (value == null && cellType == CellType.FORMULA){
                        continue;
                    }
                    processCell(cell);
                    ActiveExcelUtils.setCellValue(cell, value);
                }finally {
                    cellIndex ++;
                }

            }
            k = k + dataNumberOfrowsOccupied - 1;
            k++;
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

    public void writeFile(String path){
        try {
            getWorkbook().write(new FileOutputStream(path));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void write(OutputStream outputStream){
        try {
            getWorkbook().write(outputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String standardLine;

    public List<Map<String, Object>> readDatas(int start, boolean filter, boolean filterNullStandard){

        checkRowNum(start);
        List<Row> rows = getRows(start, filter);
        Map<String, CellValueWrapper> titleInfo = getTitleInfo();
        List<String> titleList = new ArrayList<>(titleInfo.keySet());
        //结果
        List<Map<String, Object>> result = new ArrayList<>();
        //为了保证正确读到一行数据, 所以要设置标准标题名, 该标题下的行宽就为标准
        Map<String, Object> current = null;
        //标准高
        int standardHeight = -1;
        int standardTitleHeight = -1;
        int readHeight = 0;
        loop:for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            List<Cell> cells = getCells(row);
            int t = 0;
            if (current == null){
                current = new LinkedHashMap<>();
            }
            boolean save = true;
            Map<String, Map<String, Object>> sonBodyMap = new LinkedHashMap<>();
            //要根据设置的标准获取一条数据的标准
            for (int k = 0; k < cells.size(); k++) {
                Cell cell = cells.get(k);
                if (t >= titleList.size()){
                    continue loop;
                }
                String title = titleList.get(t++);
                if (standardLine == null){
                    standardLine = title;
                }

                CellValueWrapper wrapper = titleInfo.get(title);
                if (wrapper.isMerge()){
                    k = k + wrapper.getWidth() -1;
                }
                CellValueWrapper cellWrapper = ActiveExcelUtils.getMergeCellInfo(sheet, row.getRowNum(), cell);
                if(standardHeight == -1){
                    standardHeight = cellWrapper.getHeight();
                    standardTitleHeight = wrapper.getHeight();
                }

                /*
                    3   2  read = 1
                        1
                        2
                        确保不会重复读
                 */
                if (cellWrapper.isMerge() && cellWrapper.getFirstRowIndex() != row.getRowNum()){
                    continue;
                }
                String key = null;
                List<Map<String, Object>> sonArray = null;
                //获取当前标题高度
                int currentTitleHeight = wrapper.getHeight();
                if (currentTitleHeight < standardTitleHeight){
                    int index = title.lastIndexOf(getAppendMergeTitleFlage());
                    if (index != -1){
                        key = title.substring(0, index);
                        title = title.substring(index + 1);
                        sonArray = (List<Map<String, Object>>) current.computeIfAbsent(key, ky -> new ArrayList<>());

                    }

                }
                Object cellValue = cellWrapper.getValue();
                if (filterNullStandard && title.equalsIgnoreCase(standardLine)){
                    if (!StringUtils.hasText(TypeUtils.castToString(cellValue))){
                        save = false;
                    }
                }

                String castTitle = castTitle(title, cell.getColumnIndex());
                String titleKey = castTitle == null ? title : castTitle;
                if (sonArray != null){
                    Map<String, Object> map = sonBodyMap.computeIfAbsent(key, ky -> new LinkedHashMap<>());
                    map.put(titleKey, cellValue);
                    if (!sonArray.contains(map)){
                        sonArray.add(map);
                    }
                }else {
                    current.put(titleKey, cellValue);

                }

            }
            //每次读完单元格, 将 readHeight + 1 表示读完一行
            readHeight++;
            if(readHeight >= standardHeight){
                if (save){
                    //表示读完标准行高, 可以将数据收集起来了
                    result.add(current);
                }
                current = null;
                readHeight = 0;
                standardHeight = -1;
            }

        }
        return result;
    }

}
