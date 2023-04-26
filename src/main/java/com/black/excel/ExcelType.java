package com.black.excel;

public enum ExcelType {

    REGULAR(RegularTidyConfiguration.class);

    Class<? extends Configuration> configType;


    ExcelType(Class<? extends Configuration> configurationClass) {
        configType = configurationClass;
    }

    public Class<? extends Configuration> getConfigType() {
        return configType;
    }
}
