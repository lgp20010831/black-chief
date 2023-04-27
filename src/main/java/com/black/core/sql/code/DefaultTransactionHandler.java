package com.black.core.sql.code;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;

import java.sql.Connection;
import java.sql.SQLException;

public class DefaultTransactionHandler implements TransactionHandler{

    private boolean close = true;
    private Connection connection;
    private final String alias;
    private final Log log;

    public DefaultTransactionHandler(Connection connection, String alias){
        this(connection, alias, new SystemLog());
    }
    
    public DefaultTransactionHandler(Connection connection, String alias, Log log) {
        this.connection = connection;
        this.alias = alias;
        this.log = log;
        
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }


    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return null;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public Log getLog() {
        return log;
    }

    @Override
    public void commit() {
        Log log = getLog();
        if (!isOpen()){
            if (log.isDebugEnabled()) {
                log.debug("Cannot commit at this time because it is " +
                        "controlled by an external method transaction" + alias);
            }
            return;
        }
        try {
            if (ConnectionManagement.checkConnection(connection)){
                if (log.isDebugEnabled()) {
                    log.debug("==> commit transaction: [" + alias + "]");
                }
                connection.commit();
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("==> connection is closed while commit");
                }
            }

        } catch (SQLException e) {
            ConnectionManagement.closeCurrentConnection(alias);
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
                            "controlled by an external method transaction" + alias);
                }
                return;
            }

            if(ConnectionManagement.checkConnection(connection)){
                String savePointAlias = SaveManager.getSavePointAlias(getAlias());
                if (savePointAlias != null){
                    if (log.isDebugEnabled()) {
                        log.debug("<== rollback transaction: [" + alias + "] of save point: [" + savePointAlias + "}");
                    }
                    connection.rollback(SaveManager.getSavePoint(getAlias(), savePointAlias));
                }else {
                    if (log.isDebugEnabled()) {
                        log.debug("<== rollback transaction: [" + alias + "]");
                    }

                    connection.rollback();
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("==> connection is closed while roll back");
                }
            }

        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("rollback data source fail: " + alias);
            }
            ConnectionManagement.closeCurrentConnection(alias);
            throw new SQLSException("rollback fail", e);
        }finally {
            SaveManager.clear();
        }
    }

    @Override
    public void close() {
        Log log = getLog();
        try {
            if (!close){
                if (log.isDebugEnabled()) {
                    log.debug("===> close transaction: [" + alias + "]");
                }
                if (!connection.getAutoCommit()){
                    connection.setAutoCommit(true);
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot close transaction at this time because it is " +
                            "controlled by an external method transaction" + alias);
                }
            }
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("close transaction fail: " + alias);
            }
            ConnectionManagement.closeCurrentConnection(alias);
            throw new SQLSException("close transaction fail: ", e);
        }finally {
            close = true;
        }
    }

    @Override
    public void open() throws SQLException {
        Log log = getLog();
        try {
            if (close){
                if (log.isDebugEnabled()) {
                    log.debug("==> open transaction: [" + alias + "]");
                }
                if (connection.getAutoCommit()){
                    connection.setAutoCommit(false);
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot open transaction at this time because it is " +
                            "controlled by an external method transaction" + alias);
                }
            }
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("open transaction fail: " + alias);
            }
            ConnectionManagement.closeCurrentConnection(alias);
            throw e;
        }finally {
            close = false;
        }
    }

    @Override
    public boolean isOpen() {
        return !close;
    }
}
