package com.black.core.sql.code.sup.impl;

import com.black.core.sql.code.sup.AbstractSqlSeqParser;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;
import com.black.throwable.InterceptException;

import java.util.Map;

public class XYDYSeqParser extends AbstractSqlSeqParser {

    @Override
    public boolean support(String seq, OperationType type) {
        return type == OperationType.SELECT && seq.contains("<=") && StringUtils.isIndependent(seq, "<=");
    }

    @Override
    public boolean queryParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata) {
        String[] eqkv = StringUtils.split(seq, "<=", 2, "error parse seq: " + seq);
        String columnName = eqkv[0].trim();
        if (saveColumn(metadata, columnName)){
            String val;
            try {
                val = processorValue(eqkv[1], argMap, metadata);
            } catch (InterceptException e) {
                System.out.println("intercept sql sequence:[" + seq + "]");
                return false;
            }
            String sql = statement.getColumnName(columnName) + " <= " + val;
            statement.write(sql, OperationType.SELECT);
            return true;
        }
        return false;
    }

}
