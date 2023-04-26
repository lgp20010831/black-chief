package com.black.core.sql.code.datasource;

import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.sql.code.SqlConfigurationAware;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.util.Assert;
import com.black.swagger.AliasAware;
import com.black.table.AbstractDataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;

public class PropertiesHikariDataSource extends AbstractDataSourceBuilder implements SqlConfigurationAware, AliasAware {

    public static final String DRIVER_CLASS = "driver-class";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String CT = "connection-timeout";
    public static final String MT = "minimum-idle";
    public static final String MP = "maximum-poolsize";
    public static final String ML = "max-lifetime";
    public static final String IT = "idle-timeout";
    public static final String CTQ = "test-query";
    public static final String RO = "read-only";

    private GlobalSQLConfiguration sqlConfiguration;
    private String alias;

    public PropertiesHikariDataSource(){}

    public PropertiesHikariDataSource(String alias){
        this.alias = alias;
    }

    @Override
    protected DataSource createDataSource() {
        Assert.notNull(alias, "unset alias");
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        Map<String, String> map = reader.groupQueryForGlobal(alias, true);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(map.get(DRIVER_CLASS));
        dataSource.setJdbcUrl(map.get(URL));
        dataSource.setUsername(map.get(USERNAME));
        dataSource.setPassword(map.get(PASSWORD));
        if (map.containsKey(CT)){
            dataSource.setConnectionTimeout(Long.parseLong(map.get(CT)));
        }
        if (map.containsKey(MT)){
            dataSource.setMinimumIdle(Integer.parseInt(map.get(MT)));
        }
        if (map.containsKey(MP)){
            dataSource.setMaximumPoolSize(Integer.parseInt(map.get(MP)));
        }
        if (map.containsKey(ML)){
            dataSource.setMaxLifetime(Long.parseLong(map.get(ML)));
        }
        if (map.containsKey(IT)){
            dataSource.setIdleTimeout(Long.parseLong(map.get(IT)));
        }
        if (map.containsKey(CTQ)){
            dataSource.setConnectionTestQuery(map.get(CTQ));
        }
        if (map.containsKey(RO)){
            dataSource.setReadOnly(Boolean.parseBoolean(map.get(RO)));
        }
        return dataSource;
    }

    @Override
    public void setConfiguration(GlobalSQLConfiguration configuration) {
        sqlConfiguration = configuration;
        setAlias(configuration.getDataSourceAlias());
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public void close() {
        ((HikariDataSource)getDataSource()).close();
    }
}
