package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.NullNeglect;
import com.black.core.sql.code.condition.SqlConditionEngine;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlsArguramentResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

public abstract class AbstractArguramentResolver implements SqlsArguramentResolver {

    @Override
    public void doResolver(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        if (value == null){
            String defaultValue = nullValueResolve(configuration, ep, pw);
            if (defaultValue == null){
                return;
            }
            value = defaultValue;
        }
        if (SqlConditionEngine.isConditional(configuration)){
            //do condition
            if (!SqlConditionEngine.getInstance().resolveCondition(pw.getParameter(), ep.getOriginalArgs())) {
                return;
            }
        }
        doCommon(configuration, ep, value, pw);
    }

    abstract void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw);

    //当传入参数为空时的处理
    protected String nullValueResolve(Configuration configuration, ExecutePacket ep, ParameterWrapper pw){
        NullNeglect annotation = pw.getAnnotation(NullNeglect.class);
        SqlOutStatement statement = ep.getStatement();

        if (annotation == null){
            //默认将这列设置等于 null
            String columnName = getColumnName(pw, configuration);
            if (columnName != null){
                String nullSql = columnName + " is null";
                statement.writeAftSeq(nullSql);
            }
            return null;
        }else {
            String value = annotation.value();
            return StringUtils.hasText(value) ? value : null;
        }
    }

    protected abstract String getColumnName(ParameterWrapper pw, Configuration configuration);
}
