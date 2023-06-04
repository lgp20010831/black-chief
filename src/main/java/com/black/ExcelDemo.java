package com.black;

import com.black.core.util.Av0;
import com.black.core.util.ExcelUtils;
import com.black.excel.active.ActiveSheet;
import com.black.utils.ServiceUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("all")
public class ExcelDemo {

    public static void main(String[] args) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(ServiceUtils.getNonNullResource("生成.xlsx"));
        workbook.setForceFormulaRecalculation(true);
        testRead(workbook);
    }

    static void testRead(Workbook workbook){
        ActiveSheet sheet = new ActiveSheet(wb -> wb.getSheetAt(0), workbook);
        System.out.println(sheet.getMergeDatas());
        sheet.writeData(Av0.js("班级", "四班", "年龄", 19), true);
        sheet = sheet.recalculate();
        System.out.println(sheet.getMergeDatas());

    }

    static void testjs(Workbook workbook) throws IOException {
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(2);
        System.out.println(cell.getNumericCellValue());
        System.out.println(cell.getCellFormula());
        System.out.println(ExcelUtils.getCellValue(cell));
        System.out.println(cell.getCellTypeEnum());
        row.getCell(0).setCellValue(2);
        row.getCell(1).setCellValue(4);
        cell.setCellFormula(cell.getCellFormula());
        FileOutputStream stream = new FileOutputStream("F:\\ideaPros\\chief\\src\\main\\resources\\生成.xlsx");
        workbook.write(stream);
    }
}
