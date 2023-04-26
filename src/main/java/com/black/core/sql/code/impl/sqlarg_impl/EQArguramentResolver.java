package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.EQ;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

public class EQArguramentResolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(EQ.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        String columnName = getColumnName(pw, configuration);
        statement.writeEq(columnName, "?", false);
        boundStatement.addMV(new MappingVal(OperationType.SELECT, value, columnName));
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        EQ annotation = pw.getAnnotation(EQ.class);
        return configuration.convertColumn(StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
    }
}
