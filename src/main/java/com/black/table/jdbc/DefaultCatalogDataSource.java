package com.black.table.jdbc;

import com.black.core.sql.code.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DefaultCatalogDataSource implements CatalogDataSource{

    final DataSource dataSource;

    public DefaultCatalogDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getDriverName() {
        if (dataSource instanceof DynamicDataSource){
            return doGetDriverName(((DynamicDataSource) dataSource).lookUp());
        }
        return doGetDriverName(dataSource);
    }

    private String doGetDriverName(DataSource dataSource){
        if (dataSource instanceof HikariDataSource){
            return ((HikariDataSource) dataSource).getDriverClassName();
        }
        return null;
    }

    @Override
    public DataSource getDataSource() {
        if (dataSource instanceof DynamicDataSource){
            return ((DynamicDataSource) dataSource).lookUp();
        }
        return dataSource;
    }

    @Override
    public String username() {
        if (dataSource instanceof DynamicDataSource){
            return doGetUserName(((DynamicDataSource) dataSource).lookUp());
        }
        return doGetUserName(dataSource);
    }

    private String doGetUserName(DataSource dataSource){
        if (dataSource instanceof HikariDataSource){
            return ((HikariDataSource) dataSource).getUsername();
        }
        return null;
    }

    @Override
    public String password() {
        if (dataSource instanceof DynamicDataSource){
            return doGetPassword(((DynamicDataSource) dataSource).lookUp());
        }
        return doGetPassword(dataSource);
    }

    private String doGetPassword(DataSource dataSource){
        if (dataSource instanceof HikariDataSource){
            return ((HikariDataSource) dataSource).getPassword();
        }
        return null;
    }

    @Override
    public String url() {
        if (dataSource instanceof DynamicDataSource){
            return doGetUrl(((DynamicDataSource) dataSource).lookUp());
        }
        return doGetUrl(dataSource);
    }

    private String doGetUrl(DataSource dataSource){
        if (dataSource instanceof HikariDataSource){
            return ((HikariDataSource) dataSource).getJdbcUrl();
        }
        return null;
    }

    @Override
    public CatalogConnection getConnection() {
        return new DefaultCatalogConnection(this);
    }

    @Override
    public boolean isDynamic() {
        return dataSource instanceof DynamicDataSource;
    }
}
