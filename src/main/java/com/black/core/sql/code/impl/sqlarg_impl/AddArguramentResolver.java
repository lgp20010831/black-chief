package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.Add;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

public class AddArguramentResolver extends AbstractCommonArguramentResolver{

    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(Add.class);
    }


    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        String columnName = getColumnName(pw, configuration);
        if (statement.exisOperation(columnName, OperationType.INSERT)) {
            statement.replaceOperation(columnName, OperationType.INSERT, SQLUtils.getString(value), false);
        }else {
            statement.insertVariable(columnName, SQLUtils.getString(value));
        }
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        Add annotation = pw.getAnnotation(Add.class);
        return configuration.convertColumn(StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
    }
}
