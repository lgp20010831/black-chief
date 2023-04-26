package com.black.core.sql.code.impl.result_impl;

import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.code.sqls.ResultType;



import java.sql.SQLException;

import java.util.List;

public class StringInsertUpdateResultHandler implements ExecuteResultResolver {

    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return (type == SQLMethodType.INSERT || type == SQLMethodType.UPDATE) && (returnType.equals(String.class) ||
                (returnType.equals(List.class) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                        && gv[0].equals(String.class)));
    }

    @Override
    public Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper mw, boolean skip) throws SQLException {
        Class<?> returnType = mw.getReturnType();
        Object result = ResultSetThreadManager.getResultAndParse(ResultType.GeneratedKeys, body.getWrapper().getGeneratedKeys());
        if (result == null){
            throw new SQLSException("no handler to resolver generatedKeys");
        }
        List<String> list = (List<String>) result;
        if (returnType.equals(String.class)){
            return list.isEmpty() ? null : list.get(0);
        }else {
            return list;
        }
    }
}
