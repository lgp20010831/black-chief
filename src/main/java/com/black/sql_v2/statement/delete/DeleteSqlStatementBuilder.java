package com.black.sql_v2.statement.delete;

import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.sql_v2.statement.SqlStatementBuilder;

public class DeleteSqlStatementBuilder implements SqlStatementBuilder {


    @Override
    public SqlOutStatement build(String tableName, boolean alias, Object param) {
        return SqlWriter.delete(tableName);
    }

    @Override
    public boolean support(Object param) {
        return true;
    }
}
