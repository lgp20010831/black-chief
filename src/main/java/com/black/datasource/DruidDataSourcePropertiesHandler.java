package com.black.datasource;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DruidDataSourcePropertiesHandler {

    private final DataSource dataSource;

    public DruidDataSourcePropertiesHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void handleDataSource(DataSourceProperties properties){
        if (!(dataSource instanceof DruidDataSource)){
            return;
        }
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        druidDataSource.setUrl(properties.getUrl());
        druidDataSource.setUsername(properties.getUsername());
        druidDataSource.setPassword(properties.getPassword());
        druidDataSource.setDriverClassName(properties.getDriverClassName());
        DruidConfig druidConfig = properties.getDruidConfig();
        if (druidConfig != null){
            Properties druidConfigProperties = druidConfig.toProperties(druidConfig);
            druidDataSource.configFromPropety(druidConfigProperties);
        }
    }
}
