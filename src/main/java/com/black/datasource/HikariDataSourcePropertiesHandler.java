package com.black.datasource;

import com.black.config.AttributeUtils;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class HikariDataSourcePropertiesHandler {

    private final DataSource dataSource;

    public HikariDataSourcePropertiesHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void handleDataSource(DataSourceProperties properties){
        if (!(dataSource instanceof HikariDataSource)){
            return;
        }

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        hikariDataSource.setJdbcUrl(properties.getUrl());
        hikariDataSource.setPassword(properties.getPassword());
        hikariDataSource.setUsername(properties.getUsername());
        hikariDataSource.setDriverClassName(properties.getDriverClassName());
        HikariCpConfig hikariCpConfig = properties.getHikariCpConfig();
        AttributeUtils.mappingBean(hikariCpConfig, hikariDataSource);
    }
}
