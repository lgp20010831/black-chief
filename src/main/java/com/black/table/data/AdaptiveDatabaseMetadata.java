package com.black.table.data;

import com.black.core.sql.SQLSException;
import com.black.core.sql.lock.LockType;
import com.black.table.DatabaseMetadataObtainor;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class AdaptiveDatabaseMetadata implements DatabaseMetadataObtainor{

    private static DatabaseMetadataObtainor mysql8Obtainor;

    private static DatabaseMetadataObtainor postgresqlObtainor;

    private static SqlServerDatabaseMetadata sqlServerDatabaseMetadata;

    static {
        mysql8Obtainor = new Mysql8DatabaseMetadata();
        postgresqlObtainor = new PostgresqlDatabaseMetadata();
        sqlServerDatabaseMetadata = new SqlServerDatabaseMetadata();
    }

    @Override
    public TableMetadata getTableMetadata(String tableName, Connection connection) {
        return lookup(connection).getTableMetadata(tableName, connection);
    }

    @Override
    public List<TableMetadata> getTableMetadatas(Connection connection) {
        return lookup(connection).getTableMetadatas(connection);
    }

    @Override
    public Set<String> getTableNames(Connection connection) {
        return lookup(connection).getTableNames(connection);
    }

    @Override
    public String getCurrentDatabase(Connection connection) {
        return lookup(connection).getCurrentDatabase(connection);
    }

    @Override
    public void clearCache() {
        mysql8Obtainor.clearCache();
        postgresqlObtainor.clearCache();
    }

    @Override
    public void lock(Connection connection, String tableName, LockType type) {
        lookup(connection).lock(connection, tableName, type);
    }

    @Override
    public void locks(LockType type, Connection connection, String... tableNames) {
        lookup(connection).locks(type, connection, tableNames);
    }

    @Override
    public PreparedStatement prepare(String sql, boolean query, boolean allowScroll, Connection connection) throws SQLException {
        return lookup(connection).prepare(sql, query, allowScroll, connection);
    }

    private DatabaseMetadataObtainor lookup(Connection connection){
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            switch (productName){
                case "MySQL":
                    return mysql8Obtainor;
                case "PostgreSQL":
                    return postgresqlObtainor;
                case "Microsoft SQL Server":
                    return sqlServerDatabaseMetadata;
                default:
                    throw new UnsupportedOperationException("不支持当前数据库: " + productName);
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
