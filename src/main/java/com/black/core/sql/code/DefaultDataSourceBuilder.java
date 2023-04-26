package com.black.core.sql.code;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import javax.sql.DataSource;

@Getter
public class DefaultDataSourceBuilder implements DataSourceBuilder {

    private final HikariDataSource dataSource;
    private final String username;
    private final String password;
    private final String driverClassName;
    private final String url;

    public DefaultDataSourceBuilder(String username, String password, String driverClassName, String url) {
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.url = url;
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setPassword(password);
        dataSource.setUsername(username);
        dataSource.setDriverClassName(driverClassName);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
