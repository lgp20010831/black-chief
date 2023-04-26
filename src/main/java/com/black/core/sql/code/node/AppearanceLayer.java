package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.AppearanceResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.util.SQLUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppearanceLayer extends AbstractSqlsPipeNode {

    private final Map<Configuration, AppearanceResolver> resolverHeadCache = new ConcurrentHashMap<>();

    private final Map<Configuration, AppearanceResolver> resolverTailCache = new ConcurrentHashMap<>();

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        Configuration configuration = arg.getConfiguration();
        ApplicationUtil.programRunMills(() ->{
            AppearanceResolver resolver = resolverHeadCache.computeIfAbsent(configuration, c -> {
                for (AppearanceResolver appearanceResolver : configuration.getGlobalSQLConfiguration().getAppearanceResolvers()) {
                    if (appearanceResolver.headSupport(configuration)) {
                        return appearanceResolver;
                    }
                }
                return null;
            });
            if (resolver != null){
                resolver.doHeadAppearance(this, configuration, arg);
            }

        }, SQLUtils.getLogString(configuration, "~~~>>AppearanceLayer head"), configuration.getLog());

        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        Configuration configuration = arg.getConfiguration();
        ApplicationUtil.programRunMills(() ->{
            AppearanceResolver resolver = resolverTailCache.computeIfAbsent(configuration, c -> {
                for (AppearanceResolver appearanceResolver : configuration.getGlobalSQLConfiguration().getAppearanceResolvers()) {
                    if (appearanceResolver.tailSupport(configuration)) {
                        return appearanceResolver;
                    }
                }
                return null;
            });
            if (resolver != null){
                resolver.doTailAppearance(this, configuration, arg);
            }
        }, SQLUtils.getLogString(configuration, "AppearanceLayer tail"), configuration.getLog());

        super.tailfireRunnable(current, arg);
    }
}
