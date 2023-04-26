package com.black.core.sql.code;


import com.black.pattern.Pipeline;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.*;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.mapping.Mapper;
import com.black.core.sql.code.mapping.MapperImpl;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.*;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.factory.PacketFactory;
import com.black.core.sql.fast.core.FastInsertProcessor;
import com.black.core.sql.run.*;
import com.black.core.util.Assert;
import com.black.core.util.MethodHandlerUtilsByCSDN;
import com.black.utils.LocalMap;
import lombok.extern.log4j.Log4j2;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

@SuppressWarnings("all") @Log4j2
public class MapperSQLProxy implements AgentLayer {

    private SQLSignalSession session;
    private final ClassWrapper<?> wrapper;
    private final GlobalSQLConfiguration configuration;
    private final Map<Method, MethodHandle> handleCache = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<RunSupport> runSupports = new LinkedBlockingQueue<>();
    private final Map<Method, RunSupport> runSupportCache = new ConcurrentHashMap<>();
    private final Map<Method, Supplier<Configuration>> supplierMutes = new ConcurrentHashMap<>();
    private final LocalMap<Method, Configuration> configurationLocalCache = new LocalMap<>();

    public MapperSQLProxy(ClassWrapper<?> wrapper, GlobalSQLConfiguration configuration) {
        this.wrapper = wrapper;
        this.configuration = configuration;
        runSupports.add(new RunSqlHandler());
        runSupports.add(new RefreshRunner());
        runSupports.add(new XmlProcessor());
        runSupports.add(new FastInsertProcessor());
        runSupports.add(new DoNothingProcessor());
        runSupports.add(new ObtainConnectionRunner());
        runSupports.add(new BaseRunner());
        runSupports.add(new ExecQueryResolver());
    }


    public LocalMap<Method, Configuration> getConfigurationLocalCache() {
        return configurationLocalCache;
    }

    public Map<Method, Supplier<Configuration>> getSupplierMutes() {
        return supplierMutes;
    }

    public void registerConfigSupplier(MethodWrapper mw, Class<?> mapperClass, Supplier<Configuration> configurationSupplier){
        Method method = mw.getMethod();
        if (!supplierMutes.containsKey(method)){
            supplierMutes.put(method, configurationSupplier);
        }
    }


    public void init(){
        //do nothing
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Object[] args = layer.getArgs();
        final Method method = layer.getProxyMethod();
        final Object proxy = layer.getProxyObject();
        final MethodWrapper mw = MethodWrapper.get(method);
        Class<?> agentClass = layer.getAgentClazz();
        final ClassWrapper<?> cw = ClassWrapper.get(agentClass);
        if (isMapperMethod(method)){
            return invokeMapperMethod(layer);
        }
        /*
            检查参数, 目前主要操作只是将如果参数为空的情况下转成空数组
         */
        args = checkArgs(args);
        //判断当前数据源有没有被加载过
        loadDatasource();
        //判断当前方法是不是获取配置类
        if (isGetConfigurationMethod(method)){
            return invokeGetConfigurationMethod();
        }
        //当前方法是不是一个 default 方法, 由于代理的缘故,基本不会出现 default 方法
        if (isDefaultMethod(mw)){
            return invokeDefaultMethod(mw, args, proxy);
        }
        boolean inPipeline = true;
        try {

            //执行前置方法
            invokeBefore(mw, args);
            Object result = null;
            try {
                prepare(mw, cw, args);
                if (!isConfigurerMethod(method, agentClass)){
                    inPipeline = false;
                    result = doNoConfigurerMethod(mw, proxy, args, cw);
                }else {
                    Configuration sqlConfiguration = processorSession(mw, agentClass);
                    result = doPipeline(sqlConfiguration, args, createPacket(sqlConfiguration));
                }
            }catch (Throwable e){
                invokeThrowable(mw, e);
            }finally {
                overMapper(mw, cw);
            }
            return invokeAfter(result, mw);
        }finally {
            end(inPipeline);
        }
    }

    private void overMapper(MethodWrapper mw, ClassWrapper<?> cw){
        StatementSetConfigurationLocal.remove();
    }

    private void prepare(MethodWrapper mw, ClassWrapper<?> cw, Object[] args){
        StatementSetConfigurationLocal.displayConfiguration(mw);
        SyntaxRangeConfigurer rangeConfigurer = SyntaxManager.getRangeConfigurer(configuration.getDataSourceAlias());
        if (rangeConfigurer != null){
            SyntaxConfigurer configurer = rangeConfigurer.getConfigurer();
            StatementValueSetDisplayConfiguration displayConfiguration = configurer.getStatementValueSetDisplayConfiguration();
            if (displayConfiguration != null){
                StatementSetConfigurationLocal.registerConfig(displayConfiguration);
            }
        }
    }

    private boolean isDefaultMethod(MethodWrapper mw){
        return mw.getMethod().isDefault();
    }

    private Object invokeDefaultMethod(MethodWrapper mw, Object[] args, Object proxy) throws Throwable {
        Method method = mw.getMethod();
        MethodHandle handle = handleCache.computeIfAbsent(method, md -> {
            MethodHandle methodHandle = MethodHandlerUtilsByCSDN.getSpecialMethodHandle(method);
            return methodHandle.bindTo(proxy);
        });
        return handle.invokeWithArguments(args);
    }

    private boolean isMapperMethod(Method method){
        return method.getDeclaringClass().equals(Mapper.class);
    }

    private boolean isConfigurerMethod(Method method, Class<?> agentClass){
        return supplierMutes.containsKey(method);
    }

