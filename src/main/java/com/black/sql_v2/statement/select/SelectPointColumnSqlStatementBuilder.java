package com.black.sql_v2.statement.select;

import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.statement.SqlStatementBuilder;

public class SelectPointColumnSqlStatementBuilder extends AbstractStringSupporter implements SqlStatementBuilder {
    public static final String PREFIX = "#COLUMN:";

    public SelectPointColumnSqlStatementBuilder() {
        super(PREFIX);
    }

    @Override
    public SqlOutStatement build(String tableName, boolean alias, Object param) {
        String txt = getTxt(param);
        return SqlWriter.select(tableName, alias, txt.split(","));
    }

}
