package com.black.core.sql.code.impl.result_impl;

import com.black.core.json.ReflexUtils;
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

public class StringSelectResultHandler implements ExecuteResultResolver {
    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return type == SQLMethodType.QUERY && (returnType.equals(String.class) ||
                (List.class.isAssignableFrom(returnType) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                        && gv[0].equals(String.class)));
    }

    @Override
    public Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper mw, boolean skip) throws SQLException {
        ResultSet queryResult = body.getQueryResult();
        Assert.notNull(queryResult, "query method but not exist result set");
        List<String> resultList = new ArrayList<>();
        try {
            ResultSetMetaData metaData = queryResult.getMetaData();
            while (queryResult.next()) {
                if (metaData.getColumnCount() != 1){
                    throw new SQLSException("查询方法返回单例结果则需要 sql 返回值列数指明为 1 ");
                }
               resultList.add(queryResult.getString(1));
            }

            if (mw.getReturnType().equals(String.class)){
                return resultList.isEmpty() ? null : resultList.get(0);
            }else {
                return resultList;
            }
        }finally {
            SQLUtils.closeResultSet(queryResult);
        }
    }
}
