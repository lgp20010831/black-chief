package com.black.core.sql.code.wrapper;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.WriedQueryStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

public class QueryStatementHandler extends AbstractStatemenHandler{
    @Override
    public boolean support(WrapperConfiguration configuration) {
        return configuration.getClass().equals(WrapperConfiguration.class);
    }

    @Override
    public boolean supportCreateConfiguration(MethodWrapper wrapper) {
        return wrapper.parameterHasAnnotation(WriedQueryStatement.class);
    }

    @Override
    public Object handler(Object arg, WrapperConfiguration configuration) {
        SqlOutStatement statement = SqlWriter.select(configuration.getTableName());
        return handlerObject(arg, statement, configuration);
    }
}
