package com.black.table;

import com.black.pattern.GlobalConnectionManagement;
import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.lock.LockType;
import com.black.core.util.Assert;
import com.black.table.data.AdaptiveDatabaseMetadata;
import com.zaxxer.hikari.HikariDataSource;
import lombok.NonNull;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TableUtils {

    private static final Map<String, Set<String>> tableNameCaches = new ConcurrentHashMap<>();

    private static DatabaseMetadataObtainor databaseMetadataObtainor = new AdaptiveDatabaseMetadata();

    public static void clearCache(){
        databaseMetadataObtainor.clearCache();
    }

    public static Set<String> getCurrentTables(String alias){
        return ConnectionManagement.employConnection(alias, connection -> {
            return getCurrentTables(alias, connection);
        });
    }

    public static synchronized PreparedStatement prepare(String sql, boolean query, boolean allowScroll, Connection connection) throws SQLException {
        return databaseMetadataObtainor.prepare(sql, query, allowScroll, connection);
    }

    public static synchronized void lock(Connection connection, String tableName, LockType type){
        databaseMetadataObtainor.lock(connection, tableName, type);
    }

    public static synchronized void locks(LockType type, Connection connection, String... tableNames){
        databaseMetadataObtainor.locks(type, connection, tableNames);
    }

    public static synchronized Set<String> getCurrentTables(String alias, @NonNull Connection connection){
        return tableNameCaches.computeIfAbsent(alias, na -> {
            return databaseMetadataObtainor.getTableNames(connection);
        });
    }

    public static synchronized TableMetadata getNonNullTableMetadata(String name, Connection connection){
        TableMetadata tableMetadata = getTableMetadata(name, connection);
        Assert.notNull(tableMetadata, "can not find tableMetadata: " + name);
        return tableMetadata;
    }

    public static synchronized TableMetadata getTableMetadata(String name, Connection connection){
        return databaseMetadataObtainor.getTableMetadata(name, connection);
    }

    public static synchronized String getCurrentDatabaseName(Connection connection){
        return databaseMetadataObtainor.getCurrentDatabase(connection);
    }

    public static void main(String[] args) throws SQLException {
        HikariDataSource dataSource = (HikariDataSource) new YmlDataSourceBuilder().getDataSource();
        GlobalConnectionManagement.registerDataSource(dataSource);
        Connection connection = GlobalConnectionManagement.getConnection();
//        transfer("select * from byc", "ayc", connection, Av0.of("a1_id", "id"));
        //System.out.println(getCurrentTables("ayc", connection));
        dataSource.close();

    }

}
