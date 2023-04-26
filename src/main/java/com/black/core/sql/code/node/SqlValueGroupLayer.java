package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.util.SQLUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlValueGroupLayer extends AbstractSqlsPipeNode {

    private final Map<Configuration, SqlValueGroupHandler> groupHandlerCache = new ConcurrentHashMap<>();

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        Configuration configuration = arg.getConfiguration();
        ApplicationUtil.programRunMills(() ->{
            SqlValueGroupHandler sqlValueGroupHandler = groupHandlerCache.computeIfAbsent(configuration, c -> {
                for (SqlValueGroupHandler groupHandler : configuration.getGlobalSQLConfiguration()
                        .getGroupHandlers()) {
                    if (groupHandler.support(configuration)) {
                        return groupHandler;
                    }
                }
                return null;
            });
            if (sqlValueGroupHandler != null){
                List<SqlValueGroup> valueGroups = sqlValueGroupHandler.handler(configuration, arg);
                arg.setValueGroupList(valueGroups);
            }
        }, SQLUtils.getLogString(configuration, "~~~>>SqlValueGroupLayer"), configuration.getLog());
        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
