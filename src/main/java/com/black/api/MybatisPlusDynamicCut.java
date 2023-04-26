package com.black.api;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public class MybatisPlusDynamicCut {


    public static void push(String name){
        DynamicDataSourceContextHolder.push(name);
    }

    public static void poll(){
        DynamicDataSourceContextHolder.poll();
    }

    public static void closeConnection(Connection connection, DataSource dataSource){
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

}
