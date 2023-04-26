package com.black.sql_v2.handler;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.*;
import com.black.sql_v2.utils.DictUtils;
import com.black.sql_v2.utils.SqlV2Utils;

public class DictSqlHandler extends AbstractStringSupporter implements SqlStatementHandler{

    public static final String PREFIX = "$D:";

    public DictSqlHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return SqlV2Utils.isSelectStatement(statement);
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        String dictTxt = getTxt(param);
        String[] expressions = dictTxt.split(";");
        return DictUtils.handlerTxt(expressions, statement);
    }


}
