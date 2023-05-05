package com.black.sql_v2.handler;

import com.black.core.json.Trust;
import com.black.core.tools.BaseBean;
import com.black.core.tools.BeanUtil;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.JDBCEnvironmentLocal;

import java.util.Collection;
import java.util.Map;

public class InsertDataSqlHandler implements SqlStatementHandler {

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return statement instanceof InsertStatement;
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        JDBCEnvironmentLocal.getPack().setAttachment(param);
        return statement;
    }

    @Override
    public boolean support(Object param) {
        return param instanceof Collection || param instanceof Map || param instanceof BaseBean ||
                (param != null && BeanUtil.getPrimordialClass(param).isAnnotationPresent(Trust.class));
    }
}
