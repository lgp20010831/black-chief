package com.black.thread;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.TransactionHandler;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.util.SQLUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author 李桂鹏
 * @create 2023-05-24 16:29
 */
@SuppressWarnings("all")
public class BaseTransactionHandler implements TransactionHandler {

    private final Connection connection;

    private final Log log;

    private boolean close = true;

    public BaseTransactionHandler(Connection connection, Log log) {
        this.connection = connection;
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return null;
    }

    @Override
    public String getAlias() {
        return "MultithreadedTransactions-Base";
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void commit() {
        Log log = getLog();
        if (!isOpen()){
            if (log.isDebugEnabled()) {
                log.debug("Cannot commit at this time because it is " +
                        "controlled by an external method transaction" + getAlias());
            }
            return;
        }
        try {
            if (ConnectionManagement.checkConnection(connection)){
                if (log.isDebugEnabled()) {
                    log.debug("==> commit transaction: [" + getAlias() + "]");
                }
                connection.commit();
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("==> connection is closed while commit");
                }
            }

        } catch (SQLException e) {
            closeConnection();
            throw new SQLSException("commit sql fail", e);
        }
    }

    @Override
    public void rollback() {
        Log log = getLog();
        try {
            if (!isOpen()){
                if (log.isDebugEnabled()) {
                    log.debug("Cannot roll back at this time because it is " +
                            "controlled by an external method transaction" + getAlias());
                }
                return;
            }

            if(ConnectionManagement.checkConnection(connection)){
                if (log.isDebugEnabled()) {
                    log.debug("<== rollback transaction: [" + getAlias() + "]");
                }

                connection.rollback();
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("==> connection is closed while roll back");
                }
            }

        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("rollback data source fail: " + getAlias());
            }
            closeConnection();
            throw new SQLSException("rollback fail", e);
        }
    }

    @Override
    public void close() {
        Log log = getLog();
        try {
            if (!close){
                if (log.isDebugEnabled()) {
                    log.debug("===> close transaction: [" + getAlias() + "]");
                }
                if (!connection.getAutoCommit()){
                    connection.setAutoCommit(true);
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot close transaction at this time because it is " +
                            "controlled by an external method transaction" + getAlias());
                }
            }
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("close transaction fail: " + getAlias());
            }
            closeConnection();
            throw new SQLSException("close transaction fail: ", e);
        }finally {
            close = true;
        }
    }

    protected void closeConnection(){
        SQLUtils.closeConnection(connection);
    }

    @Override
    public void open() throws SQLException {
        Log log = getLog();
        try {
            if (close){
                if (log.isDebugEnabled()) {
                    log.debug("==> open transaction: [" + getAlias() + "]");
                }
                if (connection.getAutoCommit()){
                    connection.setAutoCommit(false);
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot open transaction at this time because it is " +
                            "controlled by an external method transaction" + getAlias());
                }
            }
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("open transaction fail: " + getAlias());
            }
            closeConnection();
            throw e;
        }finally {
            close = false;
        }
    }

    @Override
    public boolean isOpen() {
        try {
            return !connection.getAutoCommit();
        } catch (SQLException e) {
            throw new SQLSException("Unable to access whether the transaction has already started", e);
        }
    }
}
