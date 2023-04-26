package com.black.core.sql.code.impl.statement_impl;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DefaultSqlStatementCreator;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

public class InsertStatementCreator implements DefaultSqlStatementCreator {
    @Override
    public boolean support(Configuration configuration) {
        return configuration.getMethodType() == SQLMethodType.INSERT;
    }

    @Override
    public BoundStatement createStatement(Configuration configuration) {
        SqlOutStatement sqlStatement = SqlWriter.insert(configuration.getTableName());
        for (String columnName : configuration.getColumnNames()) {
            sqlStatement.insertVariable(columnName, "?");
        }
        return new BoundStatement(sqlStatement);
    }
}
