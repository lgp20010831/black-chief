package com.black.core.sql.code;

import com.black.nio.code.Configuration;

import javax.sql.DataSource;

public interface GlobalSQLTectonicPeriodListener {

    //当上下文第一次加载数据源的时候
    //触发方法 loadDatasource
    default void postDataSource(DataSource dataSource, String alias){}

    //当上下文每初始化一次 sql 配置时
    default void postSQLConfiguration(Configuration configuration){}

    //当上下文关闭时
    default void turnShutdown(SQLApplicationContext context){}
}
