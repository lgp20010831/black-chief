package com.black.excel.active;



import com.black.utils.TypeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

@SuppressWarnings("all")
public class ActiveExcelUtils {

    /**
     * 该方法用来将Excel中的ABCD列转换成具体的数据
     * @param column:ABCD列名称
     * @return integer：将字母列名称转换成数字
     * **/
    public static int excelColStrToNum(String column) {
        int num = 0;
        int result = 0;
        int length =column.length();
        for(int i = 0; i < length; i++) {
            char ch = column.charAt(length - i - 1);
            num = (int)(ch - 'A' + 1) ;
            num *= Math.pow(26, i);
            result += num;
        }
        return result;
    }

    /**
     * 该方法用来将具体的数据转换成Excel中的ABCD列
     * @param int：需要转换成字母的数字
     * @return column:ABCD列名称
     * **/
    public static String excelColIndexToStr(int columnIndex) {
        if (columnIndex <= 0) {
            return null;
        }
        String columnStr = "";
        columnIndex--;
        do {
            if (columnStr.length() > 0) {
                columnIndex--;
            }
            columnStr = ((char) (columnIndex % 26 + (int) 'A')) + columnStr;
            columnIndex = (int) ((columnIndex - columnIndex % 26) / 26);
        } while (columnIndex > 0);
        return columnStr;
    }

    //获取单元格有几列
    public static int getMergeColumNum(Cell cell, Sheet sheet) {
        int mergeSize = 1;
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress cellRangeAddress : mergedRegions) {
            if (cellRangeAddress.isInRange(cell)) {
                //获取合并的列数
                mergeSize = cellRangeAddress.getLastColumn() - cellRangeAddress.getFirstColumn() + 1;
                break;
            }
        }
        return mergeSize;
    }


    //获取单元格有几行
    public static int getMergeRowNum(Cell cell, Sheet sheet) {
        int mergeSize = 1;
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress cellRangeAddress : mergedRegions) {
            if (cellRangeAddress.isInRange(cell)) {
                mergeSize = cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow() + 1;
                break;
            }
        }
        return mergeSize;
    }

    public static CellValueWrapper getMergeCellInfo(Sheet sheet, int row, Cell cell){
        int column = cell.getColumnIndex();
        CellValueWrapper wrapper = new CellValueWrapper(sheet, cell);
        //获取合并单元格的总数，并循环每一个合并单元格，
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            //判断当前单元格是否在合并单元格区域内，是的话就是合并单元格
            if ((row >= firstRow && row <= lastRow) && (column >= firstColumn && column <= lastColumn)) {
                Row fRow = sheet.getRow(firstRow);
                Cell fCell = fRow.getCell(firstColumn);
                //获取合并单元格首格的值
                Object cellValue = getCellValue(fCell);
                wrapper.setMerge(true);
                wrapper.setValue(cellValue);
                wrapper.setHeight(lastRow - firstRow + 1);
                wrapper.setWidth(lastColumn - firstColumn + 1);
                wrapper.setFirstRowIndex(firstRow);
                wrapper.setLastRowIndex(lastRow);
                wrapper.setFirstColumnIndex(firstColumn);
                wrapper.setLastColumnIndex(lastColumn);
                return wrapper;
            }
        }
        //非合并单元格个返回空
        Object cellValue = getCellValue(cell);
        wrapper.setValue(cellValue);
        return wrapper;
    }

    public static Object getMergeCellValue(Sheet sheet, int row, Cell cell) {
        int column = cell.getColumnIndex();
        //获取合并单元格的总数，并循环每一个合并单元格，
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            //判断当前单元格是否在合并单元格区域内，是的话就是合并单元格
            if ((row >= firstRow && row <= lastRow) && (column >= firstColumn && column <= lastColumn)) {
                Row fRow = sheet.getRow(firstRow);
                Cell fCell = fRow.getCell(firstColumn);
                //获取合并单元格首格的值
                return getCellValue(fCell);
            }
        }
        //非合并单元格个返回空
        return getCellValue(cell);
    }

    public static Object getCellValue(Cell cell){
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType){
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                return cell.getNumericCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                }catch (IllegalStateException e){

                    try {
                        return cell.getStringCellValue();
                    }catch (IllegalStateException e1){
                        return cell.getCellFormula();
                    }
                }
            default:
                return cell.getStringCellValue();
        }
    }

    public static void setCellValue(Cell cell, Object value){
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType){
            case STRING:
                cell.setCellValue(TypeUtils.castToString(value));
                return;
            case BOOLEAN:
                cell.setCellValue(TypeUtils.castToBoolean(value));
                return;
            case NUMERIC:
                cell.setCellValue(TypeUtils.castToDouble(value));
                return;
            case FORMULA:
                try {
                    cell.setCellValue(TypeUtils.castToString(value));
                }catch (Throwable e){
                    cell.setCellValue(TypeUtils.castToDouble(value));
                }
                return;
            default:
                cell.setCellValue(TypeUtils.castToString(value));
                return;
        }
    }
}
