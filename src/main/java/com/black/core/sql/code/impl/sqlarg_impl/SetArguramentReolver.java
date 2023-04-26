package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.Set;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

public class SetArguramentReolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return configuration.getMethodType() == SQLMethodType.UPDATE &&
                pw.hasAnnotation(Set.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        String columnName = getColumnName(pw, configuration);
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        statement.writeSetVariable(columnName, "?");
        boundStatement.addMV(new MappingVal(OperationType.UPDATE, value, columnName));
    }

    @Override
    protected String nullValueResolve(Configuration configuration, ExecutePacket ep, ParameterWrapper pw) {
        String columnName = getColumnName(pw, configuration);
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        statement.writeSetVariable(columnName, "?");
        boundStatement.addMV(new MappingVal(OperationType.UPDATE, null, columnName));
        return null;
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        Set annotation = pw.getAnnotation(Set.class);
        return configuration.convertColumn(StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
    }
}
