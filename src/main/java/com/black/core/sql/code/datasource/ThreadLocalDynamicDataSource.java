package com.black.core.sql.code.datasource;

import javax.sql.DataSource;
import java.util.Map;

public abstract class ThreadLocalDynamicDataSource extends DynamicDataSource{

    private static ThreadLocal<Object> ALIAS = new ThreadLocal<>();

    public ThreadLocalDynamicDataSource() {
        this(null);
    }

    public ThreadLocalDynamicDataSource(DataSource defaultDataSource) {
        super(defaultDataSource);
    }

    public static void setDataSource(Object key){
        ALIAS.set(key);
    }

    public static Object obtainDataSource() {
        return ALIAS.get();
    }

    public static void shutdown(){
        ALIAS.remove();
    }

    @Override
    public DataSource lookUp() {
        Map<Object, DataSource> dynamicDataSourceMap = getDynamicDataSourceMap();
        Object key = obtainDataSource();
        DataSource dataSource;
        if (key == null || (dataSource = dynamicDataSourceMap.get(key)) == null){
            dataSource = getDefaultDataSource();
        }
        return dataSource;
    }
}
