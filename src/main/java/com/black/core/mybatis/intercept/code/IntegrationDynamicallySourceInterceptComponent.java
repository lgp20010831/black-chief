package com.black.core.mybatis.intercept.code;

import com.black.holder.SpringHodler;
import com.black.core.mybatis.*;
import com.black.core.mybatis.intercept.annotation.DynamicallyIbtaisIntercept;
import com.black.core.mybatis.intercept.annotation.EnableIntegratingIbtaisIntercepts;
import com.black.core.mybatis.source.IbatisDynamicallyMultipleDatabasesComponent;
import com.black.core.mybatis.source.annotation.EnableDynamicallyMultipleClients;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.ReusingProxyFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Modifier;
import java.util.*;

@LoadSort(71)
@Deprecated
@LazyLoading(EnableIntegratingIbtaisIntercepts.class)
public class IntegrationDynamicallySourceInterceptComponent implements OpenComponent, PostPatternClazzDriver {


    private Boolean cancel;

    private final Map<String, IbtaisInterceptsPointHandler> pointHandlerMap = new HashMap<>();

    private final Map<Class<? extends IbatisIntercept>, String[]> globalInterceptors = new HashMap<>();

    private final Map<String, DispatcherWrapper> wrapperCache = new HashMap<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        if(!cancel){
            IbatisDynamicallyMultipleDatabasesComponent databasesComponent = expansivelyApplication.queryComponent(IbatisDynamicallyMultipleDatabasesComponent.class);
            Set<String> aliases = databasesComponent.getConfigurationMap().keySet();
            for (String alias : aliases) {
                globalInterceptors.forEach((c, n) ->{
                    IbtaisInterceptsPointHandler h = pointHandlerMap
                            .computeIfAbsent(alias, a -> new IbtaisInterceptsPointHandler());
                    h.registerEarlyIntercept(c, n);
                });
            }

            Map<String, Configuration> configurationMap = databasesComponent.getConfigurationMap();
            configurationMap.forEach((alias, config) ->{
                IbtaisInterceptsPointHandler handler = pointHandlerMap.get(alias);
                MybatisInterceptsDispatcher dispatcher = obtainMybatisInterceptsDispatcher();
                MybatisInterceptsConfiguartion configuartion = obtainMybatisInterceptsConfiguartion();
                dispatcher.setMybatisInterceptsConfiguartion(configuartion);
                if(handler != null){
                    handler.setMybatisInterceptsConfiguartion(configuartion);
                    handler.setMybatisInterceptsDispatcher(dispatcher);
                    handler.instanceLayers(SpringHodler.getListableBeanFactory(), expansivelyApplication.instanceFactory());
                    dispatcher.add(handler.getMybatisLayers());
                }
                config.addInterceptor(dispatcher);
                wrapperCache.put(alias, new DispatcherWrapper(config, dispatcher));
            });
        }
    }



    @Getter @Setter @AllArgsConstructor
    public static class DispatcherWrapper{
        private Configuration configuration;
        private MybatisInterceptsDispatcher dispatcher;
    }

    @Override
    public void postPatternClazz(Class<?> beanClazz,
                                 Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                 ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        if (cancel == null){
            Class<?> startUpClazz = chiefExpansivelyApplication.getStartUpClazz();
            cancel = AnnotationUtils.getAnnotation(startUpClazz, EnableDynamicallyMultipleClients.class) == null;
        }
        if (!cancel){
            if (beanClazz.isEnum() || beanClazz.isInterface() || Modifier.isAbstract(beanClazz.getModifiers())){
                return;
            }
            if (IbatisIntercept.class.isAssignableFrom(beanClazz)){
                DynamicallyIbtaisIntercept ibtaisIntercept;
                if ( (ibtaisIntercept = AnnotationUtils.getAnnotation(beanClazz, DynamicallyIbtaisIntercept.class)) != null){
                    String alias = ibtaisIntercept.alias();
                    if ("".equals(alias)){
                        globalInterceptors.put((Class<? extends IbatisIntercept>) beanClazz, ibtaisIntercept.value());
                    }else {
                        IbtaisInterceptsPointHandler ibtaisInterceptsPointHandler = pointHandlerMap
                                .computeIfAbsent(alias, k -> new IbtaisInterceptsPointHandler());
                        ibtaisInterceptsPointHandler.registerEarlyIntercept((Class<? extends IbatisIntercept>) beanClazz, ibtaisIntercept.value());
                    }
                }
            }
        }
    }

    protected MybatisInterceptsDispatcher obtainMybatisInterceptsDispatcher(){
        return new MybatisInterceptsDispatcher();
    }

    protected MybatisInterceptsConfiguartion obtainMybatisInterceptsConfiguartion(){
        return new MybatisInterceptsConfiguartion();
    }

    public Map<String, DispatcherWrapper> getWrapperCache() {
        return wrapperCache;
    }
}
