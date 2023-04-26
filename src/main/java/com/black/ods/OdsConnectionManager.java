package com.black.ods;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.utils.LocalMap;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class OdsConnectionManager {

    private final OdsChain chain;

    private LocalMap<DataSource, Connection> connectionLocalMap = new LocalMap<>();

    public OdsConnectionManager(OdsChain chain) {
        this.chain = chain;
    }

    public Connection getConnection(OdsUndertake undertake){
        DataSource dataSource = undertake.getDataSource();
        Assert.notNull(dataSource, "can not find datasource:" + undertake);
        return connectionLocalMap.computeIfAbsent(dataSource, ds -> {
            Connection connection = doGetConnection(dataSource);
            if (undertake.isOpenTransactional()) {
                openConnectionTransaction(connection);
            }
            return connection;
        });
    }

    public void end(){
        for (Connection connection : connectionLocalMap.values()) {
            commitConnection(connection);
            closeConnectionTransaction(connection);
            SQLUtils.closeConnection(connection);
        }
        connectionLocalMap.clear();
    }

    public void rollback(){
        for (Connection connection : connectionLocalMap.values()) {
            rollbackConnection(connection);
        }
    }

    protected void rollbackConnection(Connection connection){
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    protected void commitConnection(Connection connection){
        try {
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    protected void closeConnectionTransaction(Connection connection){
        try {
            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    protected void openConnectionTransaction(Connection connection){
        try {
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    protected Connection doGetConnection(DataSource dataSource){
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
