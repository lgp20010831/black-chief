package com.black.core.sql.code.sqls;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DatabaseCompanyLevel;
import com.black.table.TableMetadata;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CompanyLevelAdaptation implements DatabaseCompanyLevel {

    private final BeanFactory factory;

    public CompanyLevelAdaptation() {
        FactoryManager.init();
        factory = FactoryManager.getBeanFactory();
    }

    @Override
    public List<TableMetadata> getCompanyLevelTable(Configuration configuration, Connection connection) throws SQLException {
        DataSource dataSource = ConnectionManagement.getDataSource(configuration.getDatasourceAlias());
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        String driverClassName = hikariDataSource.getDriverClassName();
        switch (driverClassName){
            case "org.postgresql.Driver":
                return factory.getSingleBean(PostgresqlDatabaseCompanyLevel.class).getCompanyLevelTable(configuration, connection);
            case "com.mysql.cj.jdbc.Driver":
            case "com.mysql.jdbc.Driver":
                return factory.getSingleBean(MysqlDatabaseCompanyLevel.class).getCompanyLevelTable(configuration, connection);
            default:
                throw new SQLSException("不支持数据库驱动: " + driverClassName + " 获取外键关联关系");
        }
    }
}
