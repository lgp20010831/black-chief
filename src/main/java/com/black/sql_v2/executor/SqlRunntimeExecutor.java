package com.black.sql_v2.executor;

import com.black.sql.SqlOutStatement;

public interface SqlRunntimeExecutor {


    boolean support(SqlOutStatement statement);


    Object runSql(SqlOutStatement statement, String sql) throws Throwable;
}
