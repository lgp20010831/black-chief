package com.black.core.sql.code.datasource;

import com.black.core.sql.SQLSException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@SuppressWarnings("all")
public class DynamicConnection implements Connection {


    private final DynamicDataSource dataSource;

    private final Map<DataSource, Connection> connectionMap = new ConcurrentHashMap<>();

    public DynamicConnection(DynamicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection loopUp(){
        DataSource dataSource = this.dataSource.lookUp();
        return connectionMap.computeIfAbsent(dataSource, ds -> {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                throw new SQLSException(e);
            }
        });
    }

    @Override
    public Statement createStatement() throws SQLException {
        return loopUp().createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return loopUp().prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return loopUp().prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return loopUp().nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        loopUp().setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return loopUp().getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        loopUp().commit();
    }

    @Override
    public void rollback() throws SQLException {
        loopUp().rollback();
    }

    @Override
    public void close() throws SQLException {
        SQLException ex = null;
        for (Connection connection : connectionMap.values()) {
            try {
                connection.close();
            }catch (SQLException e){
                ex = e;
            }
        }
        if (ex != null){
            throw ex;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return loopUp().isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return loopUp().getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        loopUp().setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return loopUp().isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        loopUp().setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return loopUp().getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        loopUp().setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return loopUp().getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return loopUp().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        loopUp().clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return loopUp().createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return loopUp().prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return loopUp().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return loopUp().getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        loopUp().setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        loopUp().setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return loopUp().getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return loopUp().setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return loopUp().setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        loopUp().rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        loopUp().releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return loopUp().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return loopUp().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return loopUp().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return loopUp().prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return loopUp().prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return loopUp().prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return loopUp().createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return loopUp().createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return loopUp().createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return loopUp().createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return loopUp().isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        loopUp().setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        loopUp().setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return loopUp().getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return loopUp().getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return loopUp().createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return loopUp().createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        loopUp().setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return loopUp().getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        loopUp().abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        loopUp().setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return loopUp().getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return loopUp().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return loopUp().isWrapperFor(iface);
    }
}
