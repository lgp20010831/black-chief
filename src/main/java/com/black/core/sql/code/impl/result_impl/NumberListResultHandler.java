package com.black.core.sql.code.impl.result_impl;

import com.black.core.json.ReflexUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NumberListResultHandler implements ExecuteResultResolver {

    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        String name = returnType.getName();
        Class<?> nc = returnType;
        if (ClassWrapper.isBasic(name)) {
            nc = ClassWrapper.pack(name);
        }
        Class<?>[] gv;
        return Number.class.isAssignableFrom(nc) ||
                (List.class.isAssignableFrom(returnType) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                        && Number.class.isAssignableFrom(gv[0]));
    }

    @Override
    public Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper mw, boolean skip) throws SQLException {
        Class<?> returnType = mw.getReturnType();
        String name = returnType.getName();
        Class<?> nc = returnType;
        boolean single = true;
        if(List.class.isAssignableFrom(nc)){
            Class<?>[] gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod());
            nc = gv[0];
            single = false;
        }

        if (ClassWrapper.isBasic(name)) {
            nc = ClassWrapper.pack(name);
        }
        List<Number> result = new ArrayList<>();
        if (configuration.getMethodType() == SQLMethodType.QUERY){
            ResultSet queryResult = body.getQueryResult();
            Assert.notNull(queryResult, "query method but not exist result set");
            try {
                ResultSetMetaData metaData = queryResult.getMetaData();
                while (queryResult.next()) {
                    if (metaData.getColumnCount() != 1){
                        throw new SQLSException("查询方法返回单例结果则需要 sql 返回值列数指明为 1 ");
                    }
                    if (Integer.class.isAssignableFrom(nc))
                        result.add(queryResult.getInt(1));
                    else if (Double.class.isAssignableFrom(nc))
                        result.add(queryResult.getDouble(1));
                    else if (Float.class.isAssignableFrom(nc))
                        result.add(queryResult.getFloat(1));
                    else if (Short.class.isAssignableFrom(nc))
                        result.add(queryResult.getShort(1));
                    else if (Byte.class.isAssignableFrom(nc))
                        result.add(queryResult.getByte(1));
                    else if (Long.class.isAssignableFrom(nc))
                        result.add(queryResult.getLong(1));
                }
            }finally {
                SQLUtils.closeResultSet(queryResult);
            }
        }else {
             result.add(body.getUpdateCount());
        }

        return single ? result.isEmpty() ? null : result.get(0) : result;
    }
}
