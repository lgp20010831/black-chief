package com.black.core.native_sql;

import com.black.core.sql.code.datasource.ConnectionManagement;

import java.sql.Connection;
import java.sql.SQLException;

public class MapSqlNativeBlendResolver implements NativeBlendSupportResolver{
    @Override
    public boolean support(String alias) {
        return "mapSql".equalsIgnoreCase(alias);
    }

    @Override
    public TransactionHandlerAndDataSourceHolder obtainDataSource(String value) throws Throwable {
        NativeDataSource nativeDataSource = new NativeDataSource(value);
        return new TransactionHandlerAndDataSourceHolder(
                new MapSqlTransactionDataSourceHandlerWrapperDataSource(nativeDataSource, value),
                nativeDataSource,
                value + "-NativeMapper"
        );
    }

    public static class NativeDataSource extends AbstractUnsupportDataSource{

        private final String mapSqlAlias;

        public NativeDataSource(String mapSqlAlias) {
            this.mapSqlAlias = mapSqlAlias;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return ConnectionManagement.getConnection(mapSqlAlias);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException("getConnection(username, password)");
        }
    }
}
