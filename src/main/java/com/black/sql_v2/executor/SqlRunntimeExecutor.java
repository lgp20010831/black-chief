package com.black.sql_v2.executor;

import com.black.sql.SqlOutStatement;

import java.sql.ResultSet;

public interface SqlRunntimeExecutor {


    boolean support(SqlOutStatement statement);


    ResultSet runSql(SqlOutStatement statement, String sql) throws Throwable;
}
