package com.black.core.sql.code.impl.sqlvalue_impl;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.unc.SqlValue;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InsertSqlValueResolver implements SqlValueGroupHandler {

    @Override
    public boolean support(Configuration configuration) {
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.INSERT;
    }

    @Override
    public List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep) {
        Object attachment = ep.attachment();
        List<SqlValueGroup> valueGroups = new ArrayList<>();
        if (!(attachment instanceof List)){
            SqlValueGroup group = new SqlValueGroup();
            for (SqlVariable variable : ep.getStatement().getVariables(OperationType.INSERT)) {
                group.addValue(new SqlValue(variable, null, configuration.getTableMetadata().getColumnMetadata(variable.getColumnName())));
            }
            valueGroups.add(group);
            return valueGroups;
        }
        List<Map<String, Object>> insertValues = (List<Map<String, Object>>) attachment;
        BoundStatement nhStatement = ep.getNhStatement();
        SqlOutStatement statement = nhStatement.getStatement();
        for (Map<String, Object> map : insertValues) {
            valueGroups.add(parse(map, statement, configuration));
        }
        return valueGroups;
    }

    SqlValueGroup parse(Map<String, Object> map, SqlOutStatement statement, Configuration configuration){
        SqlValueGroup group = new SqlValueGroup();
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        TableMetadata tableMetadata = configuration.getTableMetadata();
        for (SqlVariable variable : statement.getVariables(OperationType.INSERT)) {
            String columnName = variable.getColumnName();
            String alias = handler.convertAlias(columnName);
            Object val = map.get(alias);
            SqlValue sqlValue = new SqlValue(variable, val, tableMetadata.getColumnMetadata(columnName));
            for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
                listener.postSqlValue(configuration, sqlValue);
            }
            group.addValue(sqlValue);
        }
        return group;
    }
}
