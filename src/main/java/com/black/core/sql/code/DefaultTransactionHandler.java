package com.black.core.sql.code;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;

import java.sql.Connection;
import java.sql.SQLException;

public class DefaultTransactionHandler implements TransactionHandler{

    private boolean close = true;
    private Connection connection;
    private final GlobalSQLConfiguration configuration;

    public DefaultTransactionHandler(Connection connection, GlobalSQLConfiguration configuration) {
        this.connection = connection;
        this.configuration = configuration;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public GlobalSQLConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getAlias() {
        return configuration.getDataSourceAlias();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void commit() {
        Log log = configuration.getLog();
        if (!isOpen()){
            if (log.isDebugEnabled()) {
                log.debug("Cannot commit at this time because it is " +
                        "controlled by an external method transaction" + configuration.getDataSourceAlias());
            }
            return;
        }
        try {
            if (ConnectionManagement.checkConnection(connection)){
                if (log.isDebugEnabled()) {
                    log.debug("==> commit transaction: [" + configuration.getDataSourceAlias() + "]");
                }
                connection.commit();
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("==> connection is closed while commit");
                }
            }

        } catch (SQLException e) {
            ConnectionManagement.closeCurrentConnection(configuration.getDataSourceAlias());
            throw new SQLSException("commit sql fail", e);
        }
    }

    @Override
    public void rollback() {
        Log log = configuration.getLog();
        try {
            if (!isOpen()){
                if (log.isDebugEnabled()) {
                    log.debug("Cannot roll back at this time because it is " +
                            "controlled by an external method transaction" + configuration.getDataSourceAlias());
                }
                return;
            }

            if(ConnectionManagement.checkConnection(connection)){
                String savePointAlias = SaveManager.getSavePointAlias(getAlias());
                if (savePointAlias != null){
                    if (log.isDebugEnabled()) {
                        log.debug("<== rollback transaction: [" + configuration.getDataSourceAlias() + "] of save point: [" + savePointAlias + "}");
                    }
                    connection.rollback(SaveManager.getSavePoint(getAlias(), savePointAlias));
                }else {
                    if (log.isDebugEnabled()) {
                        log.debug("<== rollback transaction: [" + configuration.getDataSourceAlias() + "]");
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
                log.error("rollback data source fail: " + configuration.getDataSourceAlias());
            }
            ConnectionManagement.closeCurrentConnection(configuration.getDataSourceAlias());
            throw new SQLSException("rollback fail", e);
        }finally {
            SaveManager.clear();
        }
    }

    @Override
    public void close() {
        Log log = configuration.getLog();
        try {
            if (!close){
                if (log.isDebugEnabled()) {
                    log.debug("===> close transaction: [" + configuration.getDataSourceAlias() + "]");
                }
                if (!connection.getAutoCommit()){
                    connection.setAutoCommit(true);
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot close transaction at this time because it is " +
                            "controlled by an external method transaction" + configuration.getDataSourceAlias());
                }
            }
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("close transaction fail: " + configuration.getDataSourceAlias());
            }
            ConnectionManagement.closeCurrentConnection(configuration.getDataSourceAlias());
            throw new SQLSException("close transaction fail: ", e);
        }finally {
            close = true;
        }
    }

    @Override
    public void open() throws SQLException {
        Log log = configuration.getLog();
        try {
            if (close){
                if (log.isDebugEnabled()) {
                    log.debug("==> open transaction: [" + configuration.getDataSourceAlias() + "]");
                }
                if (connection.getAutoCommit()){
                    connection.setAutoCommit(false);
                }
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot open transaction at this time because it is " +
                            "controlled by an external method transaction" + configuration.getDataSourceAlias());
                }
            }
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("open transaction fail: " + configuration.getDataSourceAlias());
            }
            ConnectionManagement.closeCurrentConnection(configuration.getDataSourceAlias());
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
