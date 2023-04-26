package com.black.sql;


import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;
import lombok.NonNull;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Consumer;


public class NativeQueryManager {


    public static Query createQuery(@NonNull String sql, String alias, Object... paramArray){
        Connection connection = ConnectionManagement.getConnection(alias);
        Query query = new Query(sql);
        for (int i = 1; i <= paramArray.length; i++) {
            query.setParam(i, paramArray[i - 1]);
        }
        query.setConnection(connection);
        query.setCallback(conn -> {
            if (!TransactionSQLManagement.isActivity(alias)){
                ConnectionManagement.closeCurrentConnection(alias);
            }
        });
        return query;
    }

    public static Query createQuery(@NonNull String sql, Connection connection, DataSource dataSource, Object... paramArray){
        Query query = new Query(sql);
        for (int i = 1; i <= paramArray.length; i++) {
            query.setParam(i, paramArray[i - 1]);
        }
        query.setConnection(connection);
        query.setCallback(conn -> {
            if (!DataSourceUtils.isConnectionTransactional(conn, dataSource)) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        });
        return query;
    }

    public static Query createAutonomousShutdownQuery(@NonNull String sql, Connection connection, Consumer<Connection> consumer, Object... paramArray){
        Query query = new Query(sql);
        for (int i = 1; i <= paramArray.length; i++) {
            query.setParam(i, paramArray[i - 1]);
        }
        query.setConnection(connection);
        query.setCallback(conn -> {
            if (consumer != null){
                consumer.accept(conn);
            }
        });
        return query;
    }
}
