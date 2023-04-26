package com.black.core.sql.code.impl.result_impl;

import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class MapSelectResultHandler implements ExecuteResultResolver {
    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return type == SQLMethodType.QUERY && (Map.class.isAssignableFrom(returnType) ||
                (Collection.class.isAssignableFrom(returnType) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                        && gv[0].equals(Map.class)));
    }

    @Override
    public Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper mw, boolean skip) throws SQLException {
        GlobalSQLConfiguration globalSQLConfiguration = configuration.getGlobalSQLConfiguration();
        Log log = globalSQLConfiguration.getLog();
        boolean singlon = !skip && checkMethod(mw);
        Collection<Map<String, Object>> resultMapList = createCollection(mw);
        AliasColumnConvertHandler convertHandler = configuration.getColumnConvertHandler();
        ResultSet resultSet = body.getQueryResult();
        Assert.notNull(resultSet, "query result is null");
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()){
            Map<String, Object> resultMap = createMap(configuration);
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Object columnResult = resultSet.getObject(i);
                if (columnResult != null || !configuration.isFilterNullValueKey()){
                    resultMap.put(convertHandler.convertAlias(columnName), columnResult);
                }
            }
            for (GlobalSQLRunningListener listener : configuration
                    .getGlobalSQLConfiguration()
                    .getApplicationContext()
                    .getSQLRunningListeners()) {
                listener.postMapResult(configuration, resultMap);
            }
            resultMapList.add(resultMap);
        }

        SQLUtils.closeResultSet(resultSet);
        if (log.isDebugEnabled()) {
            log.debug("<== total:" + resultMapList.size());
        }

        for (GlobalSQLRunningListener listener : configuration
                .getGlobalSQLConfiguration()
                .getApplicationContext()
                .getSQLRunningListeners()) {
            listener.postFinallyMapResult(configuration, resultMapList);
        }
        if (singlon){
            if (resultMapList.size() > 1){
                throw new SQLSException("The return value type is specified as a single instance, " +
                        "but there are multiple results");
            }

            if (resultMapList.isEmpty()){
                return null;
            }

            if (resultMapList instanceof List){
                return ((List<Map<String, Object>>) resultMapList).get(0);
            }
            throw new SQLSException("It won't happen");
        }
        return resultMapList;
    }

    protected boolean checkMethod(MethodWrapper wrapper){
        Class<?> returnType = wrapper.getReturnType();
        if (Map.class.isAssignableFrom(returnType)){
            return true;
        }else if (Collection.class.isAssignableFrom(returnType)){
            return false;
        }
        throw new SQLSException("The return value of query method only supports collection or map");
    }


    public Collection<Map<String, Object>> createCollection(MethodWrapper wrapper){
        Class<?> returnType = wrapper.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)){
            if (Set.class.isAssignableFrom(returnType)){
                if (!BeanUtil.isSolidClass(returnType)){
                    return new HashSet<>();
                }
            }
            if (List.class.isAssignableFrom(returnType)){
                if (!BeanUtil.isSolidClass(returnType)){
                    return new ArrayList<>();
                }
            }
            if (BeanUtil.isSolidClass(returnType)){
                return (Collection<Map<String, Object>>) ReflexUtils.instance(returnType);
            }
        }
        return new ArrayList<>();
    }

    public Map<String, Object> createMap(Configuration configuration){
        Class<? extends Map> mapType = configuration.getMapType();
        return ReflexUtils.instance(mapType);
    }
}
