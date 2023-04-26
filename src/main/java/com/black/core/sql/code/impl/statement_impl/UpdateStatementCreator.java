package com.black.core.sql.code.impl.statement_impl;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DefaultSqlStatementCreator;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

public class UpdateStatementCreator implements DefaultSqlStatementCreator {
    @Override
    public boolean support(Configuration configuration) {
        return configuration.getMethodType() == SQLMethodType.UPDATE;
    }

    @Override
    public BoundStatement createStatement(Configuration configuration) {
        SqlOutStatement statement = SqlWriter.update(configuration.getTableName());
        return new BoundStatement(statement);
    }
}
