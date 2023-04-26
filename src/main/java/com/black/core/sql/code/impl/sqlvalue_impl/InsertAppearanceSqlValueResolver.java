package com.black.core.sql.code.impl.sqlvalue_impl;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.code.sqls.ResultType;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.unc.SqlValue;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.util.*;

@Log4j2
public class InsertAppearanceSqlValueResolver implements SqlValueGroupHandler {
    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof AppearanceConfiguration &&
                configuration.getMethodType() == SQLMethodType.INSERT;
    }

    @Override
    public List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep) {
        AppearanceConfiguration appearanceConfiguration = (AppearanceConfiguration) configuration;
        //是主表主键 java 写法
        String targetKey = appearanceConfiguration.getTargetKey();
        Object attachment = ep.attachment();
        if (!(attachment instanceof List)){
            throw new SQLSException("can not find insert attachement value");
        }
        List<Map<String, Object>> insertValues = (List<Map<String, Object>>) attachment;
        String appearanceName = appearanceConfiguration.getAppearanceName();
        List<SqlValueGroup> groups = new ArrayList<>();
        for (Map<String, Object> map : insertValues) {
            if (!map.containsKey(appearanceName)){
                continue;
            }
            Object appearanceValue = map.get(appearanceName);
            if (!map.containsKey(targetKey)){
                //如果元数据中每有需要的主键值, 则可能主键是自动生成的
                try {
                    List<String> primaryKeys = (List<String>) ResultSetThreadManager.getResultAndParse(ResultType.GeneratedKeys, appearanceConfiguration
                            .getEp()
                            .getRp()
                            .getExecuteBody()
                            .getWrapper()
                            .getGeneratedKeys());
                    if (primaryKeys.size() != insertValues.size()) {
                        throw new StopSqlInvokeException("无法获取主键信息, 原因是拉取添加的条数与参数条数不符合");
                    }
                    for (int i = 0; i < primaryKeys.size(); i++) {
                        insertValues.get(i).put(targetKey, primaryKeys.get(i));
                    }
                    return handler(configuration, ep);
                } catch (SQLException e) {
                    throw new StopSqlInvokeException("无法获取主键信息, 原因是获取自动生成主键结果集时发生异常: " + e.getMessage());
                }
            }
            //获取主键值
            Object primaryVal = map.get(targetKey);
            Object o = map.get(appearanceName);
            Collection<Map<String, Object>> tableDataList;
            if (o instanceof Collection){
                tableDataList = (Collection<Map<String, Object>>) o;
            }else if (o instanceof Map){
                tableDataList = Collections.singletonList((Map<String, Object>) o);
            }else {
                throw new SQLSException(appearanceName + " 在数据源中不是 map 或 list<Map> 结构无法转换成表数据");
            }
            for (Map<String, Object> tableData : tableDataList) {
                groups.add(parse(tableData, ep.getNhStatement().getStatement(), appearanceConfiguration, primaryVal));
            }
        }
        return groups;
    }

    SqlValueGroup parse(Map<String, Object> map, SqlOutStatement statement,
                        AppearanceConfiguration configuration, Object masterValue){
        SqlValueGroup group = new SqlValueGroup();
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        String foreignKeyColumnName = configuration.getForeignKeyColumnName();
        TableMetadata tableMetadata = configuration.getTableMetadata();
        for (SqlVariable variable : statement.getVariables(OperationType.INSERT)) {
            String columnName = variable.getColumnName();
            String alias = handler.convertAlias(columnName);
            Object val = foreignKeyColumnName.equals(columnName) ? masterValue : map.get(alias);
            SqlValue sqlValue = new SqlValue(variable, val, tableMetadata.getColumnMetadata(columnName));
            for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
                listener.postSqlValue(configuration, sqlValue);
            }
            group.addValue(sqlValue);
        }
        return group;
    }
}
