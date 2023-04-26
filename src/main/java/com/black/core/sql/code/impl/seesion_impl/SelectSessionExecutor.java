package com.black.core.sql.code.impl.seesion_impl;

import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.PointSessionExecutor;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.sqls.SqlValueGroup;

import java.util.List;

public class SelectSessionExecutor implements PointSessionExecutor {
    @Override
    public boolean support(Configuration configuration) {
        return configuration.getMethodType() == SQLMethodType.QUERY;
    }

    @Override
    public ExecuteBody doExecute(ExecutePacket ep) {
        Configuration configuration = ep.getConfiguration();
        SQLSignalSession session = configuration.getSession();
        String sql = ep.getNhStatement().getStatement().toString();
        List<SqlValueGroup> valueGroupList = ep.getValueGroupList();
        for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
            sql = listener.postQuerySql(configuration, sql, valueGroupList);
        }
        return session.pipelineSelect(sql, valueGroupList);
    }
}
