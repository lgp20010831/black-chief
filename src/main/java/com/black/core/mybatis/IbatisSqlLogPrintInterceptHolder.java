package com.black.core.mybatis;

public class IbatisSqlLogPrintInterceptHolder {

    private static IbatisSqlLogPrintIntercept sqlLogPrintIntercept;

    public static IbatisSqlLogPrintIntercept getIntercept(){
        if (sqlLogPrintIntercept == null){
            sqlLogPrintIntercept = new IbatisSqlLogPrintIntercept();
        }
        return sqlLogPrintIntercept;
    }
}
