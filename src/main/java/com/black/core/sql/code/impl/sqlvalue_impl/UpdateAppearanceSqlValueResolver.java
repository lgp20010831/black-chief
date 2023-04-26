package com.black.core.sql.code.impl.sqlvalue_impl;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.unc.SqlValue;
import com.black.core.sql.unc.SqlVariable;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;

import java.util.*;

public class UpdateAppearanceSqlValueResolver implements SqlValueGroupHandler {
    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof AppearanceConfiguration &&
                configuration.getMethodType() == SQLMethodType.UPDATE;
    }

    @Override
    public List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep) {
        TableMetadata tableMetadata = configuration.getTableMetadata();
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        List<Map<String, Object>> fruitSet = (List<Map<String, Object>>) ep.attachment();
        if (fruitSet == null){
            return new ArrayList<>();
        }
        List<SqlValueGroup> result = new ArrayList<>();
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        for (Map<String, Object> map : fruitSet) {
            SqlValueGroup valueGroup = new SqlValueGroup();
            List<SqlVariable> variables = statement.getVariables();
            for (SqlVariable variable : variables) {
                String columnName = variable.getColumnName();
                String alias = handler.convertAlias(columnName);
                valueGroup.addValue(new SqlValue(variable, map.get(alias), tableMetadata.getColumnMetadata(columnName)));
            }
            result.add(valueGroup);
        }
        return result;
    }
}
