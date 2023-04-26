
package com.black.table.company;

import com.black.core.sql.SQLSException;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

public class CompanyLevelDriver {

    private static final DatabaseCompanyLevel mysqlLevel = new MysqlDatabaseCompanyLevel();

    private static final DatabaseCompanyLevel postgresqlLevel = new PostgresqlDatabaseCompanyLevel();

    public static List<TableMetadata> getCompanyLevelTable(TableMetadata masterMetadata, Connection connection){
        try {
            return lookUp(connection).getCompanyLevelTable(masterMetadata, connection);
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    private static DatabaseCompanyLevel lookUp(Connection connection){
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            switch (productName){
                case "MySQL":
                    return mysqlLevel;
                case "PostgreSQL":
                    return postgresqlLevel;
                default:
                    throw new UnsupportedOperationException("不支持当前数据库: " + productName);
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

}
