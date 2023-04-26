package com.black.core.sql.code;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.session.SQLSignalSession;


import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface SQLApplicationContext {

    void registerGlobalSQLTectonicPeriodListener(GlobalSQLTectonicPeriodListener listener);

    void registerGlobalSQLRunningListener(GlobalSQLRunningListener listener);

    Collection<GlobalSQLRunningListener> getSQLRunningListeners();

    Collection<GlobalSQLTectonicPeriodListener> getSQLTectonicPeriodListeners();

    GlobalSQLConfiguration getConfiguration();

    SQLSignalSession openSession();

    Object bind(MethodWrapper mw, ClassWrapper<?> cw, Supplier<Configuration> supplier);

    <T> T createProxy(ClassWrapper<T> cw);

    void shutdown();

    void loadDataSource();

    Map<Class<?>, MapperSQLProxy> getAgentReslovers();
}
