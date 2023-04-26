package com.black.table.jdbc;

import javax.sql.DataSource;

public interface CatalogDataSource {

    String getDriverName();

    DataSource getDataSource();

    String username();

    String password();

    String url();

    CatalogConnection getConnection();

    boolean isDynamic();
}
