package com.black.core.sql.code.impl.result_impl;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.session.SQLMethodType;

public class BooleanResultResolver implements ExecuteResultResolver {

    @Override
    public boolean support(SQLMethodType type, MethodWrapper mw) {
        return type != SQLMethodType.QUERY &&
                (mw.getReturnType().equals(boolean.class) || mw.getReturnType().equals(Boolean.class));
    }

    @Override
    public Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper wrapper, boolean skip) {
        return body.getUpdateCount() > 0;
    }
}
