package com.black.core.util;

import lombok.NonNull;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

public class ExcelWritor {

    public static String key_prefix = "k";

    public static ExcelWritorBuilder prepare(String sheetName){
        return new ExcelWritorBuilder(sheetName);
    }

    public static class ExcelWritorBuilder{

        private final String sheetName;
        private Map<String, String> titleMap;
        private List<Map<String, Object>> dataMap;
        private Map<String, Object> env = new HashMap<>();
        private ExcelBookType bookType = ExcelBookType.XLSX;
        private ExcelWritorListener listener;

        public ExcelWritorBuilder(String sheetName) {
            this.sheetName = sheetName;
        }

        public ExcelWritorBuilder titleMap(Map<String, String> titleMap) {
            this.titleMap = titleMap;
            return this;
        }

        public ExcelWritorBuilder dataMap(List<Map<String, Object>> dataMap) {
            this.dataMap = dataMap;
            return this;
        }

        public ExcelWritorBuilder titleList(List<String> titleList){
            int index = 0;
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            for (String title : titleList) {
                map.put(key_prefix + index++, title);
            }
            this.titleMap = map;
            return this;
        }

        public ExcelWritorBuilder dataList(List<List<Object>> dataList){
            int index = 0;
            List<Map<String, Object>> dataMap = new ArrayList<>();
            for (List<Object> list : dataList) {
                LinkedHashMap<Object, Object> lmap = new LinkedHashMap<>();
                for (Object val : list) {
                    lmap.put(key_prefix + index++, val);
                }
            }
            this.dataMap = dataMap;
            return this;
        }

        public ExcelWritorBuilder env(Map<String, Object> env) {
            this.env = env;
            return this;
        }

        public ExcelWritorBuilder bookType(ExcelBookType bookType) {
            this.bookType = bookType;
            return this;
        }

        public ExcelWritorBuilder listener(ExcelWritorListener listener) {
            this.listener = listener;
            return this;
        }

        public byte[] writeAndGetBytes(){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            write(outputStream);
            return outputStream.toByteArray();
        }

        public void write(OutputStream out){
            writeMapExcel(sheetName, titleMap, dataMap, env, bookType, listener, out);
        }
    }



    public static void writeListExcel(String sheetName, List<String> titleList,
                                      List<List<Object>> dataList,
                                      Map<String, Object> env,
                                      ExcelBookType bookType,
                                      ExcelWritorListener listener,
                                      OutputStream out){
        int index = 0;
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String title : titleList) {
            map.put(key_prefix + index++, title);
        }
        index = 0;
        List<Map<String, Object>> dataMap = new ArrayList<>();
        for (List<Object> list : dataList) {
            LinkedHashMap<Object, Object> lmap = new LinkedHashMap<>();
            for (Object val : list) {
                lmap.put(key_prefix + index++, val);
            }
        }
        writeMapExcel(sheetName, map, dataMap, env, bookType, listener, out);
    }


    public static void writeMapExcel(@NonNull String sheetName,
                                     @NonNull Map<String, String> titleMap,
                                     @NonNull List<Map<String, Object>> dataMap,
                                     Map<String, Object> env,
                                     @NonNull ExcelBookType bookType,
                                     ExcelWritorListener listener,
                                     @NonNull OutputStream out){
        try {
            Workbook workbook = ExcelBookType.getBook(bookType);
            Sheet sheet = workbook.createSheet(sheetName);
            int titleRowIndex = 0;
            if (listener != null){
                titleRowIndex = listener.beforeWriteTitle(sheet, env);
            }
            //创建标题行
            Row titleRow = sheet.createRow(titleRowIndex);
            int titleCellIndex = 0;
            for (String key : titleMap.keySet()) {
                String titleName = titleMap.get(key);
                Cell titleRowCell = titleRow.createCell(titleCellIndex++);
                if (listener != null){
                    titleName = listener.beforeWriteTitleCell(titleRowCell, titleCellIndex - 1, key, titleName, env);
                }
                titleRowCell.setCellType(CellType.STRING);
                titleRowCell.setCellValue(titleName);
            }
            int dataIndex = titleRowIndex + 1;
            if (listener != null){
                dataIndex = listener.beforeWriteDataRows(sheet, titleRowIndex, env, dataIndex);
            }

            for (Map<String, Object> data : dataMap) {
                Row dataRow = sheet.createRow(dataIndex);
                int cellIndex = 0;
                for (String key : titleMap.keySet()) {
                    Object value = data.get(key);
                    Cell dataCell = dataRow.createCell(cellIndex);
                    CellType cellType = getType(value);
                    if (listener != null){
                        value = listener.beforeWriteDataCell(dataCell, cellIndex, key, value, env);
                    }
                    addCellValue(value, dataCell);
                    cellIndex++;
                }
                dataIndex++;
            }
            workbook.write(out);
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }

    }

    public static CellType getType(Object value){
        if (value == null){
            return CellType.BLANK;
        }
        Class<?> valueClass = value.getClass();
        if (value instanceof String){
            return CellType.STRING;
        }
        if (value instanceof Number){
            return CellType.NUMERIC;
        }

        if (value instanceof Boolean || boolean.class.equals(valueClass)){
            return CellType.BOOLEAN;
        }
        return CellType.STRING;
    }

    public static void addCellValue(Object value, Cell cell){
        CellType cellType = getType(value);
        cell.setCellType(cellType);
        switch (cellType){
            case BLANK:
                cell.setCellValue("");
                return;
            case BOOLEAN:
                cell.setCellValue(Boolean.parseBoolean(value.toString()));
                return;
            case NUMERIC:
                cell.setCellValue(Double.parseDouble(value.toString()));
                return;
            case STRING:
            case _NONE:
            case ERROR:
            case FORMULA:
                cell.setCellValue(value.toString());
        }
    }
}
