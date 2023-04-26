package com.black.core.builder;

import com.black.function.Function;
import com.black.core.util.Assert;
import com.black.core.util.ExcelReaderListener;
import com.black.core.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    public static ExcelReaderProxy prepare(){
        return prepare(null);
    }

    public static ExcelReaderProxy prepare(String sheet) {
        return prepare(sheet, 0);
    }

    public static ExcelReaderProxy prepare(String sheet, int titleRow){
        return new ExcelReaderProxy(sheet, titleRow);
    }

    public static class ExcelReaderProxy{

        private int titleRow;

        private InputStream fileIn;

        private String fileName;

        private ExcelReaderListener readerListener;

        private Function<Workbook, Sheet> sheetFunction = null;

        public ExcelReaderProxy(String sheet, int titleRow) {
            sheet(sheet);
            this.titleRow = titleRow;
        }

        public ExcelReaderProxy sheet(String s){
            if (s != null){
                sheetFunction = workbork -> {
                    return workbork.getSheet(s);
                };
            }
            return this;
        }

        public ExcelReaderProxy sheetIndex(Integer sheetIndex) {
            this.sheetFunction = workbork -> {
                return workbork.getSheetAt(sheetIndex);
            };
            return this;
        }

        public ExcelReaderProxy titleRow(int tr){
            titleRow = tr;
            return this;
        }

        public ExcelReaderProxy input(InputStream in){
            fileIn = in;
            return this;
        }

        public ExcelReaderProxy listen(ExcelReaderListener listener){
            this.readerListener = listener;
            return this;
        }

        public ExcelReaderProxy name(String name){
            fileName = name;
            return this;
        }

        public ExcelReaderProxy absolutely(String path){
            File file = new File(path);
            return file(file);
        }

        public ExcelReaderProxy part(MultipartFile file){
            fileName = file.getOriginalFilename();
            try {
                fileIn = file.getInputStream();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        public ExcelReaderProxy relative(String path){
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            Assert.notNull(url, "now find resource: " + path);
            if (!"file".equals(url.getProtocol())) {
                throw new IllegalStateException("current resource is not file:" + path);
            }
            return file(new File(url.getFile()));
        }

        public ExcelReaderProxy file(File file){
            try {
                fileIn = new FileInputStream(file);
                fileName = file.getName();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        public List<Map<String, Object>> read(){
            Assert.notNull(sheetFunction, "sheet function is null");
            Assert.notNull(fileIn, "fileIn is null");
            //Assert.notNull(fileName, "fileName is null");
            return ExcelUtils.readAll(sheetFunction, fileIn, fileName, titleRow, readerListener);
        }
    }

    public static class ExcelSheetReader{}
}
