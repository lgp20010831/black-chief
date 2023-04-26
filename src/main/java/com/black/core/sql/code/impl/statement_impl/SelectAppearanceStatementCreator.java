package com.black.core.sql.code.impl.statement_impl;

import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DefaultSqlStatementCreator;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.util.StreamUtils;
import com.black.core.util.Utils;
import com.black.sql.SqlOutStatement;
import com.black.utils.ServiceUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SelectAppearanceStatementCreator extends AbstractSelectStatementCreator implements DefaultSqlStatementCreator {
    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof AppearanceConfiguration &&
                configuration.getMethodType() == SQLMethodType.QUERY;
    }

    @Override
    public BoundStatement createStatement(Configuration configuration) {

        SqlOutStatement statement = createSelectStatement(configuration);
        AppearanceConfiguration appearanceConfiguration = (AppearanceConfiguration) configuration;
        Object result = appearanceConfiguration.getEp().getRp().getResult();
        if (!(result instanceof List)){
            result = Collections.singletonList(result);
        }

        if (result instanceof List){
            List<Map<String, Object>> lastResult = (List<Map<String, Object>>) result;
            List<String> list = StreamUtils.mapList(lastResult, map -> {
                return ServiceUtils.getString(map, configuration.getPrimaryName());
            });

            if (Utils.isEmpty(list)){
                statement.writeAftSeq(appearanceConfiguration.getForeignKeyColumnName() + " is null");
            }else {
                statement.writeIn(appearanceConfiguration.getForeignKeyColumnName(), true, list);
            }

        }
        return new BoundStatement(statement);
    }
}
