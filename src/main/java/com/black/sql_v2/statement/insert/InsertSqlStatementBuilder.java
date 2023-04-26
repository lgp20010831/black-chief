package com.black.sql_v2.statement.insert;

import com.black.core.util.Assert;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.sql_v2.statement.SqlStatementBuilder;
import com.black.table.TableMetadata;

public class InsertSqlStatementBuilder implements SqlStatementBuilder {


    @Override
    public SqlOutStatement build(String tableName, boolean alias, Object param) {
        SqlOutStatement sqlStatement = SqlWriter.insert(tableName);
        TableMetadata tableMetadata = SqlV2Utils.tryGetMetadata(tableName);
        Assert.notNull(tableMetadata, "can not find table: " + tableName);
        for (String columnName : tableMetadata.getColumnNameSet()) {
            sqlStatement.insertVariable(columnName, "?");
        }
        return sqlStatement;
    }

    @Override
    public boolean support(Object param) {
        return true;
    }
}
