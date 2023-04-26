package com.black.table.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface CatalogConnection {

    CatalogDataSource getDataSource();

    default Connection getConnection() throws SQLException {
        return getDataSource().getDataSource().getConnection();
    }
}
