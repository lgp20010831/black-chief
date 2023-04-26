package com.black.core.sql.code.impl.statement_impl;

import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DefaultSqlStatementCreator;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.sql.SqlOutStatement;

public class SelectStatementCreator extends AbstractSelectStatementCreator implements DefaultSqlStatementCreator {
    @Override
    public boolean support(Configuration configuration) {
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.QUERY;
    }

    @Override
    public BoundStatement createStatement(Configuration configuration) {
        SqlOutStatement statement = createSelectStatement(configuration);
        return new BoundStatement(statement);
    }
}
