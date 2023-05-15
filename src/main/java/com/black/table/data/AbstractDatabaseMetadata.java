package com.black.table.data;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.lock.LockType;
import com.black.core.util.StringUtils;
import com.black.table.*;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public abstract class AbstractDatabaseMetadata implements DatabaseMetadataObtainor {

    protected final Map<String, TableMetadata> tableMetadataCache = new ConcurrentHashMap<>();



    @Override
    public void lock(Connection connection, String tableName, LockType type) {
        throw new UnsupportedOperationException("lock");
    }

    @Override
    public void locks(LockType type, Connection connection, String... tableNames) {
        throw new UnsupportedOperationException("locks");
    }


    @Override
    public void clearCache() {
        tableMetadataCache.clear();
    }

    @Override
    public String getCurrentDatabase(Connection connection) {
        throw new UnsupportedOperationException("getCurrentDatabase");
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
            //先构造
            DefaultTableMetadata defaultTableMetadata = new DefaultTableMetadata(name);

            //先放入缓存, 方便过程中再次获取
            tableMetadataCache.put(name, defaultTableMetadata);
            DatabaseMetaData metaData = connection.getMetaData();
            //获取列信息
            ResultSet columns = metaData.getColumns(null, null, name, null);
            Map<String, ColumnMetadata> columnMetadataMap = new LinkedHashMap<>();
            while (columns.next()) {
                //获取列名
                String columnName = columns.getString("COLUMN_NAME");
                //获取类型名称
                String typeName = columns.getString("TYPE_NAME");
                //获取备注
                String remarks = columns.getString("REMARKS");
                //获取类型
                int type = columns.getInt("DATA_TYPE");
                //获取字段长度
                int size = columns.getInt("COLUMN_SIZE");
                //构造字段对象
                DefaultColumnMetadata metadata = new DefaultColumnMetadata(columnName, size, typeName, type);
                metadata.setMetadata(defaultTableMetadata);
                metadata.setRemarks(remarks);
                columnMetadataMap.put(columnName, metadata);
            }
            columns.close();
            //更新表对象的列属性
            defaultTableMetadata.registerColumnMetadata(columnMetadataMap.values());

            //获取主键信息
            List<PrimaryKey> primaryKeyList = new ArrayList<>();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, name);
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
            ResultSet importedKeys = metaData.getImportedKeys(null, null, name);
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
                    //throw new SQLTablesException("name = fkTableName, name: " + name + " -- pkTableName: " + pkTableName);
                    continue;
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

            //发送异常, 释放掉缓存
            tableMetadataCache.remove(name);
            throw new SQLTablesException("create table metadata fail: " + name, e);
        }
    }

    @Override
    public PreparedStatement prepare(String sql, boolean query, boolean allowScroll, Connection connection) throws SQLException {
        PreparedStatement statement;
        if (query){
            if (allowScroll){
                statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            }else {
                statement = connection.prepareStatement(sql);
            }
        }else {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }
        return statement;
    }

    @Override
    public List<TableMetadata> getTableMetadatas(Connection connection) {
        throw new UnsupportedOperationException("can not get all metadata");
    }

    @Override
    public Set<String> getTableNames(Connection connection) {
        try {
            Set<String> ts = new HashSet<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, "public", "%", null);
            while (resultSet.next()) {
                ts.add(resultSet.getString("TABLE_NAME"));
            }
            SQLUtils.closeResultSet(resultSet);
            return ts;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
