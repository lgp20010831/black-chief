package com.black.core.sql.code.sup.impl;

import com.black.core.sql.code.sup.AbstractSqlSeqParser;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;
import com.black.throwable.InterceptException;

import java.util.Map;

public class LikeSeqParser extends AbstractSqlSeqParser {

    @Override
    public boolean support(String seq, OperationType type) {
        return type == OperationType.SELECT && (seq.contains("like")) && StringUtils.isIndependent(seq, "like");
    }

    @Override
    public boolean queryParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata) {
        String[] eqkv = StringUtils.split(seq, "like", 2, "error parse seq: " + seq);
        String columnName = eqkv[0].trim();
        if (saveColumn(metadata, columnName)){
            String val;
            try {
                val = SQLUtils.getPureString(processorValue(eqkv[1], argMap, metadata));
            } catch (InterceptException e) {
                System.out.println("intercept sql sequence:[" + seq + "]");
                return false;
            }
            if (statement.exisOperation(columnName, OperationType.INSERT)){
                statement.replaceOperation(columnName, OperationType.INSERT, val, false);
                return false;
            }else {
                statement.writeLike(columnName, val);
            }
            return true;
        }
        return false;
    }
}
