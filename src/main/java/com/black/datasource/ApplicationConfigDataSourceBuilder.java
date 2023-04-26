package com.black.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.config.SpringApplicationConfigAutoInjector;
import com.black.config.annotation.Omit;
import com.black.table.AbstractDataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;

@Setter @Getter
public class ApplicationConfigDataSourceBuilder extends AbstractDataSourceBuilder {

    @Omit
    private Class<? extends DataSource> dateSourceType = HikariDataSource.class;

    private DataSourceProperties propereties;

    @Override
    protected DataSource createDataSource() {
        DataSource instance = InstanceBeanManager.instance(dateSourceType, InstanceType.REFLEX_AND_BEAN_FACTORY);
        propereties = new DataSourceProperties();
        ConfiguringAttributeAutoinjector injector = new SpringApplicationConfigAutoInjector();
        injector.setParseMethod(true);
        injector.pourintoBean(propereties);
        return DataSourcePropertiesFactory.handleDataSource(instance, propereties);
    }

    public DataSourceProperties getPropereties() {
        return propereties;
    }

    @Override
    public void close() {
        DataSource dataSource = getDataSource();
        if (dataSource instanceof HikariDataSource){
            ((HikariDataSource) dataSource).close();
        }else if (dataSource instanceof DruidDataSource){
            ((DruidDataSource) dataSource).close();
        }
    }
}
