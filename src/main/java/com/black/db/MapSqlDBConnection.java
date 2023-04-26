package com.black.db;

import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.SQLException;

public class MapSqlDBConnection extends AbstractDBConnection{

    private final String alias;

    public MapSqlDBConnection(){
        this("master");
    }

    public MapSqlDBConnection(@NonNull String alias) {
        this.alias = alias;
    }

    @Override
    protected Connection getRawConnection() throws SQLException {
        return ConnectionManagement.getConnection(alias);
    }

    @Override
    public void close() throws SQLException {
        ConnectionManagement.closeConnection(getConnection(), alias);
    }

    @Override
    public boolean isTransactionActivity() {
        return TransactionSQLManagement.isActivity(alias);
    }

    @Override
    public String getName() {
        return alias;
    }
}
