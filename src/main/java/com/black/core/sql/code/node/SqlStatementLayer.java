package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.DefaultSqlStatementCreator;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SqlStatementLayer extends AbstractSqlsPipeNode {

    private final Map<Configuration, DefaultSqlStatementCreator> creatorCache = new ConcurrentHashMap<>();

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        doCreateSqlStatement(arg);
        super.headfireRunnable(current, arg);
    }

    private void doCreateSqlStatement(ExecutePacket arg){
        if (arg.getNhStatement() == null) {
            final Configuration configuration = arg.getConfiguration();
            DefaultSqlStatementCreator statementCreator = creatorCache.computeIfAbsent(configuration, m -> {
                synchronized (configuration.getGlobalSQLConfiguration().getCreators()){
                    final LinkedBlockingQueue<DefaultSqlStatementCreator> creators = configuration.getGlobalSQLConfiguration().getCreators();
                    for (DefaultSqlStatementCreator creator : creators) {
                        if (creator.support(configuration)) {
                            return creator;
                        }
                    }
                    return null;
                }
            });
            if (statementCreator == null){
                creatorCache.remove(configuration);
                throw new SQLSException("NO CREATOR CAN CREATE STATEMENT, TARGET METHOD TYPE: ["
                        + configuration.getMethodType() + "], METHOD NAME: [" +
                        configuration.getMethodWrapper().getName() + "], TABLE NAME: [" + configuration.getTableName() +"]");
            }
            BoundStatement statement = statementCreator.createStatement(configuration);
            Assert.notNull(statement, "creator cannot create an empty statement");
            arg.setNhStatement(statement);
        }
        Configuration configuration = arg.getConfiguration();
        for (GlobalSQLRunningListener listener : configuration.getRunningListener()) {
            arg.setStatement(listener.processorStatement(arg.getStatement(), configuration, arg));
        }
    }
    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
