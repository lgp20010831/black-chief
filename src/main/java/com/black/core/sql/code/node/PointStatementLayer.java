package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.util.Assert;

public class PointStatementLayer extends AbstractSqlsPipeNode {

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        Configuration configuration = arg.getConfiguration();
        MethodWrapper mw = configuration.getMethodWrapper();
        if (configuration instanceof AppearanceConfiguration){
            super.headfireRunnable(current, arg);
            return;
        }
        ParameterWrapper parameter = mw.getSingleParameterByType(BoundStatement.class);
        if (parameter != null){
            Object[] args = arg.getArgs();
            Assert.notNull(args, "args is null");
            BoundStatement boundStatement = (BoundStatement) args[parameter.getIndex()];
            arg.setNhStatement(boundStatement);
        }
        super.headfireRunnable(current, arg);
    }


    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
