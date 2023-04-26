package com.black.core.sql.code.session;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.datasource.DataSourceCacheWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.run.RunSqlProcessor;
import com.black.core.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class CommonSession implements SqlSession{

    final DataSource dataSource;

    Connection currentConnection;

    List<String> sqlStack = new ArrayList<>();

    LinkedBlockingQueue<Object> resultStack = new LinkedBlockingQueue<>();

    public CommonSession(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void write(String sql) {
        sqlStack.add(sql);
    }

    @Override
    public Connection getConnection() {
        if (currentConnection == null){
            try {
                return currentConnection = dataSource.getConnection();
            } catch (SQLException e) {
                throw new SQLSException(e);
            }
        }else {
            if (dataSource instanceof DataSourceCacheWrapper || !ConnectionManagement.checkConnection(currentConnection)) {
                currentConnection = null;
                return getConnection();
            }
            return currentConnection;
        }
    }

    @Override
    public void writeAndFlush(String sql) {
        write(sql);
        flush();
    }

    @Override
    public void flush() {
        try {
            Connection connection = getConnection();
            for (String sql : sqlStack) {
                if (RunSqlProcessor.getSqlType(StringUtils.removeFrontSpace(sql)) == SQLMethodType.QUERY){
                    ResultSet resultSet = SQLUtils.runQuery(sql, connection);
                    List<Map<String, Object>> result = SQLUtils.parseResultSet(resultSet);
                    resultStack.add(result);
                }else {
                    resultStack.add(SQLUtils.runSql(sql, connection));
                }
            }
        }finally {
            sqlStack.clear();
        }
    }

    @Override
    public Object poll() {
        return resultStack.poll();
    }

    @Override
    public void close() {
        if (currentConnection != null){
            SQLUtils.closeConnection(currentConnection);
        }
    }
}
