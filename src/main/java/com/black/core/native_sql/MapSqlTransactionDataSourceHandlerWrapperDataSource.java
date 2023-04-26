package com.black.core.native_sql;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MapSqlTransactionDataSourceHandlerWrapperDataSource implements TransactionalDataSourceHandler{

    private final DataSource dataSource;

    private final String alias;

    public MapSqlTransactionDataSourceHandlerWrapperDataSource(DataSource dataSource, String alias) {
        this.dataSource = dataSource;
        this.alias = alias;
    }

    @Override
    public Connection openConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        if (!TransactionSQLManagement.isActivity(alias)){
            ConnectionManagement.closeConnection(connection, alias);
        }
    }
}
