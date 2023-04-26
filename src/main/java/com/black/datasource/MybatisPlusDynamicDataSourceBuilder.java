package com.black.datasource;

import com.black.config.SpringApplicationConfigAutoInjector;
import com.black.table.AbstractDataSourceBuilder;
import com.black.utils.CollectionUtils;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;

public class MybatisPlusDynamicDataSourceBuilder extends AbstractDataSourceBuilder {

    private MybatisDynamicDataSourceProperties properties;

    @Override
    protected DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        SpringApplicationConfigAutoInjector injector = new SpringApplicationConfigAutoInjector();
        injector.setParseMethod(true);
        properties = new MybatisDynamicDataSourceProperties();
        injector.pourintoBean(properties);
        DataSourceProperties sourceProperties = null;
        Map<String, DataSourceProperties> datasourceMap = properties.getDatasourceMap();
        if (datasourceMap.isEmpty()){
            throw new IllegalStateException("datasource map is null");
        }
        if (datasourceMap.size() == 1){
            sourceProperties = CollectionUtils.firstElement(datasourceMap.values());
        }
        String primary = properties.getPrimary();
        if (primary == null){
            sourceProperties = CollectionUtils.firstElement(datasourceMap.values());
        }else {
            sourceProperties = datasourceMap.get(primary);
        }
        if (sourceProperties == null){
            throw new IllegalStateException("can not find datasource properties:" + datasourceMap);
        }
        return DataSourcePropertiesFactory.handleDataSource(dataSource, sourceProperties);
    }


    @Override
    public void close() {
        ((HikariDataSource)getDataSource()).close();
    }
}
