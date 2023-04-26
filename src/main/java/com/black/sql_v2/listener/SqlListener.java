package com.black.sql_v2.listener;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.SqlExecutor;

public interface SqlListener {


    default void beforeFlush(SqlOutStatement statement, SqlExecutor executor){}


    default String postInvokeSql(String sql, SqlOutStatement statement, SqlExecutor executor){
        return sql;
    }
}
