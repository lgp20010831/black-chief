package com.black.excel.active;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.util.Iterator;

/**
 * Description: Excel 单元格 测试
 * @author w
 * @version 1.0
 * @date 2022/8/23 14:14
 * @see https://blog.csdn.net/wutbiao/article/details/8696446 【源码这里】
 * 
  */
@SuppressWarnings("all")
public class ExcelCellCopy {
 
 
        /**
         * @Description:  复制单元格 cell 样式
         * @param workbook
         * @param fromStyle
         * @param toStyle
         * @return  void
         * @version v1.0
         * @author wu
         * @date 2022/8/24 22:39
         */
        public static void copyCellStyle(Workbook workbook, CellStyle fromStyle, CellStyle toStyle) {
            // 水平垂直对齐方式
            toStyle.setAlignment(fromStyle.getAlignmentEnum());
            toStyle.setVerticalAlignment(fromStyle.getVerticalAlignmentEnum());
 
            //边框和边框颜色
            toStyle.setBorderBottom(fromStyle.getBorderBottomEnum());
            toStyle.setBorderLeft(fromStyle.getBorderLeftEnum());
            toStyle.setBorderRight(fromStyle.getBorderRightEnum());
            toStyle.setBorderTop(fromStyle.getBorderTopEnum());
            toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
            toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
            toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
            toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());
 
            //背景和前景
            if(fromStyle instanceof  XSSFCellStyle){
                XSSFCellStyle xssfToStyle = (XSSFCellStyle) toStyle;
                xssfToStyle.setFillBackgroundColor(((XSSFCellStyle) fromStyle).getFillBackgroundColorColor());
                xssfToStyle.setFillForegroundColor(((XSSFCellStyle) fromStyle).getFillForegroundColorColor());
            }else {
                toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
                toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());
            }
 
            toStyle.setDataFormat(fromStyle.getDataFormat());
            toStyle.setFillPattern(fromStyle.getFillPatternEnum());
 
//    toStyle.setFont(fromStyle.getFont(null)); // 没有提供get 方法
            if (fromStyle instanceof HSSFCellStyle) {
                // 处理字体获取：03版 xls
                HSSFCellStyle style = (HSSFCellStyle) fromStyle;
                toStyle.setFont(style.getFont(workbook));
            } else if (fromStyle instanceof XSSFCellStyle) {
                // 处理字体获取：07版以及之后 xlsx
                XSSFCellStyle style = (XSSFCellStyle) fromStyle;
                toStyle.setFont(style.getFont());
            }
 
            toStyle.setHidden(fromStyle.getHidden());
            toStyle.setIndention(fromStyle.getIndention());//首行缩进
            toStyle.setLocked(fromStyle.getLocked());
            toStyle.setRotation(fromStyle.getRotation());//旋转
            toStyle.setWrapText(fromStyle.getWrapText());
        }
 
        /**
         * @Description: 复制 sheet 页
         * @param wb
         * @param fromSheet
         * @param toSheet
         * @param copyValueFlag
         * @return  void
         * @version v1.0
         * @author wu
         * @date 2022/8/24 22:40
         */
        public static void copySheet(Workbook wb, Sheet fromSheet, Sheet toSheet,  boolean copyValueFlag) {
            //合并区域处理
            mergerRegion(fromSheet, toSheet);
            Iterator<Row> rowIterator = fromSheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Row newRow = toSheet.createRow(row.getRowNum());
                copyRow(wb, row, newRow, copyValueFlag);
            }
        }
 
        /**
         * @Description: 复制 行row 数据
         * @return  void
         * @version v1.0
         * @author wu
         * @date 2022/8/24 22:40
         */
        public static void copyRow(Workbook wb, Row fromRow, Row toRow, boolean copyValueFlag) {
            short lastCellNum = fromRow.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                Cell fromCel = fromRow.getCell(i);
                Cell  destCell = toRow.createCell(i);
                copyCell(wb, fromCel, destCell, copyValueFlag);
            }
        }
 
 
        /**
         * @Description: 复制原有sheet的合并单元格到新创建的sheet
         * <br> 处理 sheet 页的合并区域
         * @param fromSheet
         * @param toSheet
         * @return  void
         * @version v1.0
         * @author wu
         * @date 2022/8/24 22:43
         */
        public static void mergerRegion(Sheet fromSheet, Sheet toSheet) {
            int sheetMergerCount = fromSheet.getNumMergedRegions();
            for (int i = 0; i < sheetMergerCount; i++) {
                CellRangeAddress mergedRegion = fromSheet.getMergedRegion(i);
                toSheet.addMergedRegion(mergedRegion);
            }
        }
 
        /**
         * @Description: 复制 单元格 cell
         * @param wb
         * @param srcCell
         * @param distCell
         * @param copyValueFlag  是否包含内容 - true 包含内容复制
         * @return  void
         * @version v1.0
         * @author wu
         * @date 2022/8/24 22:43
         */
        public static void copyCell(Workbook wb, Cell srcCell, Cell distCell, boolean copyValueFlag) {
 
            CellStyle newstyle= wb.createCellStyle();
            copyCellStyle(wb,srcCell.getCellStyle(), newstyle);
//            distCell.setEncoding(srcCell.getEncoding());
            // 复制样式
            distCell.setCellStyle(newstyle);
            //评论
            if (srcCell.getCellComment() != null) {
                distCell.setCellComment(srcCell.getCellComment());
            }
 
            // 不同数据类型处理
            CellType cellTypeEnum = srcCell.getCellTypeEnum();
            if (cellTypeEnum != CellType.FORMULA){
                distCell.setCellType(cellTypeEnum);
            }

            if (copyValueFlag) {
                if (cellTypeEnum == CellType.NUMERIC) {
                    if (HSSFDateUtil.isCellDateFormatted(srcCell)) {
                        distCell.setCellValue(srcCell.getDateCellValue());
                    } else {
                        distCell.setCellValue(srcCell.getNumericCellValue());
                    }
                } else if (cellTypeEnum == CellType.STRING) {
                    distCell.setCellValue(srcCell.getRichStringCellValue());
                } else if (cellTypeEnum == CellType.BLANK) {
                    // nothing21
                } else if (cellTypeEnum == CellType.BOOLEAN) {
                    distCell.setCellValue(srcCell.getBooleanCellValue());
                } else if (cellTypeEnum == CellType.ERROR) {
                    distCell.setCellErrorValue(srcCell.getErrorCellValue());
                } else if (cellTypeEnum == CellType.FORMULA) {
                    distCell.setCellFormula(srcCell.getCellFormula());
                } else { // nothing29
                }
            }
        }
}