package com.black.core.sql.code;

import com.black.core.sql.code.config.GlobalSQLConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public interface TransactionHandler {

    GlobalSQLConfiguration getConfiguration();

    String getAlias();

    Connection getConnection();

    void commit();

    void rollback();

    void close();

    void open() throws SQLException;

    boolean isOpen();
}
