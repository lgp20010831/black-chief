package com.black.core.sql.code.datasource;

public class Dynamics {


    public static void cutDataSource(Object loopKey){
        ThreadLocalDynamicDataSource.setDataSource(loopKey);
    }

    public static void closeDataSource(){
        ThreadLocalDynamicDataSource.shutdown();
    }

    public static Object loopKey(){
        return ThreadLocalDynamicDataSource.obtainDataSource();
    }
}
