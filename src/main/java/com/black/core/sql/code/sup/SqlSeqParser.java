package com.black.core.sql.code.sup;

import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;

import java.util.Map;

public interface SqlSeqParser {

    boolean support(String seq, OperationType type);

    void doParse(String seq, SqlOutStatement statement, OperationType type, Map<String, Object> argMap, TableMetadata metadata);
}
