package com.black.core.sql.code.component;

import com.black.core.chain.*;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.ClosableSort;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.annotation.*;
import com.black.core.sql.code.AnnotationMapperSQLApplicationContext;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.GlobalSQLTectonicPeriodListener;
import com.black.core.sql.code.MapperRegister;
import com.black.core.tools.BeanUtil;
import com.black.holder.SpringHodler;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@ClosableSort(1000)
@LoadSort(7856)
@ChainClient(SQLMapComponent.class)
@LazyLoading(EnabledMapSQLApplication.class)
public class SQLMapComponent implements OpenComponent, CollectedCilent, ChainPremise, ApplicationDriver {

    private DefaultListableBeanFactory beanFactory;
    private final Collection<Object> mapperClasses = new HashSet<>();
    private final Collection<Object> listeners = new HashSet<>();
    private final Map<String, Object> mapperCache = new ConcurrentHashMap<>();
    private InstanceFactory instanceFactory;
    private MapperRegister mapperRegister;

    public SQLMapComponent(){
        SQLMapperHolder.mapComponent = this;
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        instanceFactory = expansivelyApplication.instanceFactory();
        beanFactory = SpringHodler.getListableBeanFactory();
        mapperRegister = MapperRegister.getInstance();
        processor();
        if (log.isInfoEnabled()) {
            log.info("SQL Mapper: {}", mapperCache.keySet());
        }
    }

    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        if (mapperRegister != null){
            mapperRegister.shutdown();
        }
        ApplicationDriver.super.whenApplicationStop(application);
    }

    protected void processor(){
        for (Object mapperClass : mapperClasses) {
            Class<?> mapperClazz = (Class<?>) mapperClass;
            String name = NameUtil.getName(mapperClazz);
            Object mapper = mapperRegister.getMapper(mapperClazz);
            mapperCache.put(name, mapper);
            beanFactory.registerSingleton(name, mapper);
        }
        Map<String, AnnotationMapperSQLApplicationContext> contextCache = mapperRegister.getContextCache();
        Map<String, Collection<Object>> listenerAliasCache = new HashMap<>();
        for (Object listener : listeners) {
            SQLListener annotation = AnnotationUtils.getAnnotation(BeanUtil.getPrimordialClass(listener), SQLListener.class);
            String[] values = annotation.value();
            if (values.length == 1 && "*".equals(values[0])){
                for (String alias : contextCache.keySet()) {
                    Collection<Object> collection = listenerAliasCache.computeIfAbsent(alias, ali -> {
                        return new HashSet<>();
                    });
                    collection.add(listener);
                }
            }else {
                for (String value : values) {
                    if (!contextCache.containsKey(value)){
                        throw new IllegalArgumentException("该监听者无效, 因为不存在此别名数据源");
                    }
                    Collection<Object> collection = listenerAliasCache.computeIfAbsent(value, ali -> {
                        return new HashSet<>();
                    });
                    collection.add(listener);
                }
            }
        }

        for (String alias : contextCache.keySet()) {
            AnnotationMapperSQLApplicationContext context = mapperRegister.getContextCache().get(alias);
            Collection<Object> collection = listenerAliasCache.get(alias);
            if (collection != null){
                for (Object obj : collection) {
                    if (obj instanceof GlobalSQLTectonicPeriodListener){
                        context.registerGlobalSQLTectonicPeriodListener((GlobalSQLTectonicPeriodListener) obj);
                    }

                    if (obj instanceof GlobalSQLRunningListener){
                        context.registerGlobalSQLRunningListener((GlobalSQLRunningListener) obj);
                    }
                }
            }
        }
    }

    public <T> T getMapper(Class<T> mapperClass){
        for (Object value : mapperCache.values()) {
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(value);
            if (mapperClass.isAssignableFrom(primordialClass)){
                return (T) value;
            }
        }
        return null;
    }

    public Object getMapper(String name){
        return mapperCache.get(name);
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("mapper", mpc ->{
            return mpc.isInterface() && AnnotationUtils.getAnnotation(mpc, ExcludeScanMapper.class) == null &&
                    (AnnotationUtils.getAnnotation(mpc, GlobalConfiguration.class) != null ||
                            AnnotationUtils.getAnnotation(mpc, ImportMapper.class) != null ||
                            AnnotationUtils.getAnnotation(mpc, ImportMapperAndPlatform.class) != null);
        }).instance(false);

        register.begin("listener", muy ->{
            return (GlobalSQLRunningListener.class.isAssignableFrom(muy) ||
                    GlobalSQLTectonicPeriodListener.class.isAssignableFrom(muy)) &&
                    AnnotationUtils.getAnnotation(muy, SQLListener.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("mapper".equals(resultBody.getAlias())){
            mapperClasses.addAll(resultBody.getCollectSource());
        }

        if ("listener".equals(resultBody.getAlias())){
            listeners.addAll(resultBody.getCollectSource());
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledMapSQLApplication.class);
    }
}
