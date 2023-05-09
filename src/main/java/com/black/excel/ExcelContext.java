package com.black.excel;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.util.Assert;
import com.black.core.util.ExcelUtils;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExcelContext<T extends Configuration> {

    private final T configuration;

    private InputStream excelInputStream;

    private Function<Workbook, Sheet> sheetFunction;

    public ExcelContext(T configuration) {
        this.configuration = configuration;
    }

    public ExcelContext<T> biko(Consumer<T> configurationConsumer){
        if (configurationConsumer != null){
            configurationConsumer.accept((T) configuration);
        }
        return this;
    }

    public ExcelContext<T> sheet(Function<Workbook, Sheet> sheetFunction){
        this.sheetFunction = sheetFunction;
        return this;
    }

    public ExcelContext<T> in(@NonNull InputStream in){
        this.excelInputStream = in;
        return this;
    }

    public ExcelContext<T> file(@NonNull File file){
        try {
            this.excelInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public ExcelContext<T> bytes(@NonNull byte[] buf){
        excelInputStream = new JHexByteArrayInputStream(buf);
        return this;
    }

    public ExcelContext<T> part(MultipartFile multipartFile){
        try {
            excelInputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public Object read(){
        try {
            Workbook workBook = ExcelUtils.getWorkBook(excelInputStream);
            Sheet target;
            if (sheetFunction == null){
                target = workBook.getSheetAt(0);
            }else {
                target = sheetFunction.apply(workBook);
            }
            ExcelReader excelReader = configuration.getExcelReader();
            Assert.notNull(excelReader, "reader is null");
            if (!excelReader.support(configuration)) {
                throw new IllegalStateException("current reader is not support configuration:" + configuration);
            }

            return excelReader.read(configuration, target);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
