package com.black.core.sql.code.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DataSourceCacheWrapper implements DataSource {

    protected DataSource dataSource;

    public static boolean threadCacheConnection = false;

    final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public DataSourceCacheWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null, null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (threadCacheConnection){
            Connection connection = connectionThreadLocal.get();
            if (connection != null && ConnectionUtils.checkConnection(connection)) {
                return connection;
            }else {
                ConnectionUtils.closeConnection(connection);
                if (username == null && password == null){
                    connection =  getDataSource().getConnection();
                }else {
                    connection = getDataSource().getConnection(username, password);
                }
                connectionThreadLocal.set(connection);
                return connection;
            }
        }else {
            if (username == null && password == null){
                return getDataSource().getConnection();
            }else {
                return getDataSource().getConnection(username, password);
            }
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }
}
