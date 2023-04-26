package com.black.excel;

public class ExcelBuilder {


    public static <T extends RegularTidyConfiguration> ExcelContext<T> build(ExcelType type){
        Configuration configuration;
        switch (type){
            case REGULAR:
                configuration = new RegularTidyConfiguration();
                break;
            default:
                throw new IllegalStateException("ill excel type:" + type);
        }
        return new ExcelContext(configuration, type.getConfigType());
    }


}
