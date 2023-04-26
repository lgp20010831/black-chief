package com.black.sql_v2.sql_statement;

import com.black.sql_v2.ObjectParamSupporter;

import java.sql.Statement;

public interface JavaSqlStatementHandler extends ObjectParamSupporter {

    Statement handlerJavaStatement(Statement statement, Object param) throws Throwable;

}
