package com.black.datasource;

import com.black.core.sql.code.datasource.DynamicConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SpringConnectionWrapper extends DynamicConnection {

    private final DataSource dataSource;

    private final Connection connection;

    public SpringConnectionWrapper(DataSource dataSource, Connection connection) {
        super(null);
        this.dataSource = dataSource;
        this.connection = connection;
    }

    @Override
    public Connection loopUp() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (!DataSourceUtils.isConnectionTransactional(connection, dataSource)){
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
