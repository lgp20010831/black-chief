package com.black.table.jdbc;

public class DefaultCatalogConnection implements CatalogConnection{

    final CatalogDataSource catalogDataSource;

    public DefaultCatalogConnection(CatalogDataSource catalogDataSource) {
        this.catalogDataSource = catalogDataSource;
    }

    @Override
    public CatalogDataSource getDataSource() {
        return catalogDataSource;
    }
}
