package com.black.sql_v2.statement.select;

import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.sql_v2.statement.SqlStatementBuilder;

public class DefaultSelectSqlStatementBuilder implements SqlStatementBuilder {


    @Override
    public SqlOutStatement build(String tableName, boolean alias, Object param) {
        return SqlWriter.select(tableName, alias);
    }

    @Override
    public boolean support(Object param) {
        return true;
    }
}
