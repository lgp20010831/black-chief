package com.black.sql_v2.statement;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.ObjectParamSupporter;

public interface SqlStatementBuilder extends ObjectParamSupporter {


    SqlOutStatement build(String tableName, boolean alias, Object param);

}
