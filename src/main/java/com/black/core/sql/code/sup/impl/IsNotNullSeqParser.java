package com.black.core.sql.code.sup.impl;


import com.black.core.sql.code.sup.AbstractSqlSeqParser;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;


import java.util.Map;

public class IsNotNullSeqParser extends AbstractSqlSeqParser {
    @Override
    public boolean support(String seq, OperationType type) {
        return type == OperationType.SELECT && seq.contains("is not null");
    }


    @Override
    public boolean queryParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata) {
        return super.queryParse(seq, statement, argMap, metadata);
    }
}