    private Object invokeMapperMethod(AgentObject layer){
        Class<?> agentClazz = layer.getAgentClazz();
        AnnotationMapperSQLApplicationContext context = (AnnotationMapperSQLApplicationContext) getConfiguration().getApplicationContext();
        MapperImpl impl = context.getAndCreateImpl((Class<? extends Mapper>) agentClazz);
        MethodWrapper mw = MethodWrapper.get(layer.getProxyMethod());
        return mw.invoke(impl, layer.getArgs());
    }

    protected ExecutePacket createPacket(Configuration sqlConfiguration){
        return PacketFactory.createPacket(sqlConfiguration);
    }

    protected void loadDatasource(){
        if (ConnectionManagement.existDataSource(configuration.getDataSourceAlias())) {
            ((AnnotationMapperSQLApplicationContext)configuration.getApplicationContext()).loadDataSource();
        }
    }

    protected void end(boolean inPipeline){
        final String alias = configuration.getDataSourceAlias();
        ResultSetThreadManager.close();
        PacketFactory.close();
        configuration.getConformity().clear();
        if (!TransactionSQLManagement.isActivity(alias)) {
            ConnectionManagement.closeCurrentConnection(alias);
        }

        if (inPipeline){
            //处理 syntax
            SyntaxRangeConfigurer rangeConfigurer = SyntaxManager.getRangeConfigurer(alias);
            if (rangeConfigurer != null){
                //增加引用
                rangeConfigurer.addReference();
                //是否需要释放
                if (rangeConfigurer.referencesOrNot()){
                    SyntaxManager.releaseSyntax(alias);
                }
            }
        }
    }

    protected Object invokeAfter(Object result, MethodWrapper mw){
        for (GlobalSQLRunningListener listener : configuration.getApplicationContext().getSQLRunningListeners()) {
            try {
                result = listener.afterInvoke(configuration, mw, result);
            }catch (Throwable e){
                throw new SQLSException("后置处理发生异常, 当前监听者: " + listener, e);
            }
        }
        return result;
    }

    protected void invokeThrowable(MethodWrapper mw, Throwable e) throws Throwable {
        for (GlobalSQLRunningListener listener : configuration.getApplicationContext().getSQLRunningListeners()) {
            try {
                listener.throwableInvoke(configuration, mw, e);
            }catch (Throwable ex){
                e = ex;
            }
        }
        throw e;
    }

    protected void invokeBefore(MethodWrapper mw, Object[] args){
        for (GlobalSQLRunningListener listener : configuration.getApplicationContext().getSQLRunningListeners()) {
            try {
                listener.beforeInvoke(configuration, mw, args);
            }catch (Throwable e){
                throw new SQLSException("前置处理发生异常, 当前监听者: " + listener, e);
            }
        }
    }

    protected Object[] checkArgs(Object[] args){
        if (args == null) {
            args = new Object[0];
        }
        return args;
    }

    protected boolean isGetConfigurationMethod(Method method){
        return method.getReturnType().equals(GlobalSQLConfiguration.class) &&
                method.getName().equals("getConfiguration") &&
                method.getParameterCount() == 0;
    }

    public Object invokeGetConfigurationMethod(){
        return getConfiguration();
    }

    protected Object doNoConfigurerMethod(MethodWrapper mw, Object proxy, Object[] args,  ClassWrapper<?> cw) throws Throwable {
        Method method = mw.getMethod();
        RunSupport support = runSupportCache.computeIfAbsent(method, m -> {
            for (RunSupport runSupport : runSupports) {
                if (runSupport.support(mw)) {
                    return runSupport;
                }
            }
            return null;
        });
        String dataSourceAlias = configuration.getDataSourceAlias();
        try {
            return support == null ? null : support.run(mw, args, configuration, cw);
        }finally {
            if (!TransactionSQLManagement.isActivity(dataSourceAlias)){
                ConnectionManagement.closeCurrentConnection(dataSourceAlias);
            }
        }
    }

    public GlobalSQLConfiguration getConfiguration() {
        return configuration;
    }

    public Configuration getLocalConfiguration(MethodWrapper mw){
        return configurationLocalCache.computeIfAbsent(mw.getMethod(), method -> {
            Supplier<Configuration> supplier = supplierMutes.get(method);
            Assert.notNull(supplier, "supplier<configuration> is null");
            Configuration configuration = supplier.get();
            Assert.notNull(configuration, "supplier do get, but configuration is null");
            return configuration;
        });
    }

    protected Configuration processorSession(MethodWrapper mw, Class<?> agentClass){
        Configuration sqlConfiguration = getLocalConfiguration(mw);
        if (session == null){
            session = configuration.getApplicationContext().openSession();
            sqlConfiguration.setSession(session);
        }

        if (sqlConfiguration.getSession() == null){
            sqlConfiguration.setSession(session);
        }
        return sqlConfiguration;
    }

    protected Object doPipeline(Configuration sqlConfiguration, Object[] args, ExecutePacket executePacket) throws Throwable {

        //前置触发监听效果
        for (GlobalSQLRunningListener listener : sqlConfiguration.getGlobalSQLConfiguration()
                .getApplicationContext()
                .getSQLRunningListeners()) {
            listener.beforeProcessExecution(sqlConfiguration, args);
        }
        Pipeline<AbstractSqlsPipeNode, ExecutePacket, ResultPacket> pipeline = PipelinesManager.getCurrentPipeline();
        executePacket.setArgs(args);
        ApplicationUtil.programRunMills(() ->{
            pipeline.headfire(executePacket);
        }, "pipeline task", sqlConfiguration.getLog(), "===>> ");

        //后置触发监听效果
        for (GlobalSQLRunningListener listener : sqlConfiguration.getGlobalSQLConfiguration()
                .getApplicationContext()
                .getSQLRunningListeners()) {
            listener.afterProcessExecution(sqlConfiguration, args);
        }
        return configuration.getConformity().conformityResult();
    }
}
