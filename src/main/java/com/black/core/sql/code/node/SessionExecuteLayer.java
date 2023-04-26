package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.PointSessionExecutor;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.util.SQLUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class SessionExecuteLayer extends AbstractSqlsPipeNode {

    private final Map<Method, PointSessionExecutor> executorCache = new ConcurrentHashMap<>();

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        Configuration configuration = arg.getConfiguration();
        AtomicReference<ExecuteBody> executeBody = new AtomicReference<>();
        PointSessionExecutor executor = executorCache.computeIfAbsent(configuration.getMethodWrapper().getMethod(), m -> {
            for (PointSessionExecutor sessionExecutor : configuration.getGlobalSQLConfiguration()
                    .getSessionExecutors()) {
                if (sessionExecutor.support(configuration)) {
                    return sessionExecutor;
                }
            }
            return null;
        });
        if (executor == null){
            throw new SQLSException("now executor to execute sql method");
        }
        ApplicationUtil.programRunMills(() ->{
            executeBody.set(executor.doExecute(arg));
        }, SQLUtils.getLogString(configuration, "~~~>>SessionExecuteLayer"), configuration.getLog());
        ResultPacket rp = arg.getRp();
        rp.setExecuteBody(executeBody.get());
        this.getPipeline().tailfire(rp);
        //执行 sql 语句
        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
