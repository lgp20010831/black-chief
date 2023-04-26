package com.black.sql_v2.statement.select;

import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.sql_v2.statement.SqlStatementBuilder;

public class SelectCountSqlStatementBuilder implements SqlStatementBuilder {
    @Override
    public boolean support(Object param) {
        return true;
    }

    @Override
    public SqlOutStatement build(String tableName, boolean alias, Object param) {
        return SqlWriter.selectCount(tableName, alias);
    }
}
