package com.black.core.sql.code.impl.sqlvalue_impl;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.SqlValueGroup;

import java.util.ArrayList;
import java.util.List;

public class SelectAppearanceSqlValueResolver implements SqlValueGroupHandler {
    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof AppearanceConfiguration &&
                configuration.getMethodType() == SQLMethodType.QUERY;
    }

    @Override
    public List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep) {
        return new ArrayList<>();
    }
}
