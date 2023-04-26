package com.black.core.sql.code.impl.appearance_impl;

import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.Query;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.pattern.AppearanceFactory;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
public class SelectAppearanceReolver extends AbstractAppearanceResolver {

    @Override
    public boolean tailSupport(Configuration configuration) {
        MethodWrapper mw = configuration.getMethodWrapper();
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.QUERY &&
                configuration.getMethodWrapper().hasAnnotation(Query.class) &&
                (Map.class.isAssignableFrom(returnType) ||
                        (Collection.class.isAssignableFrom(returnType) && (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                                && gv[0].equals(Map.class)));
    }

    @Override
    public void doTailAppearance(AbstractSqlsPipeNode node, Configuration configuration, ResultPacket rp) {
        Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
        Object argResult = rp.getResult();
        if (!(argResult instanceof Map || argResult instanceof List)){
            return;
        }
        super.doTailAppearance(node, configuration, rp);
    }

    @Override
    protected List<AppearanceConfiguration> parse(MethodWrapper mw, Configuration configuration, Connection connection) {
        Query query = mw.getAnnotation(Query.class);
        return AppearanceFactory.parse(query.value(), configuration, connection);
    }
}
