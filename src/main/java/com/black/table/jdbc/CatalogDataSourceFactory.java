package com.black.table.jdbc;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class CatalogDataSourceFactory {


    public static CatalogDataSource createDataSource(DataSource dataSource){
        return new DefaultCatalogDataSource(dataSource);
    }

    public static CatalogDataSource createHirDataSource(HikariDataSource dataSource){
        return new DefaultCatalogDataSource(dataSource);
    }


}
