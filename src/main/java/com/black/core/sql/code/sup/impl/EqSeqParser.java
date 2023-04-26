package com.black.core.sql.code.sup.impl;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.sup.AbstractSqlSeqParser;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;
import com.black.throwable.InterceptException;

import java.util.Map;

public class EqSeqParser extends AbstractSqlSeqParser {
    @Override
    public boolean support(String seq, OperationType type) {
        return seq.contains("=");
    }

    @Override
    public boolean queryParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata) {
        String[] eqkv = StringUtils.split(seq, "=", 2, "error parse seq: " + seq);
        String columnName = eqkv[0].trim();
        if (saveColumn(metadata, columnName)){
            String val;
            try {
                val = processorValue(eqkv[1], argMap, metadata);
            } catch (InterceptException e) {
                System.out.println("intercept sql sequence:[" + seq + "]");
                return false;
            }
            if (statement.exisOperation(columnName, OperationType.SELECT)) {
                statement.replaceOperation(columnName, OperationType.SELECT, val, false);
                return false;
            }else
                statement.writeEq(columnName, val, false);
            return true;
        }
        return false;

    }

    @Override
    public void insertParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata) {
        String[] eqkv = StringUtils.split(seq, "=", 2, "error parse seq: " + seq);
        String columnName = eqkv[0].trim();
        if(saveColumn(metadata, columnName)){
            String val = MapArgHandler.parseSql(eqkv[1], argMap);
            if (statement.exisOperation(columnName, OperationType.INSERT)){
                statement.replaceOperation(columnName, OperationType.INSERT, val, false);
            }else
                statement.insertVariable(columnName, val);
        }

    }

    @Override
    public void updateParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata) {
        String[] eqkv = StringUtils.split(seq, "=", 2, "error parse seq: " + seq);
        String columnName = eqkv[0].trim();
        if(saveColumn(metadata, columnName)){
            String val = MapArgHandler.parseSql(eqkv[1], argMap);
            if (statement.exisOperation(columnName, OperationType.UPDATE)){
                statement.replaceOperation(columnName, OperationType.UPDATE, val, false);
            }else
                statement.writeSetVariable(columnName, val);
        }
    }
}
