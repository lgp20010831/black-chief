package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.In;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

import java.util.List;

public class InArguramentResolver extends AbstractCommonArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(In.class);
    }

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        Class<?> type = pw.getType();
        BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        if (!statement.isAndCon()) {
            statement.filp();
        }
        In annotation = pw.getAnnotation(In.class);
        String columnName = getColumnName(pw, configuration);
        List<Object> list = SQLUtils.wrapList(value);
        if (list.isEmpty()){
            statement.writeAftSeq(columnName + " is null ");
            return;
        }

        int size = list.size();
        String[] array = SQLUtils.createW(size);
        if (annotation.isNot()){
            statement.writeNotIn(columnName, false, array);
        }else {
            statement.writeIn(columnName, false, array);
        }
        boundStatement.addMV(new MappingVal(OperationType.SELECT, list, columnName));
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        In annotation = pw.getAnnotation(In.class);
        return configuration.convertColumn(StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
    }
}
