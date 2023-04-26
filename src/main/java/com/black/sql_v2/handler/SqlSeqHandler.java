package com.black.sql_v2.handler;

import com.black.core.sql.unc.OperationType;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.utils.SqlV2Utils;

public class SqlSeqHandler extends AbstractStringSupporter implements SqlStatementHandler{

    public static final String PREFIX = "$S:";
    public SqlSeqHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return true;
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        String sqlSequenceTxt = getTxt(param);
        String[] sqlSequences = sqlSequenceTxt.split(",");
        OperationType type = statement instanceof InsertStatement ?
                OperationType.INSERT : OperationType.SELECT;
        SqlV2Utils.setSeqInStatement(statement, type, sqlSequences);
        return statement;
    }
}
