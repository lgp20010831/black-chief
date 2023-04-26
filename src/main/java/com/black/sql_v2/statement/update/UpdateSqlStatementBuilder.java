package com.black.sql_v2.statement.update;

import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.sql_v2.statement.SqlStatementBuilder;

public class UpdateSqlStatementBuilder implements SqlStatementBuilder {


    @Override
    public SqlOutStatement build(String tableName, boolean alias, Object param) {
        return SqlWriter.update(tableName);
    }

    @Override
    public boolean support(Object param) {
        return true;
    }
}
