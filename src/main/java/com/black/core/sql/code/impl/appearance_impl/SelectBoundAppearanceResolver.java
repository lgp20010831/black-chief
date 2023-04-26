package com.black.core.sql.code.impl.appearance_impl;

import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.BoundSelects;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.config.BoundConfig;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.util.AnnotationUtils;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SelectBoundAppearanceResolver extends AbstractBoundAppearanceResolver{

    @Override
    public boolean tailSupport(Configuration configuration) {
        MethodWrapper mw = configuration.getMethodWrapper();
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return
                configuration.getMethodType() == SQLMethodType.QUERY &&
                configuration.getMethodWrapper().hasAnnotation(BoundSelects.class)&&
                (Map.class.isAssignableFrom(returnType) ||
                        (Collection.class.isAssignableFrom(returnType) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                                && gv[0].equals(Map.class)));
    }

    @Override
    public void doTailAppearance(AbstractSqlsPipeNode node, Configuration configuration, ResultPacket rp) {
        Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
        Object argResult = rp.getResult();
        if (argResult == null){
            return;
        }

        if(argResult instanceof Map){
            Map<String, Object> map = (Map<String, Object>) argResult;
            if (map.isEmpty()){
                return;
            }
        }
        if (argResult instanceof List){
            List<?> list = (List<?>) argResult;
            if (list.isEmpty()){
                return;
            }
        }

        if (!(argResult instanceof Map || argResult instanceof List)){
            return;
        }
        super.doTailAppearance(node, configuration, rp);
    }

    @Override
    protected BoundConfig loadConfig(MethodWrapper mw, Configuration configuration, Connection connection) {
        BoundSelects annotation = mw.getAnnotation(BoundSelects.class);
        return AnnotationUtils.loadAttribute(annotation, new BoundConfig());
    }
}
