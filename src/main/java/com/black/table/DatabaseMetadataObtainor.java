package com.black.table;

import com.black.core.sql.lock.LockType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface DatabaseMetadataObtainor {

    TableMetadata getTableMetadata(String tableName, Connection connection);

    List<TableMetadata> getTableMetadatas(Connection connection);

    Set<String> getTableNames(Connection connection);

    String getCurrentDatabase(Connection connection);

    void clearCache();

    /**
     * 上锁, 对一个表进行上锁
     */
    void lock(Connection connection, String tableName, LockType type);

    /** 锁住多个表 */
    void locks(LockType type, Connection connection, String... tableNames);

    PreparedStatement prepare(String sql, boolean query, boolean allowScroll, Connection connection) throws SQLException;
}
