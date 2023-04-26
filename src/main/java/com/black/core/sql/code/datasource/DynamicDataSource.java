package com.black.core.sql.code.datasource;

import com.black.core.sql.code.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class DynamicDataSource extends DataSourceCacheWrapper implements DataSourceBuilder {

    private final Map<Object, DataSource> dynamicDataSourceMap = new ConcurrentHashMap<>();

    public DynamicDataSource(DataSource defaultDataSource) {
        super(defaultDataSource);
    }

    @Override
    public DataSource getDataSource() {
        return this;
    }

    public DataSource getDefaultDataSource(){
        return dataSource;
    }

    public void setDefaultDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    public Map<Object, DataSource> getDynamicDataSourceMap() {
        return dynamicDataSourceMap;
    }

    public void registerDataSource(Object key, DataSource dataSource){
        if (key != null && dataSource != null){
            dynamicDataSourceMap.put(key, new DataSourceCacheWrapper(dataSource));
        }
    }

    public abstract DataSource lookUp();

    @Override
    public Connection getConnection() throws SQLException {
        return lookUp().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return lookUp().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return lookUp().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return lookUp().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return lookUp().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        lookUp().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        lookUp().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return lookUp().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return lookUp().getParentLogger();
    }
}
