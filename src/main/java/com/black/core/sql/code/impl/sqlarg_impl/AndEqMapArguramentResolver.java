package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.AndEqMap;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;

import java.util.Map;
import java.util.Set;

public class AndEqMapArguramentResolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(AndEqMap.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        AndEqMap annotation = pw.getAnnotation(AndEqMap.class);
        boolean convert = annotation.convert();
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        if (!statement.isAndCon()) {
            statement.filp();
        }
        if (value == null) return;
        if (!(value instanceof Map)){
            throw new SQLSException("andEqMap the parameter type is required to be map");
        }
        Set<String> columnNames = configuration.getColumnNames();
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        Map<String, Object> map = (Map<String, Object>) value;
        for (String alias : map.keySet()) {
            String columnName = convert ? handler.convertColumn(alias) : alias;
            if (columnNames.contains(columnName)){
                Object o = map.get(alias);
                statement.writeEq(columnName, "?", false);
                boundStatement.addMV(new MappingVal(OperationType.SELECT, o, columnName));
            }
        }
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        return null;
    }
}
