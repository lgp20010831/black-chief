package com.black.template.jdbc;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.core.util.Av0;
import com.black.table.ColumnMetadata;
import com.black.table.DefaultColumnMetadata;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class JdbcEnvironmentResolver {

    public static final String MAP_LEY = "source";

    private final DataSourceBuilder dataSourceBuilder;

    public JdbcEnvironmentResolver(@NonNull DataSourceBuilder dataSourceBuilder) {
        this.dataSourceBuilder = dataSourceBuilder;
    }

    public Map<String, Object> getTemplateSource(String tableName){
        JdbcSource jdbcSource = getSource(tableName);
        return Av0.of(MAP_LEY, jdbcSource);
    }

    public Connection getConnection(){
        DataSource dataSource = dataSourceBuilder.getDataSource();
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public JdbcSource getSource(@NonNull String tableName){
        DataSource dataSource = dataSourceBuilder.getDataSource();
        Connection connection;
        try {
             connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        TableMetadata metadata;
        try {
            metadata = TableUtils.getTableMetadata(tableName, connection);
            Assert.notNull(metadata, "表数据: " + tableName + " 不存在");
            Map<String, ColumnMetadata> columnMetadataMap = metadata.getColumnMetadataMap();
            for (String columnName : columnMetadataMap.keySet()) {
                ColumnMetadata old = columnMetadataMap.get(columnName);
                columnMetadataMap.replace(columnName, new JavaColumnMetadata((DefaultColumnMetadata) old));
            }
        }finally {
            SQLUtils.closeConnection(connection);
        }
        return new JdbcSource(metadata);
    }

    public void close(){
        DataSource dataSource = dataSourceBuilder.getDataSource();
        if (dataSource instanceof HikariDataSource){
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            hikariDataSource.close();
        }
    }
}
