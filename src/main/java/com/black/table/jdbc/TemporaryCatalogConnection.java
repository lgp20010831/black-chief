package com.black.table.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class TemporaryCatalogConnection implements CatalogConnection{

    final CatalogDataSource catalogDataSource;

    final Connection finalConnection;

    public TemporaryCatalogConnection(CatalogDataSource catalogDataSource, Connection finalConnection) {
        this.catalogDataSource = catalogDataSource;
        this.finalConnection = finalConnection;
    }

    @Override
    public CatalogDataSource getDataSource() {
        return catalogDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return finalConnection;
    }
}
