package com.black.core.sql.code.sqls;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DatabaseCompanyLevel;
import com.black.core.sql.code.util.SQLUtils;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class MysqlDatabaseCompanyLevel implements DatabaseCompanyLevel {

    private static final String querySql = "select TABLE_NAME AS name from INFORMATION_SCHEMA.KEY_COLUMN_USAGE where REFERENCED_TABLE_NAME= ?";

    private Map<TableMetadata, List<TableMetadata>> cache = new ConcurrentHashMap<>();

    @Override
    public List<TableMetadata> getCompanyLevelTable(Configuration configuration, Connection connection) throws SQLException {
        TableMetadata masterTable = configuration.getTableMetadata();
        if (cache.containsKey(masterTable)){
            return cache.get(masterTable);
        }
        PreparedStatement statement = connection.prepareStatement(querySql);
        statement.setString(1, masterTable.getTableName());
        ResultSet resultSet = statement.executeQuery();
        List<TableMetadata> subMetadatas = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String tableName = resultSet.getString("name");
                TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
                subMetadatas.add(metadata);
            }
            cache.put(masterTable, subMetadatas);
        }finally {
            SQLUtils.closeResultSet(resultSet);
            SQLUtils.closeStatement(statement);
        }
        return subMetadatas;
    }
}
