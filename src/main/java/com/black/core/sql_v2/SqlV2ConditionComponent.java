package com.black.core.sql_v2;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.pattern.MethodInvoker;
import com.black.pattern.NameAndValue;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.chain.*;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.action.AbstractProvideSupportChiefApiController;
import com.black.sql_v2.javassist.*;
import com.black.sql_v2.javassist.aop.SqlV2AopUtils;
import com.black.table.TableUtils;

import java.sql.Connection;
import java.util.*;

@ChainClient
public class SqlV2ConditionComponent implements OpenComponent, CollectedCilent {

    private final IoLog log = LogFactory.getArrayLog();

    private final Set<Object> virtualClients = new HashSet<>();

    private final Set<Object> optConfigurerClients = new HashSet<>();

    private final Set<Object> proxyClients = new HashSet<>();

    private int virtualSize = 0;

    private final MethodReflectionIntoTheParameterProcessor parameterProcessor;

    public SqlV2ConditionComponent() {
        parameterProcessor = new MethodReflectionIntoTheParameterProcessor();
        parameterProcessor.setGetParamAliasFunction(parameterWrapper -> {
            Opt annotation = parameterWrapper.getAnnotation(Opt.class);
            return annotation == null ? Sql.DEFAULT_ALIAS : annotation.value();
        });
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws Throwable {
        log.info("handlerProxy ....");
        handlerProxy();

        log.info("handlerVirtual ....");
        handlerVirtual();
        log.info("create virtual controller: {}", virtualSize);

        log.info("handlerOptConfig ....");
        handlerOptConfig();
    }


    private void handlerVirtual(){
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        for (Object client : virtualClients) {
            Class<?> type = (Class<?>) client;
            Object virtual = beanFactory.getSingleBean(type);
            invokeVirtual(virtual);
        }
    }
    private void invokeVirtual(Object virtual){
        SqlV2JavassistControllerManager manager = new SqlV2JavassistControllerManager();
        manager.setSuperClass(AbstractProvideSupportChiefApiController.class);

        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(virtual);
        Collection<MethodWrapper> methods = classWrapper.getMethodByAnnotation(Virtual.class);

        Set<SqlExecutor> runExecutors = new HashSet<>();
        try {
            for (MethodWrapper mw : methods) {
                if (mw.getReturnType().equals(void.class)){
                    continue;
                }
                Virtual annotation = mw.getAnnotation(Virtual.class);
                String alias = annotation.optAlias();
                SqlExecutor executor = Sql.opt(alias);
                runExecutors.add(executor);
                Connection connection = executor.getConnection();
                Set<String> currentTables = TableUtils.getCurrentTables(alias, connection);

                //构造参数, 执行
                Object[] args = parameterProcessor.parse(mw, new Object[]{currentTables}, executor);
                Object result = mw.invoke(virtual, args);
                List<Object> list = SQLUtils.wrapList(result);
                for (Object value : list) {
                    String tableName = String.valueOf(value);
                    if (currentTables.contains(tableName)){
                        try {

                            SqlV2ProxyJavassistAnnexSpringServletHandler.createAndRegister(tableName, alias, manager);
                            virtualSize++;
                        }catch (Throwable ex){
                            log.error("[SQL] -- Failed to create virtual interface: {}", tableName);
                            CentralizedExceptionHandling.handlerException(ex);
                        }
                    }else {
                        log.error("[SQL] -- not find tableName: {} in alias env: {}", tableName, alias);
                    }
                }
            }
        }finally {
            for (SqlExecutor executor : runExecutors) {
                executor.closeConnection();
            }
        }

    }


    private void handlerProxy(){
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        for (Object client : proxyClients) {
            Class<?> type = (Class<?>) client;
            Object proxy = beanFactory.getSingleBean(type);
            invokeProxy(proxy);
        }
    }

    private void invokeProxy(Object proxy){
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(proxy);
        Collection<MethodWrapper> methods = classWrapper.getMethodByAnnotation(Proxy.class);
        for (MethodWrapper mw : methods) {
            MethodInvoker invoker = new MethodInvoker(mw);
            invoker.setInvokeBean(proxy);
            Proxy annotation = mw.getAnnotation(Proxy.class);
            String methodName = annotation.methodName();
            methodName = StringUtils.hasText(methodName) ? methodName : mw.getName();
            String alias = annotation.alias();
            SqlExecutor executor = Sql.opt(alias);
            String[] tableNames = annotation.tableNames();

            if (tableNames.length == 1 && tableNames[0].equals("*")){
                Connection connection = executor.getConnection();
                Set<String> currentTables = TableUtils.getCurrentTables(alias, connection);
                try {
                    for (String tableName : currentTables) {
                        proxyTable(tableName, alias, methodName, invoker);
                    }
                }finally {
                    executor.closeConnection();
                }
            }else {
                for (String tableName : tableNames) {
                    proxyTable(tableName, alias, methodName, invoker);
                }
            }
        }
    }

    protected void proxyTable(String tableName, String alias, String methodName, MethodInvoker invoker){
        SqlV2ProxyRegister register = SqlV2ProxyRegister.getInstance();
        GroupKeys groupKeys = new GroupKeys(alias, tableName, methodName);
        log.trace("register proxy method invoker: [{} --> {} --> {}]", alias, tableName, methodName);
        register.register(groupKeys, invoker);
    }

    private void handlerOptConfig(){
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        for (Object client : optConfigurerClients) {
            Class<?> type = (Class<?>) client;
            Object config = beanFactory.getSingleBean(type);
            invokeConfig(config);
        }
    }

    private void invokeConfig(Object config){
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(config);
        Collection<MethodWrapper> methods = classWrapper.getMethodByAnnotation(OptConfigurer.class);

        Map<String, Map<Class<?>, List<NameAndValue>>> attr = new LinkedHashMap<>();
        Set<String> aliases = Sql.getAliases();
        for (String alias : aliases) {
            Map<Class<?>, List<NameAndValue>> map = SqlV2AopUtils.getAttrByAlias(alias);
            attr.put(alias, map);
        }

        //关键在于处理参数
        for (MethodWrapper mw : methods) {
            Object[] args = new Object[mw.getParameterCount()];
            for (ParameterWrapper pw : mw.getParameterArray()) {
                Object arg = SqlV2AopUtils.getArgByAttr(attr, pw);
                args[pw.getIndex()] = arg;
            }
            log.debug("invoke config method: {}", mw.getName());
            mw.invoke(config, args);
        }

    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("virtual", cki -> {
            return BeanUtil.isSolidClass(cki) && AnnotationUtils.isPertain(cki, VirtualHybrid.class);
        }).instance(false);

        register.begin("opt", cki -> {
            return BeanUtil.isSolidClass(cki) && AnnotationUtils.isPertain(cki, OptConfigurer.class);
        }).instance(false);

        register.begin("proxy", cki -> {
            return BeanUtil.isSolidClass(cki) && AnnotationUtils.isPertain(cki, ProxyHybrid.class);
        }).instance(false);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("virtual".equals(resultBody.getAlias())){
            virtualClients.addAll(resultBody.getCollectSource());
        }

        if ("opt".equals(resultBody.getAlias())){
            optConfigurerClients.addAll(resultBody.getCollectSource());
        }

        if ("proxy".equals(resultBody.getAlias())){
            proxyClients.addAll(resultBody.getCollectSource());
        }
    }
}
