package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ConfigurationAnnotationResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.util.Assert;

public class ConfigurationAnnotationValueLayer extends AbstractSqlsPipeNode {

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        Configuration configuration = arg.getConfiguration();
        BoundStatement nhStatement = arg.getNhStatement();
        Assert.notNull(nhStatement, "statement exception is null");
        for (ConfigurationAnnotationResolver annotationResolver : configuration
                .getGlobalSQLConfiguration().getAnnotationResolvers()) {
            annotationResolver.doReolver(configuration, arg);
        }

        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
