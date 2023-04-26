package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.Like;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

public class LikeArguramentResolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(Like.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        if (value == null) return;
        String columnName = getColumnName(pw, configuration);
        SqlOutStatement statement = ep.getNhStatement().getStatement();
        statement.writeLike(columnName, value.toString());
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        Like annotation = pw.getAnnotation(Like.class);
        return configuration.convertColumn(StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
    }
}
