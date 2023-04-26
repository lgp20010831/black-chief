package com.black.table.company;

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
public class PostgresqlDatabaseCompanyLevel implements DatabaseCompanyLevel {

    private static final String querySql = "SELECT\n" +
            "tc.constraint_name,\n" +
            "tc.table_name, \n" +
            "kcu.column_name,\n" +
            "ccu.table_name AS foreign_table_name, \n" +
            "ccu.column_name AS foreign_column_name,\n" +
            "tc.is_deferrable,\n" +
            "tc.initially_deferred\n" +
            "FROM\n" +
            "information_schema.table_constraints AS tc\n" +
            "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name\n" +
            "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name\n" +
            "where constraint_type = 'FOREIGN KEY' AND ccu.table_name= ?;";

    private Map<TableMetadata, List<TableMetadata>> cache = new ConcurrentHashMap<>();

    @Override
    public List<TableMetadata> getCompanyLevelTable(TableMetadata masterTable, Connection connection) throws SQLException {
        if (cache.containsKey(masterTable)){
            return cache.get(masterTable);
        }
        PreparedStatement statement = connection.prepareStatement(querySql);
        statement.setString(1, masterTable.getTableName());
        ResultSet resultSet = statement.executeQuery();
        List<TableMetadata> subMetadatas = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
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
