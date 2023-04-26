package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.util.SQLUtils;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ResultLayer extends AbstractSqlsPipeNode {


    private final Map<Method, ExecuteResultResolver> resultResolverCache = new ConcurrentHashMap<>();

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        Configuration configuration = arg.getConfiguration();
        MethodWrapper mw = configuration.getMethodWrapper();
        AtomicReference<Object> result = new AtomicReference<>();
        ApplicationUtil.programRunMills(() ->{
            try{
                ExecuteResultResolver resultResolver = resultResolverCache.computeIfAbsent(mw.getMethod(), m -> {
                    for (ExecuteResultResolver resolver : configuration.getGlobalSQLConfiguration()
                            .getResultResolvers()) {
                        if (resolver.support(configuration.getMethodType(), mw)) {
                            return resolver;
                        }
                    }
                    return null;
                });
                if (resultResolver != null){
                    if (!(configuration instanceof AppearanceConfiguration &&
                            configuration.getMethodType() != SQLMethodType.QUERY)){
                        result.set(resultResolver.doResolver(arg.getExecuteBody(), configuration, mw,
                                configuration instanceof AppearanceConfiguration));
                    }
                }
            }catch (SQLException e){
                throw new SQLSException("parse result error", e);
            }
        }, SQLUtils.getLogString(configuration, "~~~>>ResultLayer"), configuration.getLog());

        arg.setResult(result.get());
        super.tailfireRunnable(current, arg);
    }
}
