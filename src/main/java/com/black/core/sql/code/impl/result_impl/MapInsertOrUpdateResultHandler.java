package com.black.core.sql.code.impl.result_impl;

import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.ResultColumns;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.code.sqls.ResultType;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.core.util.Utils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInsertOrUpdateResultHandler implements ExecuteResultResolver {
    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return (type == SQLMethodType.INSERT || type == SQLMethodType.UPDATE) && (Map.class.isAssignableFrom(returnType) ||
                (returnType.equals(List.class) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                        && gv[0].equals(Map.class)));
    }

    @Override
    public Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper mw, boolean skip) throws SQLException {
        List<Map<String, Object>> mapList = new ArrayList<>();
        int updateCount = body.getUpdateCount();
        Class<?> returnType = mw.getReturnType();
        ResultColumns annotation = mw.getAnnotation(ResultColumns.class);
        if (updateCount > 0){
            Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
            List<String> gGeneratedKeys = (List<String>) ResultSetThreadManager.getResultAndParse(ResultType.GeneratedKeys, body.getWrapper().getGeneratedKeys());
            String tableName;
            Assert.notNull(tableName = configuration.getTableName(), "table name is null, can not parse insert map result");
            SqlOutStatement statement;
            if (annotation != null){
                statement = SqlWriter.select(tableName, annotation.value());
            }else {
                statement = SqlWriter.select(tableName);
            }
            if (Utils.isEmpty(gGeneratedKeys)){
                statement.writeAftSeq(configuration.getPrimaryName() + " is null ");
            }else {
                statement.writeIn(configuration.getPrimaryName(), true, gGeneratedKeys);
            }
            statement.flush();
            String sql = statement.toString();
            Log log = configuration.getLog();
            if (log.isDebugEnabled()) {
                log.debug("==> In order to analyze map type result to invoke sql: [" + sql + "]");
            }
            PreparedStatement prepareStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet executeQuery = prepareStatement.executeQuery();
            try {
                ResultSetMetaData metaData = executeQuery.getMetaData();
                while (executeQuery.next()) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        String alias = configuration.convertAlias(metaData.getColumnName(i));
                        map.put(alias, executeQuery.getObject(i));
                    }
                    mapList.add(map);
                }
            }finally {
                SQLUtils.closeStatement(prepareStatement);
                SQLUtils.closeResultSet(executeQuery);
            }
        }
        if (Map.class.isAssignableFrom(returnType)){
            return mapList.isEmpty() ? null : mapList.get(0);
        }else {
            return mapList;
        }
    }
}
