package com.black.sql_v2.handler;

import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;

public abstract class AbstractWhereConditionHandler implements SqlStatementHandler{
    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return !(statement instanceof InsertStatement);
    }


}
