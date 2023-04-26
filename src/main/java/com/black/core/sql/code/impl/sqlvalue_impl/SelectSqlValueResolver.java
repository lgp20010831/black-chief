package com.black.core.sql.code.impl.sqlvalue_impl;

import com.black.core.query.ArrayUtils;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlValue;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.util.Utils;
import com.black.core.util.Vfu;
import com.black.sql.SqlOutStatement;
import com.black.table.ColumnMetadata;
import com.black.table.TableMetadata;


import java.util.*;

public class SelectSqlValueResolver implements SqlValueGroupHandler {
    @Override
    public boolean support(Configuration configuration) {
        SQLMethodType methodType = configuration.getMethodType();
        return !(configuration instanceof AppearanceConfiguration) &&
                (methodType == SQLMethodType.QUERY ||
                methodType == SQLMethodType.UPDATE ||
                methodType == SQLMethodType.DELETE);
    }

    @Override
    public List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep) {
        TableMetadata tableMetadata = configuration.getTableMetadata();
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        SqlValueGroup valueGroup = new SqlValueGroup();
        Map<String, Map<OperationType, Set<SqlVariable>>> group = statement.group();
        for (String column : group.keySet()) {
            Map<OperationType, Set<SqlVariable>> typeSetMap = group.get(column);
            for (OperationType type : typeSetMap.keySet()) {
                Set<SqlVariable> sqlVariables = typeSetMap.get(type);
                ColumnMetadata columnMetadata = tableMetadata.getColumnMetadata(column);
                MappingVal mappingVal = boundStatement.getSingleMappingVal(column, type);
                if (mappingVal == null){
                    throw new SQLSException("variable : " + column + " missing value injection");
                }
                //拿到具体的值
                Object paramValue = mappingVal.getParamValue();
                //如果需要多个填充符 ?, ? ,?
                if (sqlVariables.size() > 1){
                    //将此时参数封装成 list
                    List<Object> objectList = SQLUtils.wrapList(paramValue);
                    if (objectList.size() != sqlVariables.size()){
                        throw new SQLSException("The value corresponding to the multi parameter variable " +
                                "is a single instance or the quantity does not correspond, column: " + column + " -- value : " +
                                paramValue);
                    }
                    ArrayUtils.loops(sqlVariables, objectList, (v, o) -> {
                        SqlValue sqlValue = new SqlValue(v, o, columnMetadata);
                        for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
                            listener.postSqlValue(configuration, sqlValue);
                        }
                        valueGroup.addValue(sqlValue);
                    });
                }else if (sqlVariables.size() == 1){
                    if(paramValue instanceof List){
                        List<?> list = (List<?>) paramValue;
                        paramValue = list.isEmpty() ? null : list.get(0);
                    }

                    SqlValue sqlValue = new SqlValue(Utils.firstElement(sqlVariables), paramValue, columnMetadata);
                    for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
                        listener.postSqlValue(configuration, sqlValue);
                    }
                    valueGroup.addValue(sqlValue);
                }
            }
        }
        return Vfu.as(valueGroup);
    }
}
