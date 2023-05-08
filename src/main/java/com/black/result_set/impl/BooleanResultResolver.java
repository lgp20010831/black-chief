package com.black.result_set.impl;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.xml.PrepareSource;
import com.black.result_set.ResultSetHandler;

import java.sql.ResultSet;

public class BooleanResultResolver implements ResultSetHandler {

    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        return type != SQLMethodType.QUERY &&
                (mw.getReturnType().equals(boolean.class) || mw.getReturnType().equals(Boolean.class));
    }

    @Override
    public Object resolve(ResultSet resultSet, Class<?> returnType, MethodWrapper mw, PrepareSource prepareSource, SQLMethodType methodType) {
        return true;
    }

}
