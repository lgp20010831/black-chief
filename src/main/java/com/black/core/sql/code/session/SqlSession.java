package com.black.core.sql.code.session;

import com.black.core.sql.code.YmlDataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;

public interface SqlSession {

    void write(String sql);

    Connection getConnection();

    void writeAndFlush(String sql);

    void flush();

    void close();

    Object poll();

    static SqlSession open(){
        return open(new YmlDataSourceBuilder().getDataSource());
    }

    static SqlSession open(DataSource dataSource){
        return new CommonSession(dataSource);
    }
}
