package com.black.table.data;

import com.black.core.sql.SQLSException;
import com.black.core.sql.lock.LockType;
import com.black.core.util.Assert;
import com.black.table.DatabaseMetadataObtainor;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class AdaptiveDatabaseMetadata implements DatabaseMetadataObtainor{


    private static final LinkedBlockingQueue<DatabaseMetadataObtainor> metadataObtainors = new LinkedBlockingQueue<>();

    static {
        metadataObtainors.add(new Mysql8DatabaseMetadata());
        metadataObtainors.add(new PostgresqlDatabaseMetadata());
        metadataObtainors.add(new SqlServerDatabaseMetadata());
    }

    public static void addDatabase(DatabaseMetadataObtainor... metadataObtainor){
        metadataObtainors.addAll(Arrays.asList(metadataObtainor));
    }

    public static LinkedBlockingQueue<DatabaseMetadataObtainor> getMetadataObtainors() {
        return metadataObtainors;
    }

    @Override
    public String getProductName() {
        throw new UnsupportedOperationException("unsupport");
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
        for (DatabaseMetadataObtainor obtainor : metadataObtainors) {
            obtainor.clearCache();
        }
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
            Assert.notNull(productName, "current product is not support getDatabaseProductName()");
            DatabaseMetadataObtainor target = null;
            for (DatabaseMetadataObtainor obtainor : metadataObtainors) {
                if (productName.equals(obtainor.getProductName())){
                    target = obtainor;
                    break;
                }
            }
            if (target == null){
                throw new UnsupportedOperationException("不支持当前数据库: " + productName);
            }
            return target;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
