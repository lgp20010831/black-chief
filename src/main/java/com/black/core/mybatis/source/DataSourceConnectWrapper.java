package com.black.core.mybatis.source;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceConnectWrapper  {

    HikariDataSource dataSource;

    String alias;

    public DataSourceConnectWrapper(String alias){
        this.alias = alias;
        dataSource = new HikariDataSource();
    }

    public DataSourceConnectWrapper setAutoCommit(boolean commit){
        dataSource.setAutoCommit(commit);
        return this;
    }

    public DataSourceConnectWrapper driver(String driver){
        dataSource.setDriverClassName(driver);
        return this;
    }

    public DataSourceConnectWrapper url(String url){
        dataSource.setJdbcUrl(url);
        return this;
    }

    public DataSourceConnectWrapper username(String name){
        dataSource.setUsername(name);
        return this;
    }

    public DataSourceConnectWrapper password(String password){
        dataSource.setPassword(password);
        return this;
    }

    public DataSource source(){
        return dataSource;
    }


    public DataSource getDataSource() {
        return source();
    }
}
