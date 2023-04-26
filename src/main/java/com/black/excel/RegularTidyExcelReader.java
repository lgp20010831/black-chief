package com.black.excel;

import com.black.core.log.IoLog;
import com.black.core.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all")
public class RegularTidyExcelReader implements ExcelReader{
    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof RegularTidyConfiguration;
    }

    @Override
    public Object read(Configuration configuration, Sheet sheet) throws Throwable {
        IoLog log = configuration.getLog();
        RegularTidyConfiguration tidyConfiguration = (RegularTidyConfiguration) configuration;
        LinkedBlockingQueue<RegularTidyExcelListener> listeners = tidyConfiguration.getListeners();

        //读出标题行
        Map<Integer, String> titleMap = readTitle(tidyConfiguration, sheet);
        for (RegularTidyExcelListener listener : listeners) {
            listener.finishTitle(titleMap, tidyConfiguration);
        }
        log.info("finish collect titles");
        log.info("title map is {}", titleMap);
        //获取内容行列表
        List<Row> contextRows = readContextRow(tidyConfiguration, sheet);
        for (RegularTidyExcelListener listener : listeners) {
            listener.collectContextRows(contextRows, tidyConfiguration);
        }
        log.info("collect context rows: {}", contextRows.size());
        //读出最终
        Object readResult = readContext(titleMap, contextRows, tidyConfiguration);
        log.info("read context result finish");
        for (RegularTidyExcelListener listener : listeners) {
            listener.finishContextDataResult((List<Map<String, Object>>) readResult, tidyConfiguration);
        }
        return readResult;
    }

    private Object readContext(Map<Integer, String> titleMap, List<Row> contextRows, RegularTidyConfiguration tidyConfiguration){
        LinkedBlockingQueue<RegularTidyExcelListener> listeners = tidyConfiguration.getListeners();
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Row contextRow : contextRows) {

            Map<String, Object> data = new LinkedHashMap<>();
            for (Integer titleAt : titleMap.keySet()) {
                String title = titleMap.get(titleAt);
                Cell cell = contextRow.getCell(titleAt);
                Object cellValue;
                try {
                    cellValue = ExcelUtils.getCellValue(cell);
                }catch (Throwable e){
                    if (tidyConfiguration.isAnalyticValueErrorThenSetNull()) {
                        cellValue = null;
                    }else {
                        throw new IllegalStateException(e);
                    }
                }
                for (RegularTidyExcelListener listener : listeners) {
                    cellValue = listener.joinData(title, cellValue, titleAt, cell, tidyConfiguration);
                }
                data.put(title, cellValue);
            }

            for (RegularTidyExcelListener listener : listeners) {
                listener.finishData(data, contextRow, tidyConfiguration);
            }
            dataList.add(data);
        }
        return dataList;
    }

    private Map<Integer, String> readTitle(RegularTidyConfiguration tidyConfiguration, Sheet sheet){
        LinkedBlockingQueue<RegularTidyExcelListener> listeners = tidyConfiguration.getListeners();
        int titleRow = tidyConfiguration.getTitleRow();
        Row row = sheet.getRow(titleRow);
        int titleRowIndent = tidyConfiguration.getTitleRowIndent();
        int titleRowLength = tidyConfiguration.getTitleRowLength();
        short lastCellNum = row.getLastCellNum();
        int cellLength = titleRowLength == -1 ? lastCellNum : titleRowLength;
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int i = titleRowIndent; i < cellLength; i++) {
            Cell cell = row.getCell(i);
            String title = Objects.toString(ExcelUtils.getCellValue(cell));
            for (RegularTidyExcelListener listener : listeners) {
                title = listener.joinTitle(title, i, cell, tidyConfiguration);
            }
            result.put(i, title);
        }
        return result;
    }

    private List<Row> readContextRow(RegularTidyConfiguration tidyConfiguration, Sheet sheet){
        int contextStartRow = tidyConfiguration.getContextStartRow();
        int contextEndRow = tidyConfiguration.getContextEndRow();
        List<Integer> includeRows = tidyConfiguration.getIncludeRows();
        List<Integer> skipRows = tidyConfiguration.getSkipRows();
        int titleRow = tidyConfiguration.getTitleRow();
        contextStartRow = contextStartRow == -1 ? titleRow + 1 : contextStartRow;
        int lastRowNum = sheet.getLastRowNum();
        contextEndRow = contextEndRow == -1 ? lastRowNum : (contextEndRow > lastRowNum ? lastRowNum : contextEndRow);
        List<Row> result = new ArrayList<>();
        for (int i = contextStartRow; i < contextEndRow; i++) {
            if (skipRows.contains(i)){
                continue;
            }
            Row row = sheet.getRow(i);
            result.add(row);
        }
        for (Integer includeRowAt : includeRows) {
            if(contextStartRow <= includeRowAt && includeRowAt >= contextEndRow){
                continue;
            }
            Row row = sheet.getRow(includeRowAt);
            result.add(row);
        }
        return result;
    }
}
