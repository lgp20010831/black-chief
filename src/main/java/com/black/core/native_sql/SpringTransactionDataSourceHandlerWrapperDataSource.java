package com.black.core.native_sql;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public class SpringTransactionDataSourceHandlerWrapperDataSource implements TransactionalDataSourceHandler{

    private final DataSource dataSource;

    public SpringTransactionDataSourceHandlerWrapperDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection openConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void closeConnection(Connection connection) {
        if (!DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
