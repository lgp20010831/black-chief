package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.SetMap;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;

import java.util.Map;
import java.util.Set;

public class SetMapArguramentResolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(SetMap.class) && Map.class.isAssignableFrom(pw.getType());
    }


    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        if (value == null) return;
        Map<String, Object> mapVal = (Map<String, Object>) value;
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        TableMetadata tableMetadata = configuration.getTableMetadata();
        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        PrimaryKey primaryKey = tableMetadata.firstPrimaryKey();
        for (String alias : mapVal.keySet()) {
            String column = handler.convertColumn(alias);
            //过滤主键
            if (columnNameSet.contains(column) && (primaryKey == null || !primaryKey.getName().equals(column))){
                statement.writeSetVariable(column, "?");
                boundStatement.addMV(new MappingVal(OperationType.UPDATE, mapVal.get(alias), column));
            }
        }
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        return null;
    }



}
