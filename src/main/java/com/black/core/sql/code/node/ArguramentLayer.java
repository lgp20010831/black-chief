package com.black.core.sql.code.node;

import com.black.pattern.PipeNode;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.SqlsArguramentResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.util.Assert;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ArguramentLayer extends AbstractSqlsPipeNode {

    private final Map<Parameter, List<SqlsArguramentResolver>> resolverCache = new ConcurrentHashMap<>();

    @Override
    public void headfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ExecutePacket arg) {
        resolver(arg);
        super.headfireRunnable(current, arg);
    }


    private void resolver(ExecutePacket arg){
        Object[] paramArgs;
        Assert.notNull(paramArgs = arg.getArgs(), "method args must not is null");
        final Configuration configuration = arg.getConfiguration();
        final MethodWrapper mw = configuration.getMethodWrapper();
        for (ParameterWrapper pw : mw.getParameterWrappersSet()) {
            List<SqlsArguramentResolver> resolverList = resolverCache.computeIfAbsent(pw.getParameter(), p -> {
                List<SqlsArguramentResolver> list = new ArrayList<>();
                for (SqlsArguramentResolver resolver : configuration.getGlobalSQLConfiguration()
                        .getArguramentResolvers()) {
                    if (resolver.support(configuration, pw)) {
                        list.add(resolver);
                    }
                }
                return list;
            });
            for (SqlsArguramentResolver resolver : resolverList) {
                resolver.doResolver(configuration, arg, paramArgs[pw.getIndex()], pw);
            }
            if (resolverList.isEmpty() && log.isDebugEnabled())
                log.debug("参数:[{}] 没有对应的处理器", pw.getName());
        }
    }

    @Override
    public void tailfireRunnable(PipeNode<ExecutePacket, ResultPacket> current, ResultPacket arg) {
        super.tailfireRunnable(current, arg);
    }
}
