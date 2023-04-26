package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.PrepareFinishResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.util.Assert;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class StatementFinishLayer extends AbstractSqlsPipeNode {

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        Configuration configuration = arg.getConfiguration();
        BoundStatement nhStatement = arg.getNhStatement();
        Assert.notNull(nhStatement, "statement exception is null");
        boolean intercept = false;
        for (PrepareFinishResolver resolver : configuration.getGlobalSQLConfiguration().getPrepareFinishResolvers()) {
            if (resolver.support(configuration)) {
                intercept = resolver.handler(configuration, arg, arg.getStatement());
            }
            if (intercept){break;}
        }
        if (intercept){
            if (log.isInfoEnabled()) {
                log.info("intercept finish layer");
            }
            return;
        }
        nhStatement.getStatement().calibration(configuration.getTableMetadata());
        nhStatement.getStatement().flush();
        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
