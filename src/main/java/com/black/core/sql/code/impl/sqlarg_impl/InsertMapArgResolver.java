package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.json.ReflexUtils;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlsArguramentResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
public class InsertMapArgResolver implements SqlsArguramentResolver {
    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        Class<?> type = pw.getType();
        Class[] gv;
        return !(configuration instanceof AppearanceConfiguration) && configuration.getMethodType() == SQLMethodType.INSERT &&
                (Map.class.isAssignableFrom(type) ||
                (List.class.isAssignableFrom(type) &&
                (gv = ReflexUtils.getMethodParamterGenericVals(pw.getParameter())).length == 1 &&
                Map.class.isAssignableFrom(gv[0])));
    }

    @Override
    public void doResolver(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        if (value instanceof Map){
            value = Collections.singletonList(value);
        }
        Object attachment = ep.attachment();
        if(attachment != null){
            if (log.isWarnEnabled()) {
                log.warn("too many confusion adding parameters");
            }
            return;
        }
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) value;
        for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
            mapList = listener.handlerData(mapList);
        }
        ep.attach(value);
    }
}
