package com.black.core.sql.code;

import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.factory.beans.InitMethod;
import com.black.core.yml.YmlConfigurationProperties;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class YmlDataSourceBuilder implements DataSourceBuilder{
    HikariDataSource dataSource;

    @Override
    @InitMethod
    @YmlConfigurationProperties(value = "spring.datasource", wriedFiled = true)
    public DataSource getDataSource() {
        if (dataSource == null){
            dataSource = new HikariDataSource();
            ApplicationConfigurationReader handler = ApplicationConfigurationReaderHolder.getReader();
            dataSource.setDriverClassName(handler.selectAttribute("spring.datasource.driver-class-name"));
            dataSource.setUsername(handler.selectAttribute("spring.datasource.username"));
            dataSource.setJdbcUrl(handler.selectAttribute("spring.datasource.url"));
            dataSource.setPassword(handler.selectAttribute("spring.datasource.password"));
            dataSource.setMaximumPoolSize(20);
        }
        return dataSource;
    }
}
