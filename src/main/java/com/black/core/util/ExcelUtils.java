package com.black.core.util;

import com.black.core.builder.ExcelReader;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.json.ReflexUtils;
import com.black.function.Function;
import com.black.utils.ReflexHandler;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("all")
public final class ExcelUtils {

    public static List<Map<String, Object>> readAll(MultipartFile file,
                                                    String sheet,
                                                    int titleRow){
        return readAll(file, sheet, titleRow, null);
    }


    public static List<Object> readRow(MultipartFile file, String sheetName, int index){
        try {
            return readRow(file.getInputStream(), file.getOriginalFilename(), bork -> bork.getSheet(sheetName), index);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Object> readRow(InputStream in, String fileName, int sheetIndex, int index){
        return readRow(in, fileName, bork -> bork.getSheetAt(sheetIndex), index);
    }

    public static List<Object> readRow(InputStream in, String fileName, String sheetName, int index){
        return readRow(in, fileName, bork -> bork.getSheet(sheetName), index);
    }

    public static List<Object> readRow(InputStream in, String fileName, Function<Workbook, Sheet> sheetFunction, int index){
        List<Object> result = new ArrayList<>();
        try {
            Workbook workBook = getWorkBook(in);
            Sheet xssfSheet = sheetFunction.apply(workBook);
            if (xssfSheet == null){
                throw new RuntimeException("can not find sheet");
            }
            Row row = xssfSheet.getRow(index);
            for (Cell cell : row) {
                result.add(getCellValue(cell));
            }
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
        return result;
    }

    public static List<Map<String, Object>> readAll(MultipartFile file,
                                                    String sheet,
                                                    int titleRow,
                                                    ExcelReaderListener readerListener){
        try {
            return readAll(workbook -> {
                return workbook.getSheet(sheet);
            }, file.getInputStream(), file.getOriginalFilename(), titleRow, readerListener);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> readAll(Function<Workbook, Sheet> sheetFunction,
                                                    InputStream in,
                                                    String fileName,
                                                    int titleRow,
                                                    ExcelReaderListener readerListener){
        try {
            Workbook hssfWorkbook = getWorkBook(in);
            Sheet xssfSheet = sheetFunction.apply(hssfWorkbook);
            if (xssfSheet == null){
                throw new RuntimeException("can not find sheet");
            }
            Row sheetRow = xssfSheet.getRow(titleRow);
            List<String> titleList = new ArrayList<>();
            for (Cell cell : sheetRow) {
                if (cell.getCellTypeEnum() == CellType.STRING) {
                    String title = cell.getStringCellValue();
                    if (readerListener != null){
                        if (!readerListener.postJoinTitle(titleList, title)) {
                            titleList.add(title);
                        }
                    }else {
                        titleList.add(title);
                    }
                }
            }
            if (readerListener != null){
                int before = titleList.size();
                readerListener.postTitles(titleList);
                if (before != titleList.size()){
                    throw new IllegalStateException("不应该修改标题集合的数量, 会导致数据错位");
                }
            }
            return readMap(titleList, xssfSheet, Av0.set(titleRow), readerListener);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> readAll(String sheet,
                                                    String path,
                                                    int titleRow){
        return readAll(sheet, path, titleRow, null);
    }

    public static List<Map<String, Object>> readAll(String sheet,
                                                    String path,
                                                    int titleRow, ExcelReaderListener readerListener){
        File file = new File(path);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return readAll(workbook -> {
                return workbook.getSheet(sheet);
            }, fileInputStream, file.getName(), titleRow, readerListener);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> readMap(List<String> titleList,
                                                    Sheet xssfSheet,
                                                    Set<Integer> excludeRows, ExcelReaderListener readerListener){

        Map<Integer, String> joinMap = new HashMap<>();
        for (int i = 0; i < titleList.size(); i++) {
            String name = titleList.get(i);
            joinMap.put(i, name);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            for (Row row : xssfSheet) {
                if (excludeRows.contains(row.getRowNum())){
                    continue;
                }
                if (readerListener != null){
                    if (readerListener.filteRow(row, joinMap)) {
                        continue;
                    }
                }
                Map<String, Object> map = new HashMap<>();
                for (Integer index : joinMap.keySet()) {
                    Cell cell = row.getCell(index);
                    if (cell == null){
                        continue;
                    }
                    String title = joinMap.get(index);
                    Object cellValue = getCellValue(cell);
                    if (readerListener != null){
                        if (!readerListener.postCellJoinMap(map, title, cellValue)) {
                            map.put(title, cellValue);
                        }
                    }else {
                        map.put(title, cellValue);
                    }
                }
                result.add(map);
            }
            if (readerListener != null){
                readerListener.postResultMapList(result);
            }
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <E> List<E> readObject(Class<E> clazz,
                                         String sheet,
                                         String path,
                                         int titleRow){
        try {

            FileInputStream fileInputStream = new FileInputStream(path);
            Workbook hssfWorkbook = getWorkBook(fileInputStream);
            Sheet xssfSheet = hssfWorkbook.getSheet(sheet);
            if (xssfSheet == null){
                throw new RuntimeException("can not find sheet: " + sheet);
            }
            Row sheetRow = xssfSheet.getRow(titleRow);
            List<String> titleList = new ArrayList<>();
            for (Cell cell : sheetRow) {
                if (cell.getCellTypeEnum() == CellType.STRING) {
                    titleList.add(cell.getStringCellValue());
                }
            }
            return readObject(clazz, titleList, xssfSheet, Av0.set(titleRow));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <E> List<E> readObject(Class<E> clazz,
                                         String sheet,
                                         String path,
                                         List<String> titleList,
                                         Set<Integer> excludeRows){
        try {

            FileInputStream fileInputStream = new FileInputStream(path);
            Workbook hssfWorkbook = getWorkBook(fileInputStream);
            Sheet xssfSheet = hssfWorkbook.getSheet(sheet);
            if (xssfSheet == null){
                throw new RuntimeException("can not find sheet: " + sheet);
            }
            return readObject(clazz, titleList, xssfSheet, excludeRows);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <E> List<E> readObject(Class<E> clazz,
                                         List<String> titleList,
                                         Sheet xssfSheet,
                                         Set<Integer> excludeRows){
        List<Field> fields = ReflexHandler.getAccessibleFields(clazz);
        Map<String, Field> targetMap = new HashMap<>();
        for (Field field : fields) {
            targetMap.put(field.getName(), field);
        }
        Map<Integer, Field> joinMap = new HashMap<>();
        for (int i = 0; i < titleList.size(); i++) {
            String name = titleList.get(i);
            if (targetMap.containsKey(name)){
                joinMap.put(i, targetMap.get(name));
            }
        }
        List<E> result = new ArrayList<>();
        try {
            for (Row row : xssfSheet) {
                if (excludeRows.contains(row.getRowNum())){
                    continue;
                }
                E instance = ReflexUtils.instance(clazz);
                for (Integer index : joinMap.keySet()) {
                    Cell cell = row.getCell(index);
                    if (cell == null){
                        continue;
                    }
                    Field field = joinMap.get(index);
                    if (field != null){
                        ReflexUtils.setValue(field, instance, convertCellValue(field, cell));
                    }
                }
                result.add(instance);
            }
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Object convertCellValue(Field field, Cell cell){
        Class<?> type = field.getType();
        Object value = null;
        if (cell != null){
            CellType cellType = cell.getCellTypeEnum();
            switch (cellType){
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case NUMERIC:
                    value = cell.getNumericCellValue();
                    break;
                case FORMULA:
                    value = cell.getCellFormula();
                    break;
            }
            if (value != null){
                if (!type.isAssignableFrom(value.getClass())){
                    TypeHandler handler = TypeConvertCache.initAndGet();
                    if (handler != null){
                        value = handler.convert(type, value);
                    }
                }
            }
        }
        return value;
    }

    public static Object getCellValue(Cell cell){
        Object value = null;

        if (cell != null){
            CellType cellType = cell.getCellTypeEnum();
            switch (cellType){
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case NUMERIC:
                    value = cell.getNumericCellValue();
                    break;
                case FORMULA:
                    value = cell.getCellFormula();
                    break;
            }
        }
        return value;
    }

    public static Workbook getWorkBook(InputStream inputStream) throws IOException {
        try {
            return WorkbookFactory.create(inputStream);
        } catch (InvalidFormatException e) {
            throw new IllegalStateException("文件格式不支持");
        }
    }

    public static Map<String, Sheet> getSheets(InputStream inputStream) throws IOException {
        Workbook workBook = getWorkBook(inputStream);
        Iterator<Sheet> sheetIterator = workBook.sheetIterator();
        Map<String, Sheet> sheets = new LinkedHashMap<>();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            sheets.put(sheet.getSheetName(), sheet);
        }
        return sheets;
    }

    public static boolean isNullCell(Row row, int index){
        Cell cell = row.getCell(index);
        if (cell == null) return true;
        return getCellValue(cell) == null;
    }

    public static void main(String[] args) {
        //System.out.println(readAll("Sheet1", "E:\\ideaSets\\SpringAutoThymeleaf\\src\\main\\resources\\khdr.xlsx", 0));
        System.out.println(ExcelReader.prepare("Sheet1")
                .relative("khdr.xlsx")
                        .listen(new ExcelReaderListener() {
                            @Override
                            public boolean filteRow(Row row, Map<Integer, String> titleMap) {
                                return isNullCell(row, 0);
                            }
                        })
                .read());
    }

}
