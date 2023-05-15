package com.black.table.data;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.table.*;
import com.black.template.jdbc.JdbcType;

import java.sql.*;
import java.util.*;

@SuppressWarnings("all")
public class Mysql8DatabaseMetadata extends AbstractDatabaseMetadata {

    public static final String CURRENT_DATA_BASE = "SELECT database();";

    public static final String CURRENT_TABLES = "show tables;";

    public static final String GET_TABLES = "select TABLE_NAME from information_schema.tables where table_schema = ?";

    public static final String GET_ALL_COLUMNS = "select TABLE_NAME,COLUMN_NAME,IS_NULLABLE,DATA_TYPE,COLUMN_TYPE from information_schema.COLUMNS where table_schema = ? and table_name = ?";

    public static final String MYSQL_WJ_SQL = "SELECT\n" +
            "    `column_name`, \n" +
            "    `referenced_table_schema` AS foreign_db, \n" +
            "    `referenced_table_name` AS foreign_table, \n" +
            "    `referenced_column_name`  AS foreign_column \n" +
            "FROM\n" +
            "    `information_schema`.`KEY_COLUMN_USAGE`\n" +
            "WHERE\n" +
            "    `constraint_schema` = SCHEMA()\n" +
            "AND\n" +
            "    `table_name` = 'your-table-name-here'\n" +
            "AND\n" +
            "    `referenced_column_name` IS NOT NULL\n" +
            "ORDER BY\n" +
            "    `column_name`;";


    @Override
    public String getCurrentDatabase(Connection connection) {
        ResultSet resultSet = null;
        try {

            resultSet = connection.createStatement().executeQuery(CURRENT_DATA_BASE);
            String result = null;
            while (resultSet.next()) {
                result = resultSet.getString(1);
            }
            return result;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeResultSet(resultSet);
        }
    }


    @Override
    public String getProductName() {
        return "MySQL";
    }

    @Override
    public TableMetadata getTableMetadata(String name, Connection connection) {
        if (tableMetadataCache.containsKey(name)) {
            return tableMetadataCache.get(name);
        }
        if (!StringUtils.hasText(name)){
            return null;
        }
        if (connection == null){
            return null;
        }
        try {
            String currentDatabase = getCurrentDatabase(connection);
            PreparedStatement statement = connection.prepareStatement(
                    "select * from information_schema.columns where table_name = '" + name + "' " +
                            "and table_schema = '"+ currentDatabase +"'"
            );
            ResultSet columns = statement.executeQuery();
            DefaultTableMetadata defaultTableMetadata = new DefaultTableMetadata(name);
            tableMetadataCache.put(name, defaultTableMetadata);
            Map<String, ColumnMetadata> columnMetadataMap = new HashMap<>();
            while (columns.next()) {
                //获取列名
                String columnName = columns.getString("COLUMN_NAME");
                //获取类型名称
                String typeName = columns.getString("DATA_TYPE");
                //获取备注
                String remarks = columns.getString("COLUMN_COMMENT");
                String extra = columns.getString("EXTRA");
                JdbcType jdbcType = JdbcType.getByName(typeName.toUpperCase());

                //构造字段对象
                DefaultColumnMetadata metadata = new DefaultColumnMetadata(columnName, -1, typeName, jdbcType == null ? -1 : jdbcType.getJdbcType());
                metadata.setMetadata(defaultTableMetadata);
                metadata.setRemarks(remarks);
                metadata.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                columnMetadataMap.put(columnName, metadata);
            }
            columns.close();
            defaultTableMetadata.registerColumnMetadata(columnMetadataMap.values());
            DatabaseMetaData metaData = connection.getMetaData();
            //更新表对象的列属性
            defaultTableMetadata.registerColumnMetadata(columnMetadataMap.values());

            //获取主键信息
            List<PrimaryKey> primaryKeyList = new ArrayList<>();
            ResultSet primaryKeys = metaData.getPrimaryKeys(currentDatabase, currentDatabase, name);
            while (primaryKeys.next()) {
                //获取主键名称
                String keyName = primaryKeys.getString("COLUMN_NAME");
                DefaultPrimaryKey primaryKey = new DefaultPrimaryKey(columnMetadataMap.get(keyName));
                primaryKeyList.add(primaryKey);
            }
            primaryKeys.close();
            //更新表对象主键信息
            defaultTableMetadata.resetPrimaryKey(primaryKeyList);

            //获取外键信息
            List<ForeignKey> foreignKeyList = new ArrayList<>();
            ResultSet importedKeys = metaData.getImportedKeys(currentDatabase, currentDatabase, name);
            while (importedKeys.next()) {

                //获取该外键关联其他表的主键名称
                String pkName = importedKeys.getString("PKCOLUMN_NAME");
                //获取该外键关联其他表的表名称
                String pkTableName = importedKeys.getString("PKTABLE_NAME");

                //获取该外键在当前表的表名称
                String fkTableName = importedKeys.getString("FKTABLE_NAME");
                //获取该外键在当前表的字段名称
                String fkColumnName = importedKeys.getString("FKCOLUMN_NAME");

                //通过名称拿到字段信息
                ColumnMetadata metadata = columnMetadataMap.get(fkColumnName);

                //判断, 一般不会触发
                if (name.equals(pkTableName)){
                    throw new SQLTablesException("name = fkTableName, name: " + name + " -- pkTableName: " + pkTableName);
                }
                //获取关联的其他表表信息
                TableMetadata tableMetadata = getTableMetadata(pkTableName, connection);
                //拿到关联表的主键字段对象
                PrimaryKey primaryKey = tableMetadata.getPrimaryKey(pkName);
                //构造外键对象
                DefaultForeignKey foreignKey = new DefaultForeignKey(metadata, primaryKey);
                foreignKeyList.add(foreignKey);
            }
            //更新表对象外键属性
            defaultTableMetadata.resetForeignKey(foreignKeyList);
            return defaultTableMetadata;
        } catch (SQLException e) {
            tableMetadataCache.remove(name);
            throw new SQLSException(e);
        }
    }

    @Override
    public Set<String> getTableNames(Connection connection) {
        try {

            ResultSet resultSet = connection.createStatement().executeQuery(CURRENT_TABLES);
            Set<String> tableNames = new HashSet<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(1));
            }
            SQLUtils.closeResultSet(resultSet);
            return tableNames;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
