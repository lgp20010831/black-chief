package com.black.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConnection {

    Connection getFetchConnection();

    void close() throws SQLException;

    boolean isTransactionActivity();

    String getName();

}
