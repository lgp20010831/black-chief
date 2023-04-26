package com.black.datasource;



import javax.sql.DataSource;

public class DataSourcePropertiesFactory {

    public static DataSource handleDataSource(DataSource dataSource, DataSourceProperties properties){
        if (isDruidDataSourceEnvironment()) {
            DruidDataSourcePropertiesHandler handler = new DruidDataSourcePropertiesHandler(dataSource);
            handler.handleDataSource(properties);
        }

        if (isHikariDataSourceEnvironment()){
            HikariDataSourcePropertiesHandler handler = new HikariDataSourcePropertiesHandler(dataSource);
            handler.handleDataSource(properties);
        }

        return dataSource;
    }

    private static boolean isDruidDataSourceEnvironment(){
        try {
            Class.forName("com.alibaba.druid.pool.DruidDataSource");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isHikariDataSourceEnvironment(){
        try {
            Class.forName("com.zaxxer.hikari.HikariDataSource");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
