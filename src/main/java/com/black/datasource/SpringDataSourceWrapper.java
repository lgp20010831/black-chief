package com.black.datasource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SpringDataSourceWrapper extends AbstractDataSource {

    private final DataSource dataSource;

    public SpringDataSourceWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return new SpringConnectionWrapper(dataSource, connection);
    }

}
