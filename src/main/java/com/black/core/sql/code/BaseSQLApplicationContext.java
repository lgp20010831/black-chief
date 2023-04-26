package com.black.core.sql.code;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.session.DefaultSQLSession;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.shutdown.SQLContextShutdownHook;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Log4j2
public class BaseSQLApplicationContext implements SQLApplicationContext{

    protected boolean load = false;

    public static final String DEFAULT_ALIAS = "master";

    private final Collection<GlobalSQLRunningListener> runningListeners = new ArrayList<>();

    protected final Collection<SQLSignalSession> sessionManager = new ArrayList<>();

    private final Collection<GlobalSQLTectonicPeriodListener> tectonicPeriodListeners = new ArrayList<>();

    protected final ReusingProxyFactory proxyFactory;

    protected final GlobalSQLConfiguration configuration;

    protected final Map<Class<?>, Object> agents = new ConcurrentHashMap<>();

    protected final Map<Class<?>, MapperSQLProxy> agentReslovers = new ConcurrentHashMap<>();

    public BaseSQLApplicationContext(@NonNull GlobalSQLConfiguration configuration) {
        this.configuration = configuration;
        proxyFactory = FactoryManager.getProxyFactory();
        configuration.setApplicationContext(this);
        if (!ChiefApplicationRunner.isOpen()) {
            SQLContextShutdownHook shutdownHook = SQLContextShutdownHook.getInstance();
            shutdownHook.addContext(this);
            shutdownHook.registerHook();
        }
    }

    @Override
    public Object bind(MethodWrapper mw, ClassWrapper<?> cw, Supplier<Configuration> supplier) {
        final Class<?> mapperClass = cw.getPrimordialClass();
        if (!agents.containsKey(mapperClass)){
             createProxy(cw);
        }
        Object proxy = agents.get(mapperClass);
        MapperSQLProxy proxyResolver = agentReslovers.get(mapperClass);
        proxyResolver.registerConfigSupplier(mw, cw.getPrimordialClass(), supplier);
        return proxy;
    }

    @Override
    public <T> T createProxy(ClassWrapper<T> cw) {
        return (T) agents.computeIfAbsent(cw.getPrimordialClass(), c -> {
            MapperSQLProxy resolver;
            Object jdk = proxyFactory.proxy(c, resolver = createMapperProxy(cw));
            agentReslovers.put(c, resolver);
            return jdk;
        });
    }

    protected MapperSQLProxy createMapperProxy(ClassWrapper<?> cw){
        return new MapperSQLProxy(cw, getConfiguration());
    }

    @Override
    public void registerGlobalSQLTectonicPeriodListener(GlobalSQLTectonicPeriodListener listener) {
        if (listener != null){
            tectonicPeriodListeners.add(listener);
        }
    }

    @Override
    public void registerGlobalSQLRunningListener(GlobalSQLRunningListener listener) {
        if (listener != null){
            if (!existListenerType(listener)){
                runningListeners.add(listener);
            }
        }
    }

    public boolean existListenerType(GlobalSQLRunningListener listener){
        Class<? extends GlobalSQLRunningListener> listenerClass = listener.getClass();
        for (GlobalSQLRunningListener runningListener : runningListeners) {
            if (listenerClass.equals(runningListener.getClass())){
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<GlobalSQLRunningListener> getSQLRunningListeners() {
        return runningListeners;
    }

    @Override
    public Collection<GlobalSQLTectonicPeriodListener> getSQLTectonicPeriodListeners() {
        return tectonicPeriodListeners;
    }

    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public SQLSignalSession openSession() {
        DefaultSQLSession session = new DefaultSQLSession(configuration);
        sessionManager.add(session);
        return session;
    }

    @Override
    public void shutdown() {
        log.info("shutdown sqlContext: [" + configuration.getDataSourceAlias() + "]");
        for (GlobalSQLTectonicPeriodListener periodListener : getSQLTectonicPeriodListeners()) {
            periodListener.turnShutdown(this);
        }

        for (SQLSignalSession signalSession : sessionManager) {
            signalSession.close();
        }
        sessionManager.clear();
        ConnectionManagement.shutdown(configuration.getDataSourceAlias());
    }

    public Map<Class<?>, MapperSQLProxy> getAgentReslovers() {
        return agentReslovers;
    }

    public void loadDataSource(){
        if (load){
            return;
        }
        try {
            String alias = configuration.getDataSourceAlias();
            if (ConnectionManagement.existDataSource(alias)) {
                if (alias == null){
                    throw new IllegalArgumentException("alias cannot be null");
                }
                DataSourceBuilder builder = configuration.getDataSourceBuilder();
                if (builder == null){
                    throw new IllegalArgumentException("builder cannot be null");
                }

                DataSource dataSource = builder.getDataSource();
                for (GlobalSQLTectonicPeriodListener tectonicPeriodListener : getSQLTectonicPeriodListeners()) {
                    tectonicPeriodListener.postDataSource(dataSource, alias);
                }

                ConnectionManagement.registerDataSource(alias, dataSource);
                if (configuration.isInitTable()) {
                    for (Class<?> zc : agentReslovers.keySet()) {
                        MapperSQLProxy proxy = agentReslovers.get(zc);
                        try {
                            proxy.init();
                        }catch (Throwable e){
                            log.warn("init proxy: [{}] fail", zc.getSimpleName());
                        }
                    }
                }
            }
        }finally {
            load = true;
        }
    }
}
