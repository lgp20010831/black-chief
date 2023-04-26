package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.And;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.sql.SqlOutStatement;

public class AndArguramentResolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(And.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        SqlOutStatement statement = ep.getNhStatement().getStatement();
        if (!statement.isAndCon()) {
            statement.filp();
        }
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        return null;
    }
}
