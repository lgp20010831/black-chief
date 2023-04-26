package com.black.core.aop.servlet.plus;

import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;
import com.black.core.aop.servlet.plus.handler.PlusResolverScheduler;
import com.black.core.aop.servlet.plus.handler.PlusVariableResolver;
import com.black.core.aop.servlet.plus.parse.ArgumentParser;
import com.black.core.aop.servlet.plus.parse.ArgumentParserScheduler;
import com.black.core.aop.servlet.plus.scan.MethodScanner;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.InstanceClientAdapter;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@GlobalAround
public class MybatisPlusAroundResolver implements GlobalAroundResolver, InstanceClientAdapter {

    /**
     * 首先要有扫描的功能, 扫描整个方法, 所有参数,然后封装
     * 1.对方法的封装:{@link PlusMethodWrapper}
     * 2. 对实体类的封装: {@link EntryWrapper}
     *
     * 解析结束以后就是处理, 目前有三种情况
     * 1.{@link WriedQueryWrapper} 查询:
     *      1.参数 json
     *      2.参数 String
     *      3.参数实体类
     *      4.array
     * 2.{@link WriedUpdateWrapper}
     *     1.参数json
     *     2.参数String
     *     3.参数实体类
     *     4.array
     *  3.{@link WriedDeletionWrapper}
     *     1.参数 json
     *     2.参数String
     *     3.参数array
     *     4.参数实体类
     *
     *   4.{@link WriedEntity}
     *     1.实体类
     *
     *   首先这四大中大的情况就是 4个处理类
     *   小情况, 又是子处理器
     *'
     *   有一点明确, 如果是array， 那么一定是单个元素
     *
     */
    private Boolean cancel;

    private final Map<Method, PlusMethodWrapper> plusMethodWrapperCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, MappingPolicy> policyMap = new ConcurrentHashMap<>();

    private final MappingPolicy defaultPolocy = MappingPolicy.FieldName$column_name;

    private final ArgumentParserScheduler argumentParserScheduler;

    private final PlusResolverScheduler plusResolverScheduler;

    private final List<PlusVariableResolver> resolvers;

    private final MethodScanner scanner;

    public MybatisPlusAroundResolver() {
        argumentParserScheduler = new ArgumentParserScheduler();
        plusResolverScheduler = new PlusResolverScheduler();
        resolvers = plusResolverScheduler.getVariableResolvers();
        scanner = new MethodScanner();
    }

    @Override
    public CollectedCilent getClient() {
        return argumentParserScheduler;
    }

    @Override
    public int getOrder() {
        return 54;
    }

    private boolean cancel(){
        if (cancel == null){
            try {
                Class.forName("com.baomidou.mybatisplus.core.mapper.BaseMapper");
                cancel = false;
            } catch (ClassNotFoundException e) {
                cancel = true;
            }
        }
        return cancel;
    }

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        if (cancel()){
            return args;
        }
        Method method = httpMethodWrapper.getHttpMethod();
        //扫描方法
        scanner.scannerMethod(method, httpMethodWrapper);
        //获取两个wrapper
        PlusMethodWrapper plusWrapper = scanner.getPlusWrapper(method);
        EntryWrapper entryWrapper = scanner.getEntryWrapper(method);
        if (plusWrapper != null && entryWrapper != null){
            QueryWrapperConfiguration configuration = scanner.getConfiguration(method);
            if (configuration == null){
                configuration = scanner.handlerConfiguration(method, plusWrapper);
            }
            entryWrapper.groupBy(configuration);
            if (plusWrapper.isEffective()){
                try {
                    entryWrapper.reset(args[plusWrapper.getArgIndex()]);
                    for (PlusVariableResolver resolver : resolvers) {
                        if (resolver.support(plusWrapper, entryWrapper)) {
                            args[plusWrapper.getWrapperIndex()] = resolver.handler(plusWrapper, entryWrapper, configuration);
                        }
                    }

                    Collection<Object> parsers = argumentParserScheduler.getParsers();
                    for (Object parser : parsers) {
                        ArgumentParser argumentParser = (ArgumentParser) parser;
                        if (argumentParser.support(plusWrapper, entryWrapper)) {
                            args = argumentParser.parseArgument(plusWrapper, entryWrapper, args);
                        }
                    }
                }finally {
                    entryWrapper.clear();
                }
                return args;
            }else {
                return GlobalAroundResolver.super.handlerArgs(args, httpMethodWrapper);
            }
        }
        return GlobalAroundResolver.super.handlerArgs(args, httpMethodWrapper);
    }
}
