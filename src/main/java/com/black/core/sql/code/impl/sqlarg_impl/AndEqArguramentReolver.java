package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.AndEq;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

public class AndEqArguramentReolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(AndEq.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        String columnName = getColumnName(pw, configuration);
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        if (!statement.isAndCon()) {
            statement.filp();
        }
        statement.writeEq(columnName, "?", false);
        boundStatement.addMV(new MappingVal(OperationType.SELECT, value, columnName));
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        AndEq annotation = pw.getAnnotation(AndEq.class);
        return configuration.convertColumn(StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
    }
}
