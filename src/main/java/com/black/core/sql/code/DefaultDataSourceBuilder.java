package com.black.core.sql.code;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NonNull;

import javax.sql.DataSource;

@Getter
public class DefaultDataSourceBuilder implements DataSourceBuilder {

    private final HikariDataSource dataSource;
    private final String username;
    private final String password;
    private final String driverClassName;
    private final String url;

    public DefaultDataSourceBuilder(@NonNull String username, @NonNull String password,
                                    @NonNull String driverClassName, @NonNull String url) {
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
