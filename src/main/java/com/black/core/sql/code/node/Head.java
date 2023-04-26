package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.conformity.StatementConformity;

public class Head extends AbstractSqlsPipeNode {

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        arg.getExecuteBody().close();
        StatementConformity conformity = arg.getConfiguration().getGlobalSQLConfiguration().getConformity();
        if (conformity != null){
            conformity.deposit(arg.getConfiguration(), arg.getResult());
        }
        super.tailfireRunnable(current, arg);
    }
}
